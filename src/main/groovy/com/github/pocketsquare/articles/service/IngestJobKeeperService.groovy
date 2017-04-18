package com.github.pocketsquare.articles.service

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import io.github.yermilov.kerivnyk.domain.Job
import io.github.yermilov.kerivnyk.domain.JobStatus
import io.github.yermilov.kerivnyk.service.DurableJob
import io.github.yermilov.kerivnyk.service.KerivnykService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class IngestJobKeeperService {

    static final String USERS_SERVICE_URL = 'http://pocket-square-users:8080/'

    static final String DEFAULT_SUSPEND_DURATION = '10min'
    static final String SUSPEND_DURATION = System.getenv('INGEST_JOB_KEEPER_SUSPEND_DURATION') ?: DEFAULT_SUSPEND_DURATION

    @Autowired
    KerivnykService kerivnykService

    @Autowired
    ArticlesIngestService articlesIngestService

    @Autowired
    JsonSlurper jsonSlurper

    void startKeeper() {
        log.info 'starting keeper job...'
        kerivnykService.startJob(keeperJob())
    }

    DurableJob keeperJob() {
        return new DurableJob('keep articles ingest job running for all users') {

            HttpBuilder usersService

            @Override
            boolean canStart(boolean isNew, Collection<Job> concurrentJobs) {
                concurrentJobs.find({ Job job -> job.name == this.name }) == null
            }

            @Override
            void init() {
                usersService = HttpBuilder.configure {
                    request.uri = USERS_SERVICE_URL
                }
            }

            @Override
            void act() {
                try {
                    Collection<Job> activeJobs = kerivnykService.activeJobs
                    Collection<Job> allJobs = kerivnykService.allJobs

                    def response = usersService.get {
                        request.uri.path = '/user'
                    }

                    // temporary fix as response is byte array for some reason
                    def jsonResponse = jsonSlurper.parse(response)

                    jsonResponse['_embedded']['user']
                            .collect { def user ->
                                String userHref = user['_links']['self']['href']
                                user.id = userHref.substring(userHref.lastIndexOf('/') + 1, userHref.length() - 1)
                            }
                            .each { def user ->
                                Job activeJob = activeJobs.find { Job job -> job.storage.userId == user.id }
                                if (activeJob != null) {
                                    log.info "job for user with id=${user.id} is active"
                                } else {
                                    Job abortedJob = allJobs.findAll { Job job -> job.storage.userId == user.id && job.status == JobStatus.ABORTED.toString() }
                                    if (abortedJob != null) {
                                        log.warn "job for user with id=${user.id} was aborted, restarting it..."
                                        kerivnykService.restartJobFrom(articlesIngestService.ingestArticlesByUserIdJob(user.id), abortedJob)
                                    } else {
                                        log.warn "job for user with id=${user.id} is not active, starting it..."
                                        kerivnykService.startJob(articlesIngestService.ingestArticlesByUserIdJob(user.id))
                                    }
                                }
                            }
                } catch (e) {
                    log.warn("failed during keeping articles ingest job running for all users because of ${e.class}: ${e.message}", e)
                } finally {
                    suspend(SUSPEND_DURATION)
                }
            }
        }
    }
}

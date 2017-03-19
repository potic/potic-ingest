package com.github.pocketsquare.articles.service

import com.github.pocketsquare.articles.domain.Article
import com.github.pocketsquare.articles.repository.ArticleRepository
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import io.github.yermilov.kerivnyk.domain.Job
import io.github.yermilov.kerivnyk.service.DurableJob
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class ArticlesStorageService {

    static final String INGEST_SERVICE_URL = 'http://pocket-square-ingest:5000/'

    static final Integer DEFAULT_REQUEST_SIZE = 100
    static final Integer INGEST_REQUEST_SIZE = Integer.parseInt(System.getenv('INGEST_REQUEST_SIZE') ?: DEFAULT_REQUEST_SIZE.toString())

    static final String DEFAULT_SUSPEND_DURATION = '1h'
    static final String SUSPEND_DURATION = System.getenv('SUSPEND_DURATION') ?: DEFAULT_SUSPEND_DURATION

    @Autowired
    ArticleRepository articleRepository

    @Autowired
    JsonSlurper jsonSlurper

    void deleteByUserId(String userId) {
        articleRepository.deleteByUserId(userId)
        log.info "successfully removed all articles of user with id=${userId} from database"
    }

    DurableJob ingestArticlesByUserIdJob(String userId) {
        new DurableJob("ingest articles for user-id=${userId}") {

            HttpBuilder ingestService

            int requestSize
            int offset

            @Override
            boolean canStart(Collection<Job> concurrentJobs) {
                concurrentJobs.find({ Job job -> job.name == this.name }) == null
            }

            @Override
            void init() {
                ingestService = HttpBuilder.configure {
                    request.uri = INGEST_SERVICE_URL
                }

                requestSize = INGEST_REQUEST_SIZE
                offset = 0

                dashboard.userId = userId
                dashboard.ingestedCount = 0
                dashboard.userArticlesCount = articleRepository.countByUserId(userId)
            }

            @Override
            void act() {
                try {
                    log.info "requesting ${requestSize} articles for user with id=${userId} with offset=${offset}"

                    def response = ingestService.get {
                        request.uri.path = "/fetch/${userId}"
                        request.uri.query = [count: requestSize, offset: offset]
                    }

                    // temporary fix as response is jsoup document for some reason
                    def jsonResponse = jsonSlurper.parseText((response as Document).body().html())

                    if (jsonResponse.empty) {
                        log.info "all articles ingested so far for user with id=${userId}"
                        offset = 0
                        suspend(SUSPEND_DURATION)
                        return
                    }

                    Collection<Article> articles = jsonResponse.values().findAll({ it.is_article == '1' }).collect({
                        Article.builder()
                                .userId(userId)
                                .pocketId(it.resolved_id)
                                .givenUrl(it.given_url)
                                .resolvedUrl(it.resolved_url)
                                .title(it.resolved_title)
                                .order(it.sort_id)
                                .read(it.status == '1')
                                .favorite(it.favorite == '1')
                                .wordCount(Integer.parseInt(it.word_count))
                                .tags(it.tags?.keySet() ?: [])
                                .authors(it.authors?.values()?.collect({ it.name }) ?: [])
                                .build()
                    })

                    log.info "received ${articles.size()} articles for user with id=${userId}"
                    Collection<Article> existingArticles = articles.findAll({ Article article ->
                        Article existingArticle = articleRepository.findOneByUserIdAndPocketId(article.userId, article.pocketId)
                        article.id = existingArticle?.id
                        article.content = existingArticle?.content
                        return existingArticle != null
                    })
                    log.info "updating metadata of ${existingArticles.size()} existing articles for user with id=${userId}"
                    articleRepository.save existingArticles

                    Collection<Article> articlesWithoutContent = articles.findAll({ Article article -> article.content == null })
                    log.info "ingesting content of ${articlesWithoutContent.size()} articles for user with id=${userId}"
                    articlesWithoutContent.eachWithIndex { Article article, int index ->
                        log.info "processing article ${index + 1} / ${articlesWithoutContent.size()} for user with id=${userId}"
                        try {
                            log.info "trying to load ${article.givenUrl} for user with id=${userId}"
                            article.content = Jsoup.connect(article.givenUrl).get().html()
                        } catch (e) {
                            log.warn "failed to load ${article.givenUrl} for user with id=${userId}"
                        }
                        if (article.content == null) {
                            try {
                                log.info "trying to load ${article.resolvedUrl} for user with id=${userId}"
                                article.content = Jsoup.connect(article.resolvedUrl).get().html()
                            } catch (e) {
                                log.warn "failed to load ${article.resolvedUrl} for user with id=${userId}"
                            }
                        }
                        articleRepository.save article
                        dashboard.ingestedCount++
                    }
                    offset += requestSize
                } catch (e) {
                    log.warn("failed during ingesting articles for user with id=${userId} with offset=${offset} because of ${e.class}: ${e.message}", e)
                    offset++
                } finally {
                    dashboard.userArticlesCount = articleRepository.countByUserId(userId)
                }
            }
        }
    }
}

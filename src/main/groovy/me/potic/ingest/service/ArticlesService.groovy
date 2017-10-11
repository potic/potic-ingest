package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class ArticlesService {

    HttpBuilder articlesServiceRest

    @Autowired
    HttpBuilder articlesServiceRest(@Value('${services.articles.url}') String articlesServiceUrl) {
        articlesServiceRest = HttpBuilder.configure {
            request.uri = articlesServiceUrl
        }
    }

    @Timed(name = 'upsertArticles')
    void upsertArticles(String userId, Collection<Map> ingestedArticles) {
        log.info "upserting ${ingestedArticles.size()} articles for user #${userId}..."

        ingestedArticles.each { Map article ->
            articlesServiceRest.post {
                request.uri.path = "/user/${userId}/article/fromPocket"
                request.body = article
                request.contentType = 'application/json'
            }
        }
    }
}

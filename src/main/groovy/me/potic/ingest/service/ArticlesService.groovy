package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.ingest.domain.PocketArticle
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
    void upsertArticles(String userId, Collection<PocketArticle> ingestedArticles) {
        log.debug "upserting ${ingestedArticles.size()} articles for user #${userId}..."

        ingestedArticles.each { PocketArticle article ->
            articlesServiceRest.post {
                request.uri.path = "/user/${userId}/article/fromPocket"
                request.body = article
                request.contentType = 'application/json'
            }
        }
    }
}

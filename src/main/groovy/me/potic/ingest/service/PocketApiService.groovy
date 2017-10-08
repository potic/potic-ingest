package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.ingest.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class PocketApiService {

    HttpBuilder pocketApiRest

    @Autowired
    JsonSlurper jsonSlurper

    @Autowired
    HttpBuilder pocketApiRest(@Value('${services.pocketApi.url}') String pocketApiServiceUrl) {
        pocketApiRest = HttpBuilder.configure {
            request.uri = pocketApiServiceUrl
        }
    }

    @Timed(name = 'ingestArticlesByUser')
    Collection<Map> ingestArticlesByUser(User user, long count, long since) {
        log.info "requesting ${count} articles for user with id=${user.id} since ${since}"

        Map response = pocketApiRest.get(Map) {
            request.uri.path = "/get/${user.pocketAccessToken}"
            request.uri.query = [ count: count, since: since ]
        }

        Collection<Map> ingestedArticles = response.values()

        log.info "received ${ingestedArticles.size()} articles for user with id=${user.id} since ${since}"
        return ingestedArticles
    }
}

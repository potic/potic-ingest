package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.ingest.domain.PocketArticle
import me.potic.ingest.domain.PocketAuthor
import me.potic.ingest.domain.PocketImage
import me.potic.ingest.domain.PocketVideo
import me.potic.ingest.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class PocketApiService {

    HttpBuilder pocketApiRest

    @Autowired
    HttpBuilder pocketApiRest(@Value('${services.pocketApi.url}') String pocketApiServiceUrl) {
        pocketApiRest = HttpBuilder.configure {
            request.uri = pocketApiServiceUrl
        }
    }

    @Timed(name = 'ingestArticlesByUser')
    Collection<PocketArticle> ingestArticlesByUser(User user, long count, long since) {
        log.info "requesting ${count} articles for user with id=${user.id} since ${since}"

        Map response = pocketApiRest.get(Map) {
            request.uri.path = "/get/${user.pocketAccessToken}"
            request.uri.query = [ count: count, since: since ]
        }

        Collection<PocketArticle> ingestedArticles = response.values().collect({ Map eachResponse ->
            log.debug(eachResponse.toString())

            if (eachResponse.time_added != null) eachResponse.time_added = Long.parseLong(eachResponse.time_added)
            if (eachResponse.time_updated != null) eachResponse.time_updated = Long.parseLong(eachResponse.time_updated)
            if (eachResponse.time_read != null) eachResponse.time_read = Long.parseLong(eachResponse.time_read)
            if (eachResponse.time_favorited != null) eachResponse.time_favorited = Long.parseLong(eachResponse.time_favorited)
            if (eachResponse.word_count != null) eachResponse.word_count = Long.parseLong(eachResponse.word_count)

            if (eachResponse.images != null) eachResponse.images = eachResponse.images.values().collect({ new PocketImage(it) })

            if (eachResponse.authors != null) eachResponse.authors = eachResponse.authors.values().collect({ new PocketAuthor(it) })

            if (eachResponse.videos != null) eachResponse.videos = eachResponse.videos.values().collect({ new PocketVideo(it) })

            new PocketArticle(eachResponse)
        })

        log.info "received ${ingestedArticles.size()} articles for user with id=${user.id} since ${since}"
        return ingestedArticles
    }
}

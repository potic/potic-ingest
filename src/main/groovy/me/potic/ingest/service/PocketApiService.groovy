package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.ingest.domain.Article
import me.potic.ingest.domain.Image
import org.jsoup.nodes.Document
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

    @Timed(name = 'getArticlesByUserId')
    Collection<Article> getArticlesByUser(User user, long count, long since) {
        log.info "requesting ${count} articles for user with id=${user.id} since ${since}"

        def response = pocketApiRest.get {
            request.uri.path = "/get/${user.pocketAccessToken}"
            request.uri.query = [ count: count, since: since ]
        }

        Map jsonResponse = toJson(response)

        Collection<Article> articles = jsonResponse.values().collect({
            Article.builder()
                    .userId(user.id)
                    .pocketId(it.resolved_id)
                    .givenUrl(it.given_url)
                    .resolvedUrl(it.resolved_url)
                    .title(it.resolved_title)
                    .read(it.status == '1')
                    .favorite(it.favorite == '1')
                    .wordCount(it.word_count != null ? Integer.parseInt(it.word_count) : 0)
                    .tags(it.tags?.keySet() ?: [])
                    .authors(it.authors?.values()?.collect({ it.name }) ?: [])
                    .source(extractSource(it))
                    .mainImage(extractMainImage(it.images))
                    .excerpt(it.excerpt)
                    .timeAdded(it.time_added != null ? Long.parseLong(it.time_added) : 0)
                    .timeFavored(it.time_favorited != null ? Long.parseLong(it.time_favorited) : 0)
                    .timeRead(it.time_read != null ? Long.parseLong(it.time_read) : 0)
                    .timeUpdated(it.time_updated != null ? Long.parseLong(it.time_updated) : 0)
                    .build()
        })

        log.info "received ${articles.size()} articles for user with id=${user.id} since ${since}"
        return articles
    }

    def toJson(def response) {
        // temporary fix as response is jsoup document for some reason
        String responseAsString = (response as Document).body().html()

        // temporary fix as these substrings break json parsing
        responseAsString = responseAsString.replace("<script src=\"\\&quot;https://d3js.org/d3.v4.js\\&quot;\"></script>", "")
        responseAsString = responseAsString.replace("<script src=\"\\&quot;https://unpkg.com/d3-horizon-chart\\&quot;\"></script>", "")

        return jsonSlurper.parseText(responseAsString)
    }

    static Image extractMainImage(json) {
        if (json == null || json.empty) {
            null
        } else {
            def mainImageJson = json.values().min({ Integer.parseInt(it.image_id) })

            Image.builder()
                    .pocketId("${mainImageJson.item_id}-${mainImageJson.image_id}")
                    .src(mainImageJson.src)
                    .width(Integer.parseInt(mainImageJson.width))
                    .height(Integer.parseInt(mainImageJson.height))
                    .credit(mainImageJson.credit)
                    .caption(mainImageJson.caption)
                    .build()
        }
    }

    static String extractSource(json) {
        String url = json.resolved_url ?: json.given_url
        if (url == null) {
            return null
        }

        int startIndex = 0
        int endIndex

        if (url.startsWith('http://')) {
            startIndex = 'http://'.length()
        }

        if (url.startsWith('https://')) {
            startIndex = 'https://'.length()
        }

        if (url.substring(startIndex).startsWith('www.')) {
            startIndex += 'www.'.length()
        }

        if (url.substring(startIndex).startsWith('m.')) {
            startIndex += 'm.'.length()
        }

        endIndex = url.indexOf('/', startIndex)
        if (endIndex < 0) {
            endIndex = url.length()
        }

        return url.substring(startIndex, endIndex)
    }
}

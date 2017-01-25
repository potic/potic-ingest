package com.github.pocketsquare.articles

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@Slf4j
class ArticlesController {

    static final String INGEST_URL = 'http://pocket_square_ingest:28102/fetch/'

    RestTemplate restTemplate = new RestTemplate()

    JsonSlurper jsonSlurper = new JsonSlurper()

    @GetMapping(path = '/articles/{userId}')
    @ResponseBody Collection<Article> all(@PathVariable Integer userId) {
        log.info 'Receive request to GET all articles'

        allArticles(userId)
    }

    @GetMapping(path = '/articles/{userId}/unread')
    @ResponseBody Collection<Article> unread(@PathVariable Integer userId) {
        log.info 'Receive request to GET unread articles'

        allArticles(userId).findAll({ !it.read })
    }

    @GetMapping(path = '/articles/{userId}/read')
    @ResponseBody Collection<Article> read(@PathVariable Integer userId) {
        log.info 'Receive request to GET read articles'

        allArticles(userId).findAll({ it.read })
    }

    Collection<Article> allArticles(Integer userId) {
        String ingestAsString = restTemplate.getForObject("${INGEST_URL}${userId}", String)
        def ingestAsJson = jsonSlurper.parseText(ingestAsString)
        Collection<Article> articles = ingestAsJson.values().findAll({ it.is_article == '1' }).collect({
            Article.builder()
                    .url(it.given_url)
                    .title(it.resolved_title)
                    .order(it.sort_id)
                    .read(it.status == '1')
                    .favorite(it.favorite == '1')
                    .wordCount(Integer.parseInt(it.word_count))
                    .tags(it.tags?.keySet()?:[])
                    .authors(it.authors?.values()?.collect({ it.name })?:[])
                    .build()
        })

        return articles
    }
}

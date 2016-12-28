package com.github.pocketsquare.articles

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController('/articles')
@Slf4j
class ArticlesController {

    static final String INGEST_URL = '/ingest'

    RestTemplate restTemplate = new RestTemplate()

    JsonSlurper jsonSlurper = new JsonSlurper()

    @GetMapping(path = '/')
    @ResponseBody Collection<Article> all() {
        log.info 'Receive request to GET all articles'

        allArticles()
    }

    @GetMapping(path = '/unread')
    @ResponseBody Collection<Article> unread() {
        log.info 'Receive request to GET unread articles'

        allArticles().findAll({ !it.read })
    }

    @GetMapping(path = '/read')
    @ResponseBody Collection<Article> read() {
        log.info 'Receive request to GET read articles'

        allArticles().findAll({ it.read })
    }

    Collection<Article> allArticles() {
        String ingestAsString = restTemplate.getForObject(INGEST_URL, String)
        def ingestAsJson = jsonSlurper.parseText(ingestAsString)
        Collection<Article> articles = ingestAsJson.values().findAll({ it.is_article == '1' }).collect({
            Article.builder()
                    .url(it.given_url)
                    .title(it.resolved_title)
                    .order(it.sort_id)
                    .read(it.status == '1')
                    .favorite(it.favorite == '1')
                    .excerpt(it.excerpt)
                    .wordCount(Integer.parseInt(it.word_count))
                    .tags(it.tags?.keySet()?:[])
                    .authors(it.authors?.values()?.collect({ it.name })?:[])
                    .build()
        })

        return articles
    }
}

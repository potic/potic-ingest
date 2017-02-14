package com.github.pocketsquare.articles.controller

import com.github.pocketsquare.articles.domain.Article
import com.github.pocketsquare.articles.repository.ArticleRepository
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@Slf4j
class ArticlesController {

    static final String INGEST_URL = 'http://pocket_square_ingest:5000/fetch/'

    RestTemplate restTemplate = new RestTemplate()

    JsonSlurper jsonSlurper = new JsonSlurper()

    @Autowired
    ArticleRepository articleRepository

    @GetMapping(path = '/article/clean')
    @ResponseBody Collection<Article> clean() {
        log.info 'Receive request to save articles to database'

        articleRepository.deleteAll()
    }

    @GetMapping(path = '/article/save/{userId}')
    @ResponseBody Collection<Article> save(@PathVariable String userId) {
        log.info 'Receive request to save articles to database'

        articleRepository.save(fetchArticles(userId))
    }

    Collection<Article> fetchArticles(String userId) {
        String ingestAsString = restTemplate.getForObject("${INGEST_URL}${userId}", String)
        def ingestAsJson = jsonSlurper.parseText(ingestAsString)
        Collection<Article> articles = ingestAsJson.values().findAll({ it.is_article == '1' }).collect({
            Article.builder()
                    .userId(userId)
                    .url(it.given_url)
                    .title(it.resolved_title)
                    .order(it.sort_id)
                    .read(it.status == '1')
                    .favorite(it.favorite == '1')
                    .wordCount(Integer.parseInt(it.word_count))
                    .tags(it.tags?.keySet()?:[])
                    .authors(it.authors?.values()?.collect({ it.name })?:[])
                    .content(Jsoup.connect(it.resolved_url).get().html())
                    .build()
        })

        return articles
    }
}

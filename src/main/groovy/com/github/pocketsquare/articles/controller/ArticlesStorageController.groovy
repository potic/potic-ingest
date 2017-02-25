package com.github.pocketsquare.articles.controller

import com.github.pocketsquare.articles.domain.Article
import com.github.pocketsquare.articles.repository.ArticleRepository
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@Slf4j
class ArticlesStorageController {

    static final String INGEST_URL = 'http://pocket_square_ingest:5000/fetch/'

    RestTemplate restTemplate = new RestTemplate()

    JsonSlurper jsonSlurper = new JsonSlurper()

    @Autowired
    ArticleRepository articleRepository

    @DeleteMapping(path = '/article')
    @ResponseBody Collection<Article> removeAllArticles() {
        log.info 'Receive request to remove all articles from database'

        articleRepository.deleteAll()

        log.info "removing finished successfully"
    }

    @PostMapping(path = '/article/byUserId/{userId}/ingest')
    @ResponseBody Collection<Article> ingestArticlesByUserId(@PathVariable String userId, @RequestParam('maxArticlesCount') Integer maxArticlesCount) {
        log.info 'Receive request to save articles to database'

        String response = restTemplate.getForObject("${INGEST_URL}${userId}", String)
        Map fetchedByUserId = jsonSlurper.parseText(response)

        Collection<Article> articles = fetchedByUserId.values().findAll({ it.is_article == '1' }).collect({
            Article.builder()
                    .userId(userId)
                    .givenUrl(it.given_url)
                    .resolvedUrl(it.resolved_url)
                    .title(it.resolved_title)
                    .order(it.sort_id)
                    .read(it.status == '1')
                    .favorite(it.favorite == '1')
                    .wordCount(Integer.parseInt(it.word_count))
                    .tags(it.tags?.keySet()?:[])
                    .authors(it.authors?.values()?.collect({ it.name })?:[])
                    .build()
        })

        if (maxArticlesCount != null) {
            articles = articles.take(maxArticlesCount)
        }

        articles.eachWithIndex { Article article, int index ->
            log.info "processing article ${index + 1} / ${articles.size()}..."
            try {
                log.info "trying to load ${article.givenUrl}..."
                article.content = Jsoup.connect(article.givenUrl).get().html()
            } catch (e) {
                log.warn "failed to load ${article.givenUrl}..."
            }
            if (article.content == null) {
                try {
                    log.info "trying to load ${article.resolvedUrl}..."
                    article.content = Jsoup.connect(article.resolvedUrl).get().html()
                } catch (e) {
                    log.warn "failed to load ${article.resolvedUrl}..."
                }
            }
            articleRepository.save article
        }

        log.info "ingestion finished successfully"
    }
}

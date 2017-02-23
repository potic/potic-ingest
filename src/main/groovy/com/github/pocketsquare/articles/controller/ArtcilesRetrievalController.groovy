package com.github.pocketsquare.articles.controller

import com.github.pocketsquare.articles.domain.Article
import com.github.pocketsquare.articles.repository.ArticleRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class ArtcilesRetrievalController {

    @Autowired
    ArticleRepository articleRepository

    @GetMapping(path = '/article/byUserId/{userId}/unread')
    @ResponseBody Collection<Article> getUnreadByUserId(@PathVariable String userId) {
        articleRepository.findByUserId(userId).findAll({ !it.read })
    }
}

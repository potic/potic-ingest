package com.github.pocketsquare.articles.controller

import com.github.pocketsquare.articles.domain.Article
import com.github.pocketsquare.articles.repository.ArticleRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class ArticlesRetrievalController {

    @Autowired
    ArticleRepository articleRepository

    @CrossOrigin
    @GetMapping(path = '/article/byUserId/{userId}/unread')
    @ResponseBody Collection<Article> getUnreadByUserId(@PathVariable String userId, @RequestParam('page') Integer page, @RequestParam('size') Integer size) {
        articleRepository.findByUserIdAndRead(userId, false, new PageRequest(page, size, new Sort(Sort.Direction.DESC, 'timeAdded')))
    }
}

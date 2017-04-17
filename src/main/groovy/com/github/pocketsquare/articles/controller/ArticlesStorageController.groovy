package com.github.pocketsquare.articles.controller

import com.github.pocketsquare.articles.service.ArticlesIngestService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class ArticlesStorageController {

    @Autowired
    ArticlesIngestService articlesIngestService

    @DeleteMapping(path = '/article/byUserId/{userId}')
    void removeAllUserArticles(@PathVariable String userId) {
        log.info "Receive request to remove all articles of user with id=${userId} from database"

        articlesIngestService.deleteByUserId(userId)
    }
}

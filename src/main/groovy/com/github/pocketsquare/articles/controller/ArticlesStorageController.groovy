package com.github.pocketsquare.articles.controller

import com.github.pocketsquare.articles.service.ArticlesStorageService
import groovy.util.logging.Slf4j
import io.github.yermilov.kerivnyk.domain.Job
import io.github.yermilov.kerivnyk.service.KerivnykService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class ArticlesStorageController {

    @Autowired
    ArticlesStorageService articlesStorageService

    @Autowired
    KerivnykService kerivnykService

    @PostMapping(path = '/ingest/{userId}')
    @ResponseBody Job ingestArticlesByUserId(@PathVariable String userId) {
        log.info "Receive request to ingest articles of user with id=${userId}"

        kerivnykService.asyncStartJob(articlesStorageService.ingestArticlesByUserIdJob(userId))
    }

    @DeleteMapping(path = '/article/byUserId/{userId}')
    void removeAllUserArticles(@PathVariable String userId) {
        log.info "Receive request to remove all articles of user with id=${userId} from database"

        articlesStorageService.deleteByUserId(userId)
    }
}

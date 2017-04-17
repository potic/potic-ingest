package com.github.pocketsquare.articles

import com.github.pocketsquare.articles.service.IngestJobKeeperService
import groovy.json.JsonSlurper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {

    @Bean
    JsonSlurper jsonSlurper() {
        new JsonSlurper()
    }

    static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Application, args)
        applicationContext.getBean(IngestJobKeeperService).startKeeper()
    }
}

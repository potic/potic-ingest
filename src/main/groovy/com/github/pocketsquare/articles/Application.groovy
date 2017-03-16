package com.github.pocketsquare.articles

import groovy.json.JsonSlurper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {

    @Bean
    JsonSlurper jsonSlurper() {
        new JsonSlurper()
    }

    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }
}

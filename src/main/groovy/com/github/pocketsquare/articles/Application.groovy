package com.github.pocketsquare.articles

import groovyx.net.http.HttpBuilder
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {

    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }

    @Bean
    HttpBuilder telegram() {
        HttpBuilder.configure {
            request.uri = 'http://pocket_square_ingest:5000/'
        }
    }
}

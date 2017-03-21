package com.github.pocketsquare.articles.domain

import groovy.transform.builder.Builder
import org.springframework.data.annotation.Id

@Builder
class Image {

    @Id
    String id

    String pocketId

    String src
    String credit
    String caption
    Integer height
    Integer width
}

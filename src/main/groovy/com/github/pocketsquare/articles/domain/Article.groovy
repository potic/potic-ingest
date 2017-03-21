package com.github.pocketsquare.articles.domain

import groovy.transform.builder.Builder
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed

@Builder
class Article {

    @Id
    String id

    @Indexed
    String pocketId

    @Indexed
    String userId

    String givenUrl
    String resolvedUrl
    String title
    boolean read
    boolean favorite
    Integer wordCount
    Collection<String> tags
    Collection<String> authors

    String source

    Image mainImage
    String excerpt

    Long timeAdded
    Long timeUpdated
    Long timeFavored
    Long timeRead

    String content
}

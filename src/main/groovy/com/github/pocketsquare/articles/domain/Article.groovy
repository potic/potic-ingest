package com.github.pocketsquare.articles.domain

import groovy.transform.builder.Builder
import org.springframework.data.annotation.Id

@Builder
class Article {

    @Id
    String id

    String userId

    String givenUrl
    String resolvedUrl
    String title
    boolean read
    boolean favorite
    Integer wordCount
    Integer order
    Collection<String> tags
    Collection<String> authors

    String content
}

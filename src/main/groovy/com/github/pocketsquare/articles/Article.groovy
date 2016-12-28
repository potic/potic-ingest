package com.github.pocketsquare.articles

import groovy.transform.ToString
import groovy.transform.builder.Builder

@Builder
@ToString(includeNames = true)
class Article {

    String url
    String title
    boolean read
    boolean favorite
    Integer wordCount
    Integer order
    Collection<String> tags
    Collection<String> authors
}

package com.github.pocketsquare.articles.service

import spock.lang.Specification
import spock.lang.Unroll

class ArticlesIngestServiceTest extends Specification {

    @Unroll
    def 'String extractSource(json) - when urls of article is [#given_url] and [#resolved_url], source should be [#expectedSource]'() {
        setup:
        ArticlesIngestService articlesIngestService = new ArticlesIngestService()

        Expando json = new Expando()
        json.given_url = given_url
        json.resolved_url = resolved_url

        when:
        def actualSource = articlesIngestService.extractSource(json)

        then:
        actualSource == expectedSource

        where:
        given_url                            | resolved_url || expectedSource
        null                                 | null         || null
        'given'                              | null         || 'given'
        null                                 | 'resolved'   || 'resolved'
        'given'                              | 'resolved'   || 'given'
        'source/path/to/article'             | null         || 'source'
        'http://m.source/path/to/article'    | null         || 'source'
        'https://www.source/path/to/article' | null         || 'source'
        'm.source/path/to/article'           | null         || 'source'
        'www.source'                         | null         || 'source'
    }
}

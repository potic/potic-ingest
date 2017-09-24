package me.potic.ingest.service

import spock.lang.Specification
import spock.lang.Unroll

class PocketApiServiceTest extends Specification {

    @Unroll
    def 'String extractSource(json) - when urls of article is [#resolved_url] and [#given_url], source should be [#expectedSource]'() {
        setup:
        PocketApiService pocketApiService = new PocketApiService()

        Expando json = new Expando()
        json.resolved_url = resolved_url
        json.given_url = given_url

        when:
        def actualSource = pocketApiService.extractSource(json)

        then:
        actualSource == expectedSource

        where:
        resolved_url                         | given_url    || expectedSource
        null                                 | null         || null
        'resolved'                           | null         || 'resolved'
        null                                 | 'given'      || 'given'
        'resolved'                           | 'given'      || 'resolved'
        'source/path/to/article'             | null         || 'source'
        'http://m.source/path/to/article'    | null         || 'source'
        'https://www.source/path/to/article' | null         || 'source'
        'm.source/path/to/article'           | null         || 'source'
        'www.source'                         | null         || 'source'
    }
}

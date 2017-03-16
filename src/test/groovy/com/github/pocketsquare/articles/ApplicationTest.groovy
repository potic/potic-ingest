package com.github.pocketsquare.articles

import io.github.yermilov.kerivnyk.service.KerivnykService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification

@ContextConfiguration(classes = Application)
class ApplicationTest extends Specification {

    @Autowired
    KerivnykService kerivnykService

    @Ignore('need to set up test mongodb instance')
    'kerivnyk.executorQualifier is configured as expected'() {
        expect:
        kerivnykService.executorQualifier == 'pocket-square-articles'
    }
}

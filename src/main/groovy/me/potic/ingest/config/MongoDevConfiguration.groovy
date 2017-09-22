package me.potic.ingest.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource

@Configuration
@Profile('dev')
@PropertySource(value = 'classpath:mongodb-dev.properties')
class MongoDevConfiguration {
}

package me.potic.ingest

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Reporter
import com.codahale.metrics.Slf4jReporter
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

import java.util.concurrent.TimeUnit

@EnableMetrics(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication
class Application {

    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }

    @Bean
    Reporter slf4jMetricsReporter(MetricRegistry metricRegistry) {
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger('me.potic.ingest.metrics'))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(1, TimeUnit.HOURS)

        return reporter
    }
}

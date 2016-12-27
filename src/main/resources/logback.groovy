import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'
    }
}

appender('FILE', RollingFileAppender) {
    file = '/logs/pocket-square-articles.log'

    encoder(PatternLayoutEncoder) {
        pattern = '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'
    }

    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/logs/pocket-square-articles.%d{yyyy-MM-dd}.log"
    }
}

logger('com.github.pocketsquare', INFO, [ 'STDOUT', 'FILE' ])

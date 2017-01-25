import ch.qos.logback.classic.Level
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

String POCKET_SQUARE_LOG_LEVEL = System.getenv('POCKET_SQUARE_LOG_LEVEL') ?: 'INFO'
String ROOT_LOG_LEVEL = System.getenv('ROOT_LOG_LEVEL') ?: 'INFO'

root(Level.toLevel(ROOT_LOG_LEVEL), ['STDOUT', 'FILE' ])
logger('com.github.pocketsquare', Level.toLevel(POCKET_SQUARE_LOG_LEVEL), [ 'STDOUT', 'FILE' ], false)

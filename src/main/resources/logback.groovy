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
    file = "${System.getenv('LOG_PATH') ?: 'logs'}/potic-ingest.log"

    encoder(PatternLayoutEncoder) {
        pattern = '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'
    }

    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "${System.getenv('LOG_PATH') ?: 'logs'}/potic-ingest.%d{yyyy-MM-dd}.log"
    }
}

String SERVICE_LOG_LEVEL = System.getenv('SERVICE_LOG_LEVEL') ?: (System.getenv('ENVIRONMENT_NAME') == 'prod' ? 'INFO' : 'DEBUG')
String ROOT_LOG_LEVEL = System.getenv('ROOT_LOG_LEVEL') ?: 'INFO'

root(Level.toLevel(ROOT_LOG_LEVEL), ['STDOUT', 'FILE' ])
logger('me.potic.ingest', Level.toLevel(SERVICE_LOG_LEVEL), [ 'STDOUT', 'FILE' ], false)

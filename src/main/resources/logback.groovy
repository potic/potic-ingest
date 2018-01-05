import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.hook.DelayingShutdownHook
import io.logz.logback.LogzioLogbackAppender

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

    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        FileNamePattern = "${System.getenv('LOG_PATH') ?: 'logs'}/potic-ingest.%d{yyyy-MM-dd}.%i.log"
        MaxHistory = 30
        MaxFileSize = "256MB"
        TotalSizeCap = "4GB"
    }
}

appender('LOGZIO', LogzioLogbackAppender) {
    token = System.getenv('LOGZIO_TOKEN') ?: new File('src/main/resources/logzio-dev.properties').text
    logzioUrl = 'https://listener.logz.io:8071'

    additionalFields="service=potic-ingest;env=${System.getenv('ENVIRONMENT_NAME') ?: 'dev'}"
}

def shutdownHook() {
    def shutdownHook = new DelayingShutdownHook()
    shutdownHook.setContext(context)

    Thread hookThread = new Thread(shutdownHook, "Logback shutdown hook [${context.name}]")
    context.putObject('SHUTDOWN_HOOK', hookThread)
    Runtime.getRuntime().addShutdownHook(hookThread)
}
shutdownHook()

String SERVICE_LOG_LEVEL = System.getenv('SERVICE_LOG_LEVEL') ?: (System.getenv('ENVIRONMENT_NAME') == 'prod' ? 'INFO' : 'DEBUG')
String ROOT_LOG_LEVEL = System.getenv('ROOT_LOG_LEVEL') ?: 'WARN'

root(toLevel(ROOT_LOG_LEVEL), ['STDOUT', 'FILE', 'LOGZIO' ])
logger('me.potic', toLevel(SERVICE_LOG_LEVEL), [ 'STDOUT', 'FILE', 'LOGZIO' ], false)

package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class UserService {

    HttpBuilder usersServiceRest

    @Autowired
    HttpBuilder usersServiceRest(@Value('${services.users.url}') String usersServiceUrl) {
        usersServiceRest = HttpBuilder.configure {
            request.uri = usersServiceUrl
        }
    }

    @Timed(name = 'allUsersIds')
    Collection<String> allUsersIds() {
        log.info 'fetching all users ids'

        try {
            return usersServiceRest.get {
                request.uri.path = '/users/ids'
            }
        } catch (e) {
            log.error "fetching all users ids failed: $e.message", e
            throw new RuntimeException('fetching all users ids failed', e)
        }
    }
}

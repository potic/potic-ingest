package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.ingest.domain.User
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

    @Timed(name = 'allUsers')
    Collection<User> allUsers() {
        log.info 'fetching all users'

        try {
            Collection response = usersServiceRest.get {
                request.uri.path = '/user'
            }
            return response.collect { new User(it) }
        } catch (e) {
            log.error "fetching all users failed: $e.message", e
            throw new RuntimeException('fetching all users failed', e)
        }
    }
}

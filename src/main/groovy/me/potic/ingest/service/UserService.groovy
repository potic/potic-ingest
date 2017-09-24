package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class UserService {

    HttpBuilder userServiceRest

    @Autowired
    JsonSlurper jsonSlurper

    @Autowired
    HttpBuilder userServiceRest(@Value('${services.userService.url}') String userServiceUrl) {
        userServiceRest = HttpBuilder.configure {
            request.uri = userServiceUrl
        }
    }

    @Timed(name = 'allUsersIds')
    Collection<String> allUsersIds() {
        byte[] response = userServiceRest.get {
            request.uri.path = '/user'
        }

        // temporary fix as response is byte array for some reason
        def jsonResponse = jsonSlurper.parse(response)

        jsonResponse['_embedded']['user'].collect { def user ->
            String userHref = user['_links']['self']['href']
            return userHref.substring(userHref.lastIndexOf('/') + 1)
        }
    }
}

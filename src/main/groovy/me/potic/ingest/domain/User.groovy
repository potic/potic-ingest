package me.potic.ingest.domain

import org.springframework.data.annotation.Id

class User {

    @Id
    String id

    Collection<String> socialIds

    String pocketAccessToken
}

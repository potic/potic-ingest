package me.potic.ingest.config

import com.mongodb.Mongo
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

@Configuration
class MongoConfiguration extends AbstractMongoConfiguration {

    @Value(value = '${mongodb.databaseName}')
    String databaseName

    @Value(value = '${mongodb.authentication.databaseName}')
    String authenticationDatabaseName

    @Value(value = '${mongodb.host}')
    String host

    @Value(value = '${mongodb.port}')
    Integer port

    @Value(value = '${mongodb.username}')
    String username

    @Value(value = '${mongodb.password:}')
    String password

    @Override
    protected String getDatabaseName() {
        databaseName
    }

    @Override
    Mongo mongo() throws Exception {
        new MongoClient( new ServerAddress(host, port), [MongoCredential.createCredential(username, authenticationDatabaseName, password())] )
    }

    char[] password() {
        if (System.getenv('MONGO_PASSWORD') != null) {
            System.getenv('MONGO_PASSWORD').toCharArray()
        } else {
            password.toCharArray()
        }
    }
}

package me.potic.ingest.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.ingest.domain.Article
import me.potic.ingest.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Slf4j
class IngestService {

    @Value(value = '${ingest.request.size}')
    long ingestRequestSize

    @Autowired
    UserService userService

    @Autowired
    PocketApiService pocketApiService

    @Autowired
    ArticlesService articlesService

    Map<String, Long> userArticlesIngestedTill = [:]

    @Scheduled(fixedDelay = 30_000L)
    @Timed(name = 'ingestNewArticles')
    void ingestNewArticles() {
        log.info("ingesting new articles...")

        userService.allUsers().forEach { User user ->
            long ingestSince = userArticlesIngestedTill.getOrDefault(user.id, 0)
            Collection<Article> ingestedArticles = pocketApiService.getArticlesByUser(user, ingestRequestSize, ingestSince)

            articlesService.upsertArticles(ingestedArticles)
            articlesService.downloadContent(ingestedArticles)

            if (!ingestedArticles.empty) {
                long ingestedTill = ingestedArticles.collect({ Article article -> article.timeUpdated }).max()
                userArticlesIngestedTill.put(user.id, ingestedTill)
            }
        }
    }
}

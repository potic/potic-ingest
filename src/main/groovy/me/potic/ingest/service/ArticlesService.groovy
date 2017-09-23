package me.potic.ingest.service

import com.codahale.metrics.Counter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.ingest.domain.Article
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

import static com.codahale.metrics.MetricRegistry.name
import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query

@Service
@Slf4j
class ArticlesService {

    @Autowired
    MongoTemplate mongoTemplate

    Counter downloadContentSuccessCount
    Counter downloadContentFailedCount

    @Autowired
    void initMetrics(MetricRegistry metricRegistry) {
        downloadContentSuccessCount = metricRegistry.counter(name(ArticlesService, 'downloadContent', 'count', 'success'))
        downloadContentFailedCount = metricRegistry.counter(name(ArticlesService, 'downloadContent', 'count', 'failed'))
    }

    @Timed(name = 'upsertArticles')
    void upsertArticles(Collection<Article> articles) {
        log.info "upserting ${articles.size()} articles..."

        articles.each { Article article ->
            def alreadyIngestedArticle = mongoTemplate.find(query(where('userId').is(article.userId).and('pocketId').is(article.pocketId)), Article)

            if (!alreadyIngestedArticle.empty) {
                article.id = alreadyIngestedArticle.first().id
                article.content = alreadyIngestedArticle.first().content
            }

            mongoTemplate.save(article)
        }
    }

    @Timed(name = 'downloadContent')
    void downloadContent(Collection<Article> articles) {
        Collection<Article> articlesWithoutContent = articles.findAll({ Article article ->
            article.content == null && (article.givenUrl != null || article.resolvedUrl != null)
        })

        log.info "downloading content of ${articlesWithoutContent.size()} articles..."
        articlesWithoutContent.each { Article article ->
            try {
                log.info "trying to download ${article.givenUrl}..."
                article.content = Jsoup.connect(article.givenUrl).get().html()
            } catch (e) {
                log.warn "failed to download ${article.givenUrl}", e
            }

            if (article.content == null && article.resolvedUrl != article.givenUrl) {
                try {
                    log.info "trying to download ${article.resolvedUrl}..."
                    article.content = Jsoup.connect(article.resolvedUrl).get().html()
                } catch (e) {
                    log.warn "failed to download ${article.resolvedUrl}", e
                }
            }

            if (article.content != null) {
                mongoTemplate.save(article)
                downloadContentSuccessCount.inc()
            } else {
                downloadContentFailedCount.inc()
            }
        }
    }
}

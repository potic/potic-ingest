package me.potic.ingest.service

import me.potic.ingest.domain.Article
import me.potic.ingest.domain.Image
import me.potic.ingest.repository.ArticleRepository
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import io.github.yermilov.kerivnyk.domain.Job
import io.github.yermilov.kerivnyk.service.DurableJob
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class ArticlesIngestService {

    static final Integer DEFAULT_REQUEST_SIZE = 100
    static final Integer INGEST_REQUEST_SIZE = Integer.parseInt(System.getenv('ARTICLES_INGEST_REQUEST_SIZE') ?: DEFAULT_REQUEST_SIZE.toString())

    static final String DEFAULT_SUSPEND_DURATION = '30sec'
    static final String SUSPEND_DURATION = System.getenv('ARTICLES_INGEST_SUSPEND_DURATION') ?: DEFAULT_SUSPEND_DURATION

    @Autowired
    ArticleRepository articleRepository

    @Autowired
    JsonSlurper jsonSlurper

    DurableJob ingestArticlesByUserIdJob(String userId) {
        new DurableJob("ingest articles for user-id=${userId}") {

            HttpBuilder ingestService

            @Override
            boolean canStart(boolean isNew, Collection<Job> concurrentJobs) {
                concurrentJobs.find({ Job job -> job.name == this.name }) == null
            }

            @Override
            void init() {
                ingestService = HttpBuilder.configure {
                    request.uri = INGEST_SERVICE_URL
                }

                storage.userId = userId
                storage.requestSince = storage.requestSince != null ? storage.requestSince : 0

                storage['total user articles count'] = articleRepository.countByUserId(userId)
                storage['total articles metadata ingested'] = storage['total articles metadata ingested'] ?: 0
                storage['total articles metadata updated'] = storage['total articles metadata updated'] ?: 0
                storage['total articles content loaded'] = storage['total articles content loaded'] ?: 0
                storage['total articles content load failed'] = storage['total articles content load failed'] ?: 0
            }

            @Override
            void act() {
                try {
                    log.info "requesting ${INGEST_REQUEST_SIZE} articles for user with id=${userId} since ${storage.requestSince}"

                    def response = ingestService.get {
                        request.uri.path = "/fetch/${userId}"
                        request.uri.query = [ count: INGEST_REQUEST_SIZE, since: storage.requestSince ]
                    }

                    def jsonResponse = toJson(response)

                    Collection<Article> articles = jsonResponse.values().collect({
                        Article alreadyIngestedArticle = articleRepository.findOneByUserIdAndPocketId(userId, it.resolved_id)

                        Article.builder()
                                .id(alreadyIngestedArticle?.id)
                                .userId(userId)
                                .pocketId(it.resolved_id)
                                .givenUrl(it.given_url)
                                .resolvedUrl(it.resolved_url)
                                .title(it.resolved_title)
                                .read(it.status == '1')
                                .favorite(it.favorite == '1')
                                .wordCount(it.word_count != null ? Integer.parseInt(it.word_count) : 0)
                                .tags(it.tags?.keySet() ?: [])
                                .authors(it.authors?.values()?.collect({ it.name }) ?: [])
                                .source(extractSource(it))
                                .mainImage(extractMainImage(it.images))
                                .excerpt(it.excerpt)
                                .timeAdded(it.time_added != null ? Long.parseLong(it.time_added) : 0)
                                .timeFavored(it.time_favorited != null ? Long.parseLong(it.time_favorited) : 0)
                                .timeRead(it.time_read != null ? Long.parseLong(it.time_read) : 0)
                                .timeUpdated(it.time_updated != null ? Long.parseLong(it.time_updated) : 0)
                                .content(alreadyIngestedArticle?.content)
                                .build()
                    })
                    log.info "received ${articles.size()} articles for user with id=${userId}"
                    storage['total articles metadata ingested'] += articles.size()

                    Collection<Article> existingArticles = articles.findAll({ Article article -> article.id != null })
                    log.info "updating metadata of ${existingArticles.size()} existing articles for user with id=${userId}"
                    articleRepository.save existingArticles
                    storage['total articles metadata updated'] += existingArticles.size()

                    Collection<Article> articlesWithoutContent = articles.findAll({ Article article ->
                        article.content == null && (article.givenUrl != null || article.resolvedUrl != null)
                    })
                    log.info "ingesting content of ${articlesWithoutContent.size()} articles for user with id=${userId}"
                    articlesWithoutContent.each { Article article ->
                        try {
                            log.info "trying to load ${article.givenUrl} for user with id=${userId}"
                            article.content = Jsoup.connect(article.givenUrl).get().html()
                            storage['total articles content loaded']++
                        } catch (e) {
                            log.warn "failed to load ${article.givenUrl} for user with id=${userId}"
                            storage['total articles content load failed']++
                        }
                        if (article.content == null && article.resolvedUrl != article.givenUrl) {
                            try {
                                log.info "trying to load ${article.resolvedUrl} for user with id=${userId}"
                                article.content = Jsoup.connect(article.resolvedUrl).get().html()
                                storage['total articles content loaded']++
                            } catch (e) {
                                log.warn "failed to load ${article.resolvedUrl} for user with id=${userId}"
                                storage['total articles content load failed']++
                            }
                        }
                        articleRepository.save article
                    }

                    if (!articles.empty) {
                        storage.requestSince = articles.collectMany { Article article ->
                            [ article.timeAdded, article.timeFavored, article.timeRead, article.timeUpdated ]
                        }.max()
                    }
                } catch (e) {
                    log.warn("failed during ingesting articles for user with id=${userId} since ${storage.requestSince} because of ${e.class.name}: ${e.message}", e)
                } finally {
                    storage['total user articles count'] = articleRepository.countByUserId(userId)
                    suspend(SUSPEND_DURATION)
                }
            }

            def toJson(def response) {
                // temporary fix as response is jsoup document for some reason
                String responseAsString = (response as Document).body().html()

                // temporary fix as these substrings break json parsing
                responseAsString = responseAsString.replace("<script src=\"\\&quot;https://d3js.org/d3.v4.js\\&quot;\"></script>", "")
                responseAsString = responseAsString.replace("<script src=\"\\&quot;https://unpkg.com/d3-horizon-chart\\&quot;\"></script>", "")

                return jsonSlurper.parseText(responseAsString)
            }

            Image extractMainImage(json) {
                if (json == null || json.empty) {
                    null
                } else {
                    def mainImageJson = json.values().min({ Integer.parseInt(it.image_id) })

                    Image.builder()
                        .pocketId("${mainImageJson.item_id}-${mainImageJson.image_id}")
                        .src(mainImageJson.src)
                        .width(Integer.parseInt(mainImageJson.width))
                        .height(Integer.parseInt(mainImageJson.height))
                        .credit(mainImageJson.credit)
                        .caption(mainImageJson.caption)
                        .build()
                }
            }
        }
    }

    String extractSource(json) {
        String url = json.resolved_url ?: json.given_url
        if (url == null) {
            return null
        }

        int startIndex = 0
        int endIndex

        if (url.startsWith('http://')) {
            startIndex = 'http://'.length()
        }

        if (url.startsWith('https://')) {
            startIndex = 'https://'.length()
        }

        if (url.substring(startIndex).startsWith('www.')) {
            startIndex += 'www.'.length()
        }

        if (url.substring(startIndex).startsWith('m.')) {
            startIndex += 'm.'.length()
        }

        endIndex = url.indexOf('/', startIndex)
        if (endIndex < 0) {
            endIndex = url.length()
        }

        return url.substring(startIndex, endIndex)
    }
}

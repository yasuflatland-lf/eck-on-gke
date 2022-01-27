package design.studio.content.search.usecase

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.indices.Alias
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse
import design.studio.content.search.service.elasticsearch.ElasticsearchSearchEngineService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * @author Yasuyuki Takeo
 */
@Service
class CMSArticleHandler {
    companion object {
        val log: Logger = LoggerFactory.getLogger(CMSArticleHandler::class.java)
    }

    @Autowired
    private lateinit var elasticsearchSearchEngineService: ElasticsearchSearchEngineService


    // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/api-conventions.html
    fun initialize() {
        var client: ElasticsearchAsyncClient = elasticsearchSearchEngineService.getClient()
        val createResponse: CompletableFuture<CreateIndexResponse>? = client.indices()
            .create { c: CreateIndexRequest.Builder ->
                c
                    .index("my-index")
                    .aliases(
                        "foo"
                    ) { a: Alias.Builder ->
                        a
                            .isWriteIndex(true)
                    }
            }
    }
}
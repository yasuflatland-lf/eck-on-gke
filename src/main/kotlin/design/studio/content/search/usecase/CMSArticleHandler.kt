package design.studio.content.search.usecase

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.IndexResponse
import co.elastic.clients.elasticsearch.indices.*
import design.studio.content.search.model.CMSArticle
import design.studio.content.search.service.elasticsearch.ElasticsearchSearchEngineService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

/**
 * @author Yasuyuki Takeo
 */
@Service
class CMSArticleHandler(
    private var elasticsearchSearchEngineService: ElasticsearchSearchEngineService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(CMSArticleHandler::class.java)
    }

    fun getClient(): ElasticsearchAsyncClient {
        return elasticsearchSearchEngineService.getClient()
    }

    fun deleteIndices(indexName: String): Mono<DeleteIndexResponse> {
        return Mono.fromFuture(getClient().indices().delete{
            d: DeleteIndexRequest.Builder -> d.index(indexName)
        })
    }

    // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/api-conventions.html
    fun initialize(indexName: String, alias: String): Mono<CreateIndexResponse> {
        // TODO: What's the better way to handle elasticsearch client? Passing as a parameter or holding value as a class valuable?
        return Mono.fromFuture(getClient().indices()
            .create { c: CreateIndexRequest.Builder ->
                c
                    .index(indexName)
                    .aliases(
                        alias
                    ) { a: Alias.Builder ->
                        a
                            .isWriteIndex(true)
                    }
                    .mappings { t: TypeMapping.Builder ->
                        t.dynamicTemplates(
                            mutableListOf()
                        )
                    }
            })
    }

    fun createIndex(indexName: String, id: String, entity: CMSArticle): Mono<IndexResponse> {
        return Mono.fromFuture(
            getClient().index { b: IndexRequest.Builder<Any?> ->
                b
                    .index(indexName)
                    .id(id) // test with url-unsafe string
                    .document(entity)
                    .refresh(Refresh.True) // Make it visible for search
            }
        )
    }

}
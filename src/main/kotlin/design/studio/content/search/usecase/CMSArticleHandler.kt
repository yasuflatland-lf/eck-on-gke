package design.studio.content.search.usecase

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.IndexResponse
import co.elastic.clients.elasticsearch.indices.*
import design.studio.content.search.service.elasticsearch.ElasticsearchSearchEngineService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

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

    fun createIndex(indexName: String, id: String, entity: Any): Mono<IndexResponse> {
        return Mono.fromFuture(
            getClient().index { b: IndexRequest.Builder<Any?> ->
                b
                    .index(indexName)
                    .id(id)
                    .document(entity)
                    .refresh(Refresh.True)
            }
        )
    }

    fun deleteIndices(indexName: String): Mono<DeleteIndexResponse> {
        return Mono.fromFuture(getClient().indices().delete { d: DeleteIndexRequest.Builder ->
            d.index(indexName)
        })
    }
}


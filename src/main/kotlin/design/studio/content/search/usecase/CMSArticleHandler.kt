package design.studio.content.search.usecase

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.IndexResponse
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse
import design.studio.content.search.model.CMSArticle
import design.studio.content.search.service.elasticsearch.ElasticsearchSearchEngineService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * @author Yasuyuki Takeo
 */
@Service
class CMSArticleHandler(
    var elasticsearchSearchEngineService: ElasticsearchSearchEngineService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(CMSArticleHandler::class.java)
    }

    fun getClient(): ElasticsearchAsyncClient {
        return elasticsearchSearchEngineService.getClient()
    }

    suspend fun createIndex(indexName: String, id: String, entity: Any): IndexResponse = coroutineScope {
        return@coroutineScope runCatching {
            async {
                getClient().index { b: IndexRequest.Builder<Any?> ->
                    b
                        .index(indexName)
                        .id(id)
                        .document(entity)
                        .refresh(Refresh.True)
                }
            }
        }.fold(
            onSuccess = {
                ElasticsearchSearchEngineService.log.info("${indexName} is created")
                return@fold it.await().get()
            },
            onFailure = {
                ElasticsearchSearchEngineService.log.error("Failed to create ${indexName}. \n\n" + it.stackTraceToString())
                throw it
            }
        )
    }

    suspend fun deleteIndices(indexName: String): DeleteIndexResponse = coroutineScope {
        return@coroutineScope runCatching {
            async {
                getClient().indices().delete { d: DeleteIndexRequest.Builder ->
                    d.index(indexName)
                }
            }
        }.fold(
            onSuccess = {
                ElasticsearchSearchEngineService.log.info("${indexName} is deleteed")
                return@fold it.await().get()
            },
            onFailure = {
                ElasticsearchSearchEngineService.log.error("Failed to delete ${indexName}. \n\n" + it.stackTraceToString())
                throw it
            }
        )
    }

    private suspend fun performFulltextSearch(
        indexName: String,
        requestedQuery: String,
        length: Int = 20,
        startFrom: Int = 0
    ): CompletableFuture<SearchResponse<CMSArticle>>? {

        val request = SearchRequest.of { s ->
            s.index(indexName)
            s.size(length)
            s.from(startFrom)
            s.query { query ->
                query.bool { bool ->
                    bool.must(
                        listOf(Query.of { query ->
                            query.multiMatch { mm ->
                                mm.query(requestedQuery)
                                mm.fields(
                                    "title_ja.ngram",
                                    "content_ja.ngram",
                                )
                            }
                        })
                    )
                    bool.should(
                        listOf(Query.of { query ->
                            query.multiMatch { mm ->
                                mm.query(requestedQuery)
                                mm.fields(
                                    "title_ja",
                                    "content_ja",
                                )
                            }
                        })
                    )
                }
            }
        }
        return getClient().search(request, CMSArticle::class.java)
    }

    suspend fun search(indexName: String, query: String, pagination: Int = 0, length: Int = 100): List<CMSArticle?> =
        coroutineScope {
            var result = async {
                performFulltextSearch(indexName, query, length, pagination * length)
            }

            return@coroutineScope result.await()?.get()?.hits()?.hits()?.map { it.source() } ?: emptyList<CMSArticle>()
        }
}


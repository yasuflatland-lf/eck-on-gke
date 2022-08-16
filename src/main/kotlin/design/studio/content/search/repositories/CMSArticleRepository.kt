package design.studio.content.search.repositories

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.*
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse
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
class CMSArticleRepository(var elasticsearchSearchEngineService: ElasticsearchSearchEngineService) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(CMSArticleRepository::class.java)
    }

    fun getClient(): ElasticsearchAsyncClient {
        return elasticsearchSearchEngineService.getClient()
    }

    suspend fun initialize(indexName: String, alias: String): CreateIndexResponse = coroutineScope {
        return@coroutineScope elasticsearchSearchEngineService.initialize(indexName, alias)
    }

    suspend fun updateBulkIndices(indexName: String, id: String, entities: List<CMSArticle>, upsert: Boolean? = false): BulkResponse = coroutineScope {
        return@coroutineScope runCatching {
            async {
                getClient().bulk { br: BulkRequest.Builder ->
                    entities.map { entity ->
                        br.operations { op ->
                            op
                                .update<CMSArticle, CMSArticle> { co ->
                                    co
                                        .index(indexName)
                                        .id(id).action { a ->
                                            a.docAsUpsert(upsert).doc(entity)
                                        }
                                }
                        }
                    }
                    return@bulk br
                }
            }
        }.fold(onSuccess = {
            log.info("$indexName is bulk updated")
            return@fold it.await().get()
        }, onFailure = {
            log.error("Failed to bulk update ${indexName}. \n\n" + it.stackTraceToString())
            throw it
        })
    }

    suspend fun createBulkIndices(indexName: String, entities: List<CMSArticle>): BulkResponse = coroutineScope {
        return@coroutineScope runCatching {
            async {
                getClient().bulk { br: BulkRequest.Builder ->
                    entities.map { entity ->
                        br.operations { op ->
                            op.create<CMSArticle> { co ->
                                co.index(indexName).id(null).document(entity)
                            }
                        }
                    }
                    return@bulk br
                }
            }
        }.fold(onSuccess = {
            log.info("$indexName is bulk created")
            return@fold it.await().get()
        }, onFailure = {
            log.error("Failed to bulk create ${indexName}. \n\n" + it.stackTraceToString())
            throw it
        })
    }


    suspend fun createIndex(indexName: String, id: String, entity: Any): IndexResponse = coroutineScope {
        return@coroutineScope runCatching {
            async {
                getClient().index { b: IndexRequest.Builder<Any?> ->
                    b.index(indexName).id(id).document(entity).refresh(Refresh.True)
                }
            }
        }.fold(onSuccess = {
            ElasticsearchSearchEngineService.log.info("$indexName is created")
            return@fold it.await().get()
        }, onFailure = {
            ElasticsearchSearchEngineService.log.error("Failed to create ${indexName}. \n\n" + it.stackTraceToString())
            throw it
        })
    }

    suspend fun deleteIndex(indexName: String): DeleteIndexResponse = coroutineScope {
        return@coroutineScope runCatching {
            async {
                getClient().indices().delete { d: DeleteIndexRequest.Builder ->
                    d.index(indexName)
                }
            }
        }.fold(onSuccess = {
            ElasticsearchSearchEngineService.log.info("$indexName is deleteed")
            return@fold it.await().get()
        }, onFailure = {
            ElasticsearchSearchEngineService.log.error("Failed to delete ${indexName}. \n\n" + it.stackTraceToString())
            throw it
        })
    }

    private suspend fun performFulltextSearch(indexName: String, requestedQuery: String, length: Int = 20, startFrom: Int = 0): CompletableFuture<SearchResponse<CMSArticle>>? {

        val request = SearchRequest.of { s ->
            s.index(indexName)
            s.size(length)
            s.from(startFrom)
            s.query { query ->
                query.bool { bool ->
                    bool.must(listOf(Query.of { query ->
                        query.multiMatch { mm ->
                            mm.query(requestedQuery)
                            mm.fields(
                                "title_ja.ngram",
                                "content_ja.ngram",
                            )
                        }
                    }))
                    bool.should(listOf(Query.of { query ->
                        query.multiMatch { mm ->
                            mm.query(requestedQuery)
                            mm.fields(
                                "title_ja",
                                "content_ja",
                            )
                        }
                    }))
                }
            }
        }
        return getClient().search(request, CMSArticle::class.java)
    }

    suspend fun search(indexName: String, query: String, pagination: Int = 0, length: Int = 100): List<CMSArticle?> = coroutineScope {
        var result = async {
            performFulltextSearch(indexName, query, length, pagination * length)
        }

        return@coroutineScope result.await()?.get()?.hits()?.hits()?.map { it.source() } ?: emptyList<CMSArticle>()
    }
}

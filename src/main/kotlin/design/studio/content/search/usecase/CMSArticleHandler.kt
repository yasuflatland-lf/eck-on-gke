package design.studio.content.search.usecase

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.IndexResponse
import co.elastic.clients.elasticsearch.indices.*
import design.studio.content.search.service.elasticsearch.ElasticsearchSearchEngineService
import design.studio.content.search.service.elasticsearch.MappingFileReader
import design.studio.content.search.service.elasticsearch.connection.constants.MappingConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

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

    /**
     * Return Elasticsearch Settings
     */
    fun getElaticSettings(indexName: String): Pair<IndexSettings?, TypeMapping?> {
        log.info("Loading the index mapping.")
        var reader = MappingFileReader()
        var json: String = reader.getResource(MappingConstants.MAPPING_FILE_PATH)

        var typeMapping: TypeMapping? = reader.getTypeMappings(indexName, json)
        var indexSettings: IndexSettings? = reader.getIndexSettings(indexName, json)

        return Pair(indexSettings, typeMapping)
    }

    // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/api-conventions.html
    fun initialize(indexName: String, alias: String): Mono<CreateIndexResponse> {
        return Mono.defer {
            // Get Elasticsearch settings
            val (indexSettings, typeMapping) = getElaticSettings(indexName)

            // TODO: What's the better way to handle elasticsearch client? Passing as a parameter or holding value as a class valuable?
            return@defer Mono.fromFuture(getClient().indices()
                .create { c: CreateIndexRequest.Builder ->
                    c.index(indexName)
                        .aliases(
                            alias
                        ) { a: Alias.Builder ->
                            a.isWriteIndex(true)
                        }
                        .settings(indexSettings)
                        .mappings(typeMapping)
                })
        }
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


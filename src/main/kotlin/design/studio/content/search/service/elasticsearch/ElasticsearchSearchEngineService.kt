package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.indices.*
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchClientResolver
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchConnection
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchConnectionBuilder
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import design.studio.content.search.service.elasticsearch.connection.constants.MappingConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import javax.annotation.PreDestroy

/**
 * @author Yasuyuki Takeo
 */
@Service
class ElasticsearchSearchEngineService(config: ElasticConfig) : ElasticsearchClientResolver {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ElasticsearchSearchEngineService::class.java)
    }

    private lateinit var elasticsearchConnection: ElasticsearchConnection

    init {
        this.connect(config)
    }

    @PreDestroy
    fun close() {
        try {
            elasticsearchConnection.close()

        } catch (ioException: IOException) {
            throw RuntimeException(ioException)
        }
    }

    fun connect(config: ElasticConfig) {
        elasticsearchConnection = ElasticsearchConnectionBuilder.Builder()
            .connectionId(config.connectionId)
            .serverName(config.serverName)
            .port(config.port)
            .username(config.username)
            .password(config.password)
            .caPath(config.caPath)
            .active(true)
            .build()

        try {
            elasticsearchConnection.connect()
        } catch (runtimeException: RuntimeException) {
            if (config.connectionId == ConnectionConstants.SIDECAR_CONNECTION_ID
            ) {
                log.error(
                    "Elasticsearch sidecar could not be started",
                    runtimeException
                );
            }

            throw runtimeException
        }
    }

    override fun getClient(): ElasticsearchAsyncClient {
        return elasticsearchConnection.getAsyncClient()
    }

    suspend fun initialize(indexName: String, alias: String): CreateIndexResponse = withContext(Dispatchers.IO) {
        // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/api-conventions.htmll
        runCatching {
            async {
                log.info("Loading the index mapping.")
                var reader = JsonUtils()
                var json: String = reader.getResource(MappingConstants.MAPPING_FILE_PATH)

                var typeMapping: TypeMapping? = reader.getTypeMappings(indexName, json)
                var indexSettings: IndexSettings? = reader.getIndexSettings(indexName, json)

                getClient().indices()
                    .create { c: CreateIndexRequest.Builder ->
                        c.index(indexName)
                            .aliases(
                                alias
                            ) { a: Alias.Builder ->
                                a.isWriteIndex(true)
                            }
                            .settings(indexSettings)
                            .mappings(typeMapping)
                    }
            }
        }.fold(
            onSuccess = {
                log.info("Index settings and mapping is correctly applied.")
                return@fold it.await().get()
            },
            onFailure = {
                log.error("Failed to initialize ${indexName}. \n\n" + it.stackTraceToString())
                throw it
            }
        )
    }

    suspend fun deleteIndices(indexName: String): DeleteIndexResponse = withContext(Dispatchers.IO) {
        runCatching {
            async {
                getClient().indices().delete { d: DeleteIndexRequest.Builder ->
                    d.index(indexName)
                }
            }
        }.fold(
            onSuccess = {
                log.info("${indexName} is deleted")
                return@fold it.await().get()
            },
            onFailure = {
                log.error("Failed to delete ${indexName}. \n\n" + it.stackTraceToString())
                throw it
            }
        )
    }

    suspend fun getQueryFromJSON(queryJSON: String): Query = withContext(Dispatchers.IO) {
        runCatching {
            async {
                var reader = JsonUtils()
                var query = reader.fromJson(
                    queryJSON, Query._DESERIALIZER
                )
                return@async query
            }
        }.fold(
            onSuccess = {
                return@fold it.await()
            },
            onFailure = {
                throw it
            }
        )
    }

}

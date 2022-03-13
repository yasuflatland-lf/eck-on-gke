package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.indices.*
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchClientResolver
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchConnection
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchConnectionBuilder
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import design.studio.content.search.service.elasticsearch.connection.constants.MappingConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
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

    fun initialize(indexName: String, alias: String): Mono<CreateIndexResponse> {
        // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/api-conventions.htmll
        return getElaticSettings(indexName).toMono().flatMap { (indexSettings, typeMapping) ->
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
                }.toMono().doOnSuccess {
                    log.info("Index settings and mapping is correctly applied.")
                }.doOnError {
                    log.error("Failed to initialize ${indexName}. \n\n" + it.stackTraceToString())
                }.subscribeOn(Schedulers.boundedElastic())
        }
    }

    fun deleteIndices(indexName: String): Mono<DeleteIndexResponse> {
        return Mono.fromFuture(getClient().indices().delete { d: DeleteIndexRequest.Builder ->
            d.index(indexName)
        }).doOnSuccess {
            log.info("${indexName} is deleted")
        }.doOnError {
            log.error("Failed to delete ${indexName}. \n\n" + it.stackTraceToString())
        }
    }
}

package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchClientResolver
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchConnection
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchConnectionBuilder
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


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
        elasticsearchConnection = ElasticsearchConnectionBuilder.Builder()
            .connectionId(config.connectionId)
            .serverName(config.serverName)
            .port(config.port)
            .username(config.username)
            .password(config.password)
            .active(true)
            .build()

        try {
            elasticsearchConnection.connect()
        } catch (runtimeException: RuntimeException) {
            if (config.connectionId.equals(
                    ConnectionConstants.SIDECAR_CONNECTION_ID
                )
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
        return elasticsearchConnection.getClient()
    }

    /**
     * Create Indices by Project ID
     */
    fun createIndicesByProjectId(): String? {
        return ""
    }

    /**
     * Remove Indices by Project Id
     */
    fun removeIndicesByProjectId(): String? {
        return ""
    }

    /**
     * Backup Indices
     */
    fun backup(): String? {
        return ""
    }

    /**
     * Restore Indices
     */
    fun restore(): String? {
        return ""
    }


}

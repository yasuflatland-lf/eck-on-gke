package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchClientResolver
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author Yasuyuki Takeo
 */
@Service
class ElasticsearchSearchEngineService(private val config: ElasticConfig) {
//    companion object {
//        val log: Logger = LoggerFactory.getLogger(ElasticsearchSearchEngineService::class.java)
//    }
//
//    private lateinit var client: ElasticsearchClient
//
//    private lateinit var transport: ElasticsearchTransport
//
//    // Create the low-level client
//    private lateinit  var restClient: RestClient
//
//    init {
//        restClient = RestClient.builder(
//        HttpHost(config.serverName, config.port)
//        ).build()
//
//        // Create the transport with a Jackson mapper
//        transport = RestClientTransport(
//            restClient, JacksonJsonpMapper()
//        )
//
//        // And create the API client
//        client = ElasticsearchClient(transport)
//
//        client.security().putUser { req ->
//            req.username(config.username)
//            req.password(config.password)
//            req.roles("superuser")
//            req.enabled(true)
//        }
//    }
//
//    override fun getClient(): ElasticsearchClient {
//        return client
//    }

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

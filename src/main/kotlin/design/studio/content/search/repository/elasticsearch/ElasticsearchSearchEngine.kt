package design.studio.content.search.repository.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties(prefix = "app.elastic")
data class ElasticConfig(
    var serverName: String,
    var port: Int
)

/**
 * Elasticsearch Engine
 *
 * @author Yasuyuki Takeo
 */
class ElasticsearchSearchEngine(private val config: ElasticConfig) {

    private lateinit var client: ElasticsearchClient

    private lateinit var transport: ElasticsearchTransport

    // Create the low-level client
    private var restClient: RestClient = RestClient.builder(
    HttpHost(config.serverName, config.port)
    ).build()

    companion object {
        val log: Logger = LoggerFactory.getLogger(ElasticsearchSearchEngine::class.java)
    }

    init {
        // Create the transport with a Jackson mapper
        transport = RestClientTransport(
            restClient, JacksonJsonpMapper()
        )

        // And create the API client
        client = ElasticsearchClient(transport)
    }
}

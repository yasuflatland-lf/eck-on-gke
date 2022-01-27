package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import design.studio.content.search.service.elasticsearch.connection.ElasticsearchClientResolver
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


/**
 * @author Yasuyuki Takeo
 */
@Service
class ElasticsearchSearchEngineService(private val config: ElasticConfig) : ElasticsearchClientResolver {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ElasticsearchSearchEngineService::class.java)
    }

    private lateinit var client: ElasticsearchAsyncClient

    private lateinit var transport: ElasticsearchTransport

    // TODO : Extract into ElasticsearchConnectionBuilder
    init {
        var restClientBuilder: RestClientBuilder = RestClient.builder(
            HttpHost(config.serverName, config.port)
        )

        // Login with user if set in configs

        // Login with user if set in configs
        if (config.username != null) {
            val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                UsernamePasswordCredentials(config.username, config.password)
            )
            restClientBuilder.setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.disableAuthCaching()
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
        }
        // Create the transport with a Jackson mapper
        transport = RestClientTransport(
            restClientBuilder.build(), JacksonJsonpMapper()
        )

        // And create the Asynchronous API client
        // https://github.com/elastic/elasticsearch-java/blob/main/docs/api-conventions.asciidoc#blocking-and-asynchronous-clients
        // https://github.com/elastic/elasticsearch-java/blob/f7f03a8af78f174724d38e14ddbebcdd438955dc/java-client/src/main/java/co/elastic/clients/elasticsearch/ElasticsearchAsyncClient.java
        client = ElasticsearchAsyncClient(transport)

    }

    override fun getClient(): ElasticsearchAsyncClient {
        return client
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

package design.studio.content.search.service.elasticsearch.connection

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import design.studio.content.search.service.elasticsearch.ElasticsearchSearchEngineService
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * @author Yasuyuki Takeo
 */
data class ElasticsearchConnection(
    val _connectionId: String?,
    val _serverName: String?,
    val _port: Int?,
    val _username: String?,
    val _password: String?,
    var _active: Boolean?
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ElasticsearchConnection::class.java)
    }

    private lateinit var _client: ElasticsearchAsyncClient
    private lateinit var _transport: RestClientTransport

    /**
     * Shutdown the connection to the Elasticsearch Server
     */
    fun close() {
        if (_active == false) {
            log.warn("The connection has been closed.")
            return
        }

        try {
            _transport.close()
            _active = false
            log.info("_connectionId(${_connectionId}) is closed.")
        } catch (ioException: IOException) {
            throw RuntimeException(ioException)
        }
    }

    /**
     * Connect Elasticsearch Server
     */
    fun connect() {
        ElasticsearchSearchEngineService.log.info("Connecting to the Elasticsearch server")

        if (_active == false) {
            log.error("Connecting inactive connection")
        }

        // Get Client
        _client = createClient()
    }

    fun getClient(): ElasticsearchAsyncClient {
        return _client
    }

    private fun createClient(): ElasticsearchAsyncClient {

        var restClientBuilder: RestClientBuilder = RestClient.builder(
            _port?.let { HttpHost(_serverName, it) }
        )

        // Login with user if set in configs
        if (_username != null) {
            val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                UsernamePasswordCredentials(_username, _password)
            )
            restClientBuilder.setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.disableAuthCaching()
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
        }
        // Create the transport with a Jackson mapper
        _transport = RestClientTransport(
            restClientBuilder.build(), JacksonJsonpMapper()
        )

        // And create the Asynchronous API client
        // https://github.com/elastic/elasticsearch-java/blob/main/docs/api-conventions.asciidoc#blocking-and-asynchronous-clients
        // https://github.com/elastic/elasticsearch-java/blob/f7f03a8af78f174724d38e14ddbebcdd438955dc/java-client/src/main/java/co/elastic/clients/elasticsearch/ElasticsearchAsyncClient.java
        return ElasticsearchAsyncClient(_transport)
    }

}
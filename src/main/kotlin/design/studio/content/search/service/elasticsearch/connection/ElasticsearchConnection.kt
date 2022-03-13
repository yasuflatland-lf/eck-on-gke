package design.studio.content.search.service.elasticsearch.connection

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory


/**
 * @author Yasuyuki Takeo
 */
data class ElasticsearchConnection(
    val _connectionId: String?,
    val _serverName: String?,
    val _port: Int?,
    val _username: String?,
    val _password: String?,
    var _caPath: String?,
    var _active: Boolean?
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ElasticsearchConnection::class.java)
    }

    private lateinit var _asyncClient: ElasticsearchAsyncClient
    private lateinit var _transport: RestClientTransport

    /**
     * Shutdown the connection to the Elasticsearch Server
     */
    fun close() {
        log.info("_connectionId(${_connectionId}) is closing...")
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
        _asyncClient = createAsyncClient(_serverName!!, _username!!, _password!!, _port!!, _caPath!!)
    }

    fun getAsyncClient(): ElasticsearchAsyncClient {
        return _asyncClient
    }

    private fun createAsyncClient(
        serverName: String,
        username: String,
        password: String,
        port: Int,
        caPath: String
    ): ElasticsearchAsyncClient {

        log.info("Elasticsearch Async client start creating...")

        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(username, password))

        val ks = KeyStore.getInstance("pkcs12")
        ks.load(null, null)

        val fis = FileInputStream(caPath)
        val bis = BufferedInputStream(fis)

        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val cert = cf.generateCertificate(bis)
        ks.setCertificateEntry("ca", cert)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        val context = SSLContext.getInstance("TLS")
        context.init(null, tmf.trustManagers, null)

        val restClientBuilder = RestClient.builder(
            HttpHost(serverName, port, "https")
        ).setHttpClientConfigCallback { httpClientBuilder: HttpAsyncClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            httpClientBuilder.setSSLContext(context)
        }

        // Create the transport with a Jackson mapper
        if (restClientBuilder != null) {
            _transport = RestClientTransport(
                restClientBuilder.build(), JacksonJsonpMapper()
            )
        }

        log.info("Elasticsearch Async client created.")

        // And create the Asynchronous API client
        // https://github.com/elastic/elasticsearch-java/blob/main/docs/api-conventions.asciidoc#blocking-and-asynchronous-clients
        // https://github.com/elastic/elasticsearch-java/blob/f7f03a8af78f174724d38e14ddbebcdd438955dc/java-client/src/main/java/co/elastic/clients/elasticsearch/ElasticsearchAsyncClient.java
        return ElasticsearchAsyncClient(_transport)
    }

}
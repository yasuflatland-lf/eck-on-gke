package design.studio.content.search.routers

import design.studio.content.search.repositories.CMSArticleRepository
import design.studio.content.search.service.AbstractContainerBaseTest
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import io.kotest.core.annotation.AutoScan
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers

@AutoScan
@Testcontainers
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractContainerBaseTest.Initializer::class])
@ExperimentalCoroutinesApi
class CMSArticleRouterTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var cmsArticleRepository: CMSArticleRepository

    @LocalServerPort
    private var port: Int = 0

    init {
        afterEach {
            runTest {
                cmsArticleRepository.deleteIndex(ConnectionConstants.DEFAULT_INDEX)
            }
        }

        test("Initialize Elasticsearch Test") {
            WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/articles/search/init")
                        .queryParam("indexName", ConnectionConstants.DEFAULT_INDEX)
                        .queryParam("alias", ConnectionConstants.REMOTE_CONNECTION_ID)
                        .build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is2xxSuccessful
                .expectBody()
                .json("{\"index\":\"studio-index\",\"shards_acknowledged\":true,\"acknowledged\":true}")
                .returnResult()

        }
    }
}

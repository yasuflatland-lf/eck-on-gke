package design.studio.content.search

import design.studio.content.search.service.AbstractContainerBaseTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * @author Yasuyuki Takeo
 */
@Testcontainers
@ActiveProfiles("test")
@WebFluxTest(ContentSearchApplicationTests::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = [AbstractContainerBaseTest.Initializer::class])
class ContentSearchApplicationTests : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var client: WebTestClient

    init {
        test("exec smoke") {
            val baseUri = "http://localhost:" + "9200"
            client = WebTestClient.bindToServer().baseUrl(baseUri).build()
            client.get()
                .exchange()
                .expectStatus().is4xxClientError()
        }
    }

}

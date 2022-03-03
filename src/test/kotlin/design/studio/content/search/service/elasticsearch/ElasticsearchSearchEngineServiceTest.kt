package design.studio.content.search.service.elasticsearch

import design.studio.content.search.service.AbstractContainerBaseTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

/**
 * @author Yasuyuki Takeo
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractContainerBaseTest.Initializer::class])
class ElasticsearchSearchEngineServiceTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var elasticsearchSearchEngineService: ElasticsearchSearchEngineService

    val indexName = "studio-index"

    init {
        test("getClient") {
            elasticsearchSearchEngineService shouldNotBe null
            elasticsearchSearchEngineService.getClient() shouldNotBe null
        }

        test("initialize smoke") {
            val result = elasticsearchSearchEngineService.initialize(indexName, "foo")
            StepVerifier.create(result)
                .expectNextMatches { res ->
                    res.acknowledged() == true
                }
                .verifyComplete()
        }
    }
}

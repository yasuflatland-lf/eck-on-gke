package design.studio.content.search.usecase

import design.studio.content.search.model.CMSArticle
import design.studio.content.search.service.AbstractContainerBaseTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractContainerBaseTest.Initializer::class])
class CMSArticleHandlerTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var cmsArticleHandler: CMSArticleHandler

    val indexName = "testindex"

    init {
        beforeEach {
            cmsArticleHandler.deleteIndices(indexName).block()
        }

        test("initialize smoke") {
            val result = cmsArticleHandler.initialize(indexName, "foo")
            StepVerifier.create(result)
                .expectNextMatches { res ->
                    res.acknowledged() == true
                }
                .verifyComplete()
        }

        test("add index smoke") {
            val result = cmsArticleHandler.initialize(indexName, "foo")
                .flatMap { _ ->
                    var entiety = CMSArticle()
                    entiety.title = "test title"
                    entiety.content = "test content"
                    cmsArticleHandler.createIndex(indexName, "hoge", entiety)
                }
            StepVerifier.create(result)
                .expectNextMatches { res ->
                    res.id() != ""
                }
                .verifyComplete()
        }
    }
}

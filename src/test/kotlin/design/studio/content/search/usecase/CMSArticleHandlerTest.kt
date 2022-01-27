package design.studio.content.search.usecase

import design.studio.content.search.service.AbstractContainerBaseTest
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractContainerBaseTest.Initializer::class])
class CMSArticleHandlerTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var cmsArticleHandler: CMSArticleHandler

    init {
        test("initialize smoke") {
            val exception = shouldNotThrowAny {
                cmsArticleHandler.initialize()
            }
        }
    }
}

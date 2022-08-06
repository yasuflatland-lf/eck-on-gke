package design.studio.content.search.usecase

import design.studio.content.search.model.CMSArticle
import design.studio.content.search.service.AbstractContainerBaseTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * @author Yasuyuki Takeo
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractContainerBaseTest.Initializer::class])
class CMSArticleHandlerTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var cmsArticleHandler: CMSArticleHandler

    val indexName = "studio-index"

    init {
        afterEach {
            runTest {
                var result = cmsArticleHandler.deleteIndices(indexName)
            }
        }

        test("add index smoke") {
            runTest {
                var entiety = CMSArticle()
                var id = "hoge"

                entiety.title = "test title"
                entiety.content = "test content"
                entiety.title_ja = "test title"
                entiety.content_ja = "test content"
                var res = async { cmsArticleHandler.createIndex(indexName, id, entiety) }
                res.await().id() shouldBe id
            }
        }

    }
}

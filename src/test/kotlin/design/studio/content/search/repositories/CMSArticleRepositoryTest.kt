package design.studio.content.search.repositories

import co.elastic.clients.elasticsearch.core.bulk.OperationType
import design.studio.content.search.model.CMSArticle
import design.studio.content.search.service.AbstractContainerBaseTest
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import design.studio.content.search.test.utils.TestContentReader
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
class CMSArticleRepositoryTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var cmsArticleRepository: CMSArticleRepository

    init {
        afterEach {
            runTest {
                cmsArticleRepository.deleteIndex(ConnectionConstants.DEFAULT_INDEX)
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
                var res = async { cmsArticleRepository.createIndex(ConnectionConstants.DEFAULT_INDEX, id, entiety) }
                res.await().id() shouldBe id
            }
        }

        test("search smoke") {
            runTest {
                var entiety = CMSArticle()
                var id = "hoge"

                entiety.title = "test title"
                entiety.content = "test content"
                entiety.title_ja = "test title"
                entiety.content_ja = "test content"
                var res = async { cmsArticleRepository.createIndex(ConnectionConstants.DEFAULT_INDEX, id, entiety) }
                res.await().id() shouldBe id

                var result = async { cmsArticleRepository.search(ConnectionConstants.DEFAULT_INDEX, "test", 0, 100) }
                result.await().size shouldNotBe 1

                var result2 = async { cmsArticleRepository.search(ConnectionConstants.DEFAULT_INDEX, "aaa", 0, 100) }
                result2.await().size shouldBe 0

            }
        }

        test("Bulk add smoke") {
            runTest {
                var contents = TestContentReader.getContents("/testContents.json")
                var bulk = contents?.map { it ->
                    var entity = CMSArticle()
                    entity.title_ja = it.title
                    entity.content_ja = it.body
                    return@map entity
                }.orEmpty()

                var res = async {
                    cmsArticleRepository.createBulkIndices(ConnectionConstants.DEFAULT_INDEX, bulk)
                }

                res.await().errors() shouldBe false
                res.await().items()[0].operationType() shouldBe OperationType.Create
                res.await().items()[0].status() shouldBe HttpResponseStatus.CREATED.code()
            }
        }

        test("Bulk update smoke") {
            runTest {

                // Create Index
                var contents = TestContentReader.getContents("/testContents.json")
                var bulk = contents?.map { it ->
                    var entity = CMSArticle()
                    entity.title_ja = it.title
                    entity.content_ja = it.body
                    return@map entity
                }.orEmpty()

                var res = async {
                    cmsArticleRepository.createBulkIndices(ConnectionConstants.DEFAULT_INDEX, bulk)
                }

                var result = res.await()
                var id = result.items()[0].id().orEmpty()
                result.errors() shouldBe false
                result.items()[0].status() shouldBe HttpResponseStatus.CREATED.code()
                result.items()[0].operationType() shouldBe OperationType.Create

                // Update Test
                var update = listOf<CMSArticle>(CMSArticle(id, 0, 0, listOf(0), "", "", "updated title", "updated content"))

                var updateRes = async {
                    cmsArticleRepository.updateBulkIndices(ConnectionConstants.DEFAULT_INDEX, update)
                }

                var updateResult = updateRes.await()
                updateResult.errors() shouldBe false
                updateResult.items()[0].status() shouldBe HttpResponseStatus.OK.code()
                updateResult.items()[0].operationType() shouldBe OperationType.Update
            }
        }
    }
}

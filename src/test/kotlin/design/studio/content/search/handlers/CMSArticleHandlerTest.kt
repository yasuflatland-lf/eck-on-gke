package design.studio.content.search.handlers

import co.elastic.clients.elasticsearch.indices.CreateIndexResponse
import co.elastic.clients.json.JsonpUtils
import design.studio.content.search.utils.ToStringMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class CMSArticleHandlerTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    init {

        test("json convert test") {
            var res = CreateIndexResponse.of { b: CreateIndexResponse.Builder ->
                b
                    .index("some-index")
                    .shardsAcknowledged(true)
                    .acknowledged(true)
            }
            var sb = StringBuilder()
            var result = JsonpUtils.toString(res, ToStringMapper.INSTANCE, sb)
            result.contains("some-index") shouldBe true
        }
    }
}
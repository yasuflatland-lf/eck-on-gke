package design.studio.content.search.test.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class TestContentReaderTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    init {
        test("Smoke") {
            var contents = TestContentReader.getContents("/testContents.json")
            contents?.get(0)?.title shouldNotBe ""
        }
    }
}


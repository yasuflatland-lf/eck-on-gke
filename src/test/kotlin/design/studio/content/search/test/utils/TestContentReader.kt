package design.studio.content.search.test.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.test.context.ActiveProfiles

@Serializable
data class TestContent(
    var title: String? = "",
    var body: String? = ""
)

@ActiveProfiles("test")
class TestContentReader {
    companion object {
        private val stringFormat: StringFormat = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun getContents(fileName: String): List<TestContent>? {
            var content = TestContentReader::class.java.getResource(fileName).readText(Charsets.UTF_8)
            return content?.let {
                stringFormat.decodeFromString(it)
            }
        }

    }
}
package design.studio.content.search.test.utils

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

class TestContent(
    var title: String? = "", var content: String? = "",
    // sqlTimestampIso8601, Timezone is GTM
    // https://qiita.com/niwasawa/items/27f769d8ec4742151872
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonFormat(
        shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss"
    ) var createdDateTime: LocalDateTime?
)

@ActiveProfiles("test")
class TestContentReader {
    companion object {
        fun getContents(fileName: String): List<TestContent>? {
            var jsonContent = TestContentReader::class.java.getResource(fileName).readText(Charsets.UTF_8)
            val contentList: List<TestContent>? =
                ObjectMapper().findAndRegisterModules().registerModule(JavaTimeModule()) // JSR-310 support
                    .readValue(jsonContent, object : TypeReference<List<TestContent>>() {})
            return contentList
        }
    }
}

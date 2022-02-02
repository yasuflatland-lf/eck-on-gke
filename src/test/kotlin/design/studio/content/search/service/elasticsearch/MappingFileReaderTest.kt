package design.studio.content.search.service.elasticsearch

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain


class MappingFileReaderTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    init {
        test("getResource Test") {
            var reader: MappingFileReader = MappingFileReader()
            var json: String = reader.getResource(reader.MAPPING_FILE_PATH)
            json shouldContain "template_long_sortable"
        }

        test("getTypeMappings Test") {
            val json = """{
  "testindex" : {
    "mappings" : {
      "properties" : {
        "id" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "name" : {
          "properties" : {
            "first" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "last" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            }
          }
        }
      }
    }
  }
}"""
            var reader = MappingFileReader()
            val mappings = reader.getTypeMappings("testindex", json)
            mappings shouldNotBe null
            if (mappings != null) {
                (mappings.properties().get("name")?.isObject() ?: false) shouldBe true
                (mappings.properties().get("id")?.isText() ?: false) shouldBe true
            }
        }
    }
}

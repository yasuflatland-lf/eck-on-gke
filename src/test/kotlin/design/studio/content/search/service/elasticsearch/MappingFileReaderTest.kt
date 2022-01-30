package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch.indices.GetMappingResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class MappingFileReaderTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)
    companion object {
        val log: Logger = LoggerFactory.getLogger(MappingFileReaderTest::class.java)
    }

    init {
        test("getResource Test") {
            var reader: MappingFileReader = MappingFileReader()
            var json: String = reader.getResource(reader.MAPPING_FILE_PATH)
            json shouldContain "template_long_sortable"
        }

        test("fromJson Test") {
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
            var reader: MappingFileReader = MappingFileReader()
            var response: GetMappingResponse = reader.fromJson(json, GetMappingResponse._DESERIALIZER)
            val mappings = response["testindex"].mappings()
            (mappings.properties().get("name")?.isObject() ?: false ) shouldBe true
        }
    }
}

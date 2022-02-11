package design.studio.content.search.service.elasticsearch

import design.studio.content.search.service.elasticsearch.connection.constants.MappingConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * @author Yasuyuki Takeo
 */
class MappingFileReaderTest : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    init {
        test("getResource Test") {
            var reader = MappingFileReader()
            var json: String = reader.getResource(MappingConstants.MAPPING_FILE_PATH)
            json shouldContain "with_positions_offsets"
        }

        test("studio-type-mappings.json Test") {
            var reader = MappingFileReader()
            var json: String = reader.getResource(MappingConstants.MAPPING_FILE_PATH)
            val mappings = reader.getTypeMappings("studio-index", json)
            mappings shouldNotBe null
            if (mappings != null) {
                mappings.dynamicTemplates()[0].get("template_ja")?.mapping()?.text()?.analyzer() shouldBe "kuromoji"
                mappings.dynamicTemplates()[0].get("template_ja")?.mapping()?.text()?.store() shouldBe true
                (mappings.dynamicTemplates()[0].get("template_ja")?.mapping()?.text()?.termVector()?.jsonValue()
                    ?: "") shouldBe "with_positions_offsets"
            }
        }

        test("getTypeMappings Test") {
            val json = """{
  "testindex" : {
    "mappings" : {
        "dynamic_templates": [
          {
            "integers": {
              "match_mapping_type": "long",
              "mapping": {
                "type": "integer"
              }
            }
          },
          {
            "strings": {
              "match_mapping_type": "string",
              "mapping": {
                "type": "text",
                "fields": {
                  "raw": {
                    "type":  "keyword",
                    "ignore_above": 256
                  }
                }
              }
            }
          }
        ],    
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
                mappings.properties().get("name")?.isObject() shouldBe true
                mappings.properties().get("id")?.isText() shouldBe true
                mappings.dynamicTemplates()[0].get("integers")?.matchMappingType() shouldBe "long"
                mappings.dynamicTemplates()[1].get("strings")?.mapping()?.text()?.fields()?.get("raw")?.keyword()
                    ?.ignoreAbove() shouldBe 256
            }
        }
    }
}

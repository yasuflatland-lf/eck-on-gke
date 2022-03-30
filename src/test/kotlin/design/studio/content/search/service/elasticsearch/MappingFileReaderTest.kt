package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch._types.query_dsl.*
import design.studio.content.search.service.elasticsearch.connection.constants.MappingConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
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
                mappings.dynamicTemplates()[1].get("template_ja")?.mapping()?.text()
                    ?.analyzer() shouldBe "studio_analyzer_ja"
                mappings.dynamicTemplates()[1].get("template_ja")?.mapping()?.text()?.store() shouldBe true
                (mappings.dynamicTemplates()[1].get("template_ja")?.mapping()?.text()?.termVector()?.jsonValue()
                    ?: "") shouldBe "with_positions_offsets"
            }
        }

        test("getTypeMappings Test") {
            val json = """{
  "testindex": {
    "mappings": {
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
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            }
          }
        },
        {
          "template_en": {
            "match_mapping_type": "string",
            "match": "\\w+_en\\b|\\w+_en_[A-Z]{2}\\b",
            "match_pattern": "regex",
            "mapping": {
              "analyzer": "english",
              "search_analyzer": "studio_analyzer_en",
              "store": true,
              "term_vector": "with_positions_offsets",
              "type": "text"
            }
          }
        }
      ],
      "properties": {
        "id": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "name": {
          "properties": {
            "first": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            },
            "last": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
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
                mappings.dynamicTemplates()[2].get("template_en")?.matchMappingType() shouldBe "string"
            }
        }

        test("getIndexSettings Test") {
            val json = """{
  "testindex": {
    "settings": {
      "analysis": {
        "char_filter": {
          "normalize": {
            "type": "icu_normalizer",
            "name": "nfkc",
            "mode": "compose"
          }
        },
        "tokenizer": {
          "kuromoji_tokenizer_ja": {
            "mode": "search",
            "type": "kuromoji_tokenizer",
            "discard_compound_token": true
          },
          "ngram_tokenizer_ja": {
            "type": "ngram",
            "min_gram": 3,
            "max_gram": 3,
            "token_chars": [
              "letter",
              "digit"
            ]
          }
        },
        "analyzer": {
          "keyword_lowercase": {
            "type": "custom",
            "filter": "lowercase",
            "tokenizer": "keyword"
          },
          "studio_analyzer_en": {
            "type": "custom",
            "filter": [
              "english_possessive_stemmer",
              "lowercase",
              "studio_filter_synonym_en",
              "english_stop",
              "english_stemmer"
            ],
            "tokenizer": "standard"
          },
          "studio_analyzer_ja": {
            "type": "custom",
            "char_filter": [
              "normalize"
            ],
            "tokenizer": "kuromoji_tokenizer_ja",
            "filter": [
              "kuromoji_baseform",
              "kuromoji_part_of_speech",
              "studio_filter_synonym_ja",
              "cjk_width",
              "ja_stop",
              "kuromoji_stemmer",
              "lowercase"
            ]
          },
          "studio_analyzer_search_ja": {
            "type": "custom",
            "char_filter": [
              "normalize"
            ],
            "tokenizer": "kuromoji_tokenizer_ja",
            "filter": [
              "kuromoji_baseform",
              "kuromoji_part_of_speech",
              "studio_filter_synonym_ja",
              "cjk_width",
              "ja_stop",
              "kuromoji_stemmer",
              "lowercase"
            ]
          },
          "studio_analyzer_ngram_ja": {
            "type": "custom",
            "char_filter": [
              "normalize"
            ],
            "tokenizer": "ngram_tokenizer_ja",
            "filter": [
              "lowercase"
            ]
          },
          "studio_analyzer_search_ngram_ja": {
            "type": "custom",
            "char_filter": [
              "normalize"
            ],
            "tokenizer": "ngram_tokenizer_ja",
            "filter": [
              "studio_filter_synonym_ja",
              "lowercase"
            ]
          }
        },
        "filter": {
          "english_possessive_stemmer": {
            "language": "possessive_english",
            "type": "stemmer"
          },
          "english_stemmer": {
            "language": "english",
            "type": "stemmer"
          },
          "english_stop": {
            "stopwords": "_english_",
            "type": "stop"
          },
          "studio_filter_synonym_en": {
            "lenient": true,
            "synonyms": [],
            "type": "synonym_graph"
          },
          "studio_filter_synonym_es": {
            "lenient": true,
            "synonyms": [],
            "type": "synonym_graph"
          },
          "studio_filter_synonym_index_ja": {
            "lenient": true,
            "synonyms": [],
            "type": "synonym_graph"
          },
          "studio_filter_synonym_ja": {
            "lenient": true,
            "synonyms": [],
            "type": "synonym_graph"
          }
        }
      }
    }
  }
}"""
            var reader = MappingFileReader()
            val settings = reader.getIndexSettings("testindex", json)
            settings shouldNotBe null
            if (settings != null) {
                settings.analysis()?.tokenizer()?.get("kuromoji_tokenizer_ja")?.isDefinition shouldBe true
            }
        }

        test("getQueryFromJSON Test") {
            var reader = MappingFileReader()
            val expected = Query.of { _0: Query.Builder ->
                _0
                    .intervals { _1: IntervalsQuery.Builder ->
                        _1
                            .queryName("my-query")
                            .field("a_field")
                            .anyOf { _2: IntervalsAnyOf.Builder ->
                                _2
                                    .intervals { _3: Intervals.Builder ->
                                        _3
                                            .match { _5: IntervalsMatch.Builder ->
                                                _5
                                                    .query("match-query")
                                                    .analyzer("lowercase")
                                            }
                                    }
                            }
                    }
            }
            var queryJSON = "{\"intervals\":{\"a_field\":{\"_name\":\"my-query\"," +
                    "\"any_of\":{\"intervals\":[{\"match\":{\"analyzer\":\"lowercase\",\"query\":\"match-query\"}}]}}}}"
            var query = reader.fromJson(
                queryJSON, Query._DESERIALIZER
            )

            reader.toJson(query) shouldBe reader.toJson(expected)
        }
    }
}

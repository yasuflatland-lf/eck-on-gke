package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.indices.GetMappingResponse
import co.elastic.clients.json.JsonpDeserializer
import co.elastic.clients.json.JsonpMapper
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.StringReader


class MappingFileReader() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(MappingFileReader::class.java)
    }

    lateinit var mapper: JsonpMapper

    init {
        mapper = JacksonJsonpMapper()
    }

    // TODO : Real mapping format is found here:
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic-templates.html#dynamic-mapping-runtime-fields
    final val MAPPING_FILE_PATH = "/mappings/liferay-type-mappings.json"

    fun getResource(fileName: String) =
        this::class.java.getResource(fileName).readText(Charsets.UTF_8)

    fun getTypeMappings(indexName: String, json: String): TypeMapping? {
        val response: GetMappingResponse = fromJson(json, GetMappingResponse._DESERIALIZER)

        return response[indexName].mappings()
    }

    fun <T> fromJson(json: String?, deserializer: JsonpDeserializer<T>): T {
        val parser: jakarta.json.stream.JsonParser? = mapper.jsonProvider().createParser(StringReader(json))
        return deserializer.deserialize(parser, mapper)
    }

}
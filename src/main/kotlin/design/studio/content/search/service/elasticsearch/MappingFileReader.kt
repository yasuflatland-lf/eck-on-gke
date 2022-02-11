package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.indices.GetMappingResponse
import co.elastic.clients.json.JsonpDeserializer
import co.elastic.clients.json.JsonpMapper
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import java.io.StringReader

/**
 * @author Yasuyuki Takeo
 */
class MappingFileReader() {
    var mapper: JsonpMapper = JacksonJsonpMapper()

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
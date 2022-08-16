package design.studio.content.search.service.elasticsearch

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.indices.GetIndicesSettingsResponse
import co.elastic.clients.elasticsearch.indices.GetMappingResponse
import co.elastic.clients.elasticsearch.indices.IndexSettings
import co.elastic.clients.json.JsonpDeserializer
import co.elastic.clients.json.JsonpMapper
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import io.micrometer.core.instrument.config.InvalidConfigurationException
import jakarta.json.spi.JsonProvider
import jakarta.json.stream.JsonGenerator
import jakarta.json.stream.JsonParser
import java.io.StringReader
import java.io.StringWriter

/**
 * @author Yasuyuki Takeo
 */
class MappingFileReader() {
    var mapper: JsonpMapper = JacksonJsonpMapper()

    fun getResource(fileName: String) =
        this::class.java.getResource(fileName).readText(Charsets.UTF_8)

    fun getTypeMappings(indexName: String, json: String): TypeMapping? {
        val response: GetMappingResponse = fromJson(json, GetMappingResponse._DESERIALIZER)
            ?: throw InvalidConfigurationException("Mappings are not properly defined.")

        return response.get(indexName)!!.mappings()
    }

    fun getIndexSettings(indexName: String, json: String): IndexSettings? {
        val response: GetIndicesSettingsResponse = fromJson(json, GetIndicesSettingsResponse._DESERIALIZER)
            ?: throw InvalidConfigurationException("Index settings are not properly defined.")

        return response.get(indexName)!!.settings()
    }

    fun <T> toJson(value: T): String {
        val sw = StringWriter()
        val provider: JsonProvider = mapper.jsonProvider()
        val generator: JsonGenerator = provider.createGenerator(sw)
        mapper.serialize(value, generator)
        generator.close()
        return sw.toString()
    }

    fun <T> fromJson(json: String?, deserializer: JsonpDeserializer<T>): T {
        val parser: JsonParser? = mapper.jsonProvider().createParser(StringReader(json))
        return deserializer.deserialize(parser, mapper)
    }

    fun <T> fromJson(json: String?, clazz: Class<T>?): T {
        val parser: JsonParser? = mapper.jsonProvider().createParser(StringReader(json))
        return mapper.deserialize(parser, clazz)
    }
}
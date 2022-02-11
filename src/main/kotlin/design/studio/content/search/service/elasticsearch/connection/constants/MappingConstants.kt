package design.studio.content.search.service.elasticsearch.connection.constants

/**
 * @author Yasuyuki Takeo
 */
class MappingConstants {
    companion object {
        // Real mapping format is found here:
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic-templates.html#dynamic-mapping-runtime-fields
        const val MAPPING_FILE_PATH = "/mappings/studio-type-mappings.json"
    }
}
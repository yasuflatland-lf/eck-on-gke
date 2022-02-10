package design.studio.content.search.service.elasticsearch.connection

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.ElasticsearchClient

/**
 * @author Yasuyuki Takeo
 */
interface ElasticsearchClientResolver {
    fun getClient() : ElasticsearchAsyncClient
}
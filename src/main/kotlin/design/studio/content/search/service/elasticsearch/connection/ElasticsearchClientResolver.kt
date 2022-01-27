package design.studio.content.search.service.elasticsearch.connection

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.ElasticsearchClient

interface ElasticsearchClientResolver {
    fun getClient() : ElasticsearchAsyncClient
}
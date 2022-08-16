package design.studio.content.search.handlers

import design.studio.content.search.repositories.CMSArticleRepository
import design.studio.content.search.service.elasticsearch.JsonUtils
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import kotlinx.coroutines.flow.flowOf
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.bodyAndAwait

/**
 * @author Yasuyuki Takeo
 */
@Component
class CMSArticleHandler(val repository: CMSArticleRepository) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(CMSArticleHandler::class.java)
    }

    suspend fun initialize(request: ServerRequest): ServerResponse {
        return runCatching {
            val indexName =
                request.queryParam("indexName").map { it.toString() }.orElse(ConnectionConstants.DEFAULT_INDEX)
            val alias =
                request.queryParam("alias").map { it.toString() }.orElse(ConnectionConstants.REMOTE_CONNECTION_ID)
            repository.initialize(indexName, alias)
        }.fold(
            onSuccess = {
                ok().contentType(APPLICATION_JSON)
                    .bodyAndAwait(flowOf(JsonUtils().toJson(it)))
            },
            onFailure = {
                log.error(it.message)
                status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(APPLICATION_JSON)
                    .bodyAndAwait(flowOf(it))
            }
        )
    }
}
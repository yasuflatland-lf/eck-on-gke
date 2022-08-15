package design.studio.content.search.routers

import design.studio.content.search.handlers.CMSArticleHandler
import design.studio.content.search.service.elasticsearch.connection.constants.ConnectionConstants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.coRouter

/**
 * @author Yasuyuki Takeo
 */
@Configuration
class CMSArticleRouter {
    @Bean
    @RouterOperations(
        RouterOperation(
            path = "/articles/search/init",
            method = [RequestMethod.GET],
            operation = Operation(
                operationId = "initialize",
                summary = "Initialize Elasticsearch settings and mappings",
                tags = ["Article"],
                parameters = [
                    Parameter(`in` = ParameterIn.QUERY, name = "indexName", description = "indexName", example = ConnectionConstants.DEFAULT_INDEX),
                    Parameter(`in` = ParameterIn.QUERY, name = "alias", description = "alias", example = ConnectionConstants.REMOTE_CONNECTION_ID),
                ]
            )
        ),
    )
    fun router(handler: CMSArticleHandler) = coRouter {

        accept(APPLICATION_JSON).nest {
            GET("/articles/search/init", handler::initialize)
        }
    }
}
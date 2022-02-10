package design.studio.content.search.service.elasticsearch

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app.elastic")
data class ElasticConfig(
    var connectionId: String,
    var serverName: String,
    var port: Int,
    var username: String,
    var password: String
)

package design.studio.content.search.service

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration

/**
 * @author Yasuyuki Takeo
 */
@Testcontainers
abstract class AbstractContainerBaseTest {
    companion object {
        var composeContainer: KDockerComposeContainer = KDockerComposeContainer(File("docker-compose-test.yml"))
            .withLocalCompose(true)
            .withEnv("STACK_VERSION", "8.1.1")
            .withEnv("ELASTIC_PASSWORD", "test123")
            .withEnv("KIBANA_PASSWORD", "test123")
            .withEnv("CLUSTER_NAME", "StudioElasticsearchCluster")
            .withEnv("LICENSE", "basic")
            .withEnv("ES_PORT", "9200")
            .withEnv("KIBANA_PORT", "5601")
            .withEnv("MEM_LIMIT", "1073741824")
            .apply {
                IndefiniteWaitOneShotStartupCheckStrategy()
            }
            .withExposedService("elasticsearch_1", 9200,
                WaitAllStrategy(WaitAllStrategy.Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY)
                    .apply {
                        withStrategy(
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(600))
                        )
                    }
            )
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            composeContainer.start()
        }
    }
}

class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)
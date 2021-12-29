package design.studio.content.search.service

import design.studio.content.search.logger
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration

@Testcontainers
abstract class AbstractContainerBaseTest {
    companion object {
        val logger = logger()
        var composeContainer: KDockerComposeContainer = KDockerComposeContainer(File("docker-compose.yml"))
            .withLocalCompose(true)
            .withEnv("ELASIC_VERSION", "7.16.2")
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
//            .apply {
//                withExposedService(
//                    "elasticsearch_1",
//                    9200,
//                    WaitAllStrategy(WaitAllStrategy.Mode.WITH_OUTER_TIMEOUT)
//                        .apply {
//                            withStrategy(
//                                Wait.forLogMessage(
//                                    ".*Cluster health status changed from [YELLOW] to [GREEN].*",
//                                    1
//                                )
//                            )
//                        }
//                )
//            }
//            .apply {
//                withExposedService("eck-on-gke_elasticsearch_1", 9200)
//                withExposedService("eck-on-gke_ent-search_1", 3002)
//                withExposedService("eck-on-gke_kubana_1", 5602)
//                start()
//            }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            logger.info("@@@@@@@@@@@@@@@@@ IN")
            composeContainer.start()
        }
    }
}

class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)
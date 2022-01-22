import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("io.freefair.lombok") version "6.3.0"
	id("org.springframework.boot") version "2.5.8"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
}

group = "design.studio"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

// Springboot
extra["kotestVersion"] = "5.1.0"
extra["springCloudGcpVersion"] = "2.0.7"
extra["springCloudVersion"] = "2020.0.5"
extra["testcontainersVersion"] = "1.16.2"
extra["kotlin-coroutines.version"] = "1.6.0"

// Elasticsearch
extra["elasticVersion"] = "7.16.3"
extra["jacksonDataBindVersion"] = "2.12.3"

dependencies {
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor" )
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("com.google.cloud:spring-cloud-gcp-starter")
	implementation("com.google.cloud:spring-cloud-gcp-starter-storage")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springframework:spring-jdbc")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

	// Elasticsearch
	implementation("co.elastic.clients:elasticsearch-java:${property("elasticVersion")}")
	implementation("com.fasterxml.jackson.core:jackson-databind:${property("jacksonDataBindVersion")}")

	// Test
	testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
	testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
	testImplementation("io.kotest:kotest-property:${property("kotestVersion")}")
	// TODO : Add the line below to get rid of error of coroutine for Kotest 5.1.0. https://github.com/kotest/kotest/issues/2782
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:testcontainers")
}

dependencyManagement {
	imports {
		mavenBom("com.google.cloud:spring-cloud-gcp-dependencies:${property("springCloudGcpVersion")}")
		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

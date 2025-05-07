import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("io.ktor.plugin") version libs.versions.ktor
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin
    id("org.unbroken-dome.xjc") version "2.0.0"
}

group = "nl.parlementairemonitor"
version = "v0.2.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

xjc {
    xjcVersion.set("3.0")
}

kotlin.compilerOptions.apiVersion = KotlinVersion.KOTLIN_2_1
kotlin.compilerOptions.languageVersion = KotlinVersion.KOTLIN_2_1
kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_21

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation(libs.jackson.module.jaxb.annotations)
    implementation(libs.jakarta.xml.bind.api)
    implementation(libs.jaxb.runtime)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.openapi)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.task.scheduling.core)
    implementation(libs.ktor.server.task.scheduling.mongodb)
    implementation(libs.ktor.swagger.ui)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.mongodb.bson)
    implementation(libs.mongodb.driver.core)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.pdfbox)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.poi.scratchpad)
    implementation(libs.rome)
    implementation(libs.schema.kenerator.core)
    implementation(libs.schema.kenerator.swagger)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaExec>().configureEach {
    file(".env").takeIf { it.exists() }
        ?.readLines()
        ?.filter { it.isNotEmpty() }
        ?.forEach { s ->
            val (key, value) = s.split("=")
            environment(key to value)
        }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

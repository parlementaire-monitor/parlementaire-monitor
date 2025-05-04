import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

val kotlin_version: String by project
val kotlin_css_version: String by project
val logback_version: String by project
val mongo_version: String by project
val prometheus_version: String by project
val junit5_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
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
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-forwarded-header")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.mongodb:mongodb-driver-core:$mongo_version")
    implementation("org.mongodb:mongodb-driver-sync:$mongo_version")
    implementation("org.mongodb:bson:$mongo_version")
    implementation("io.github.flaxoos:ktor-server-task-scheduling-core:2.1.2")
    implementation("io.github.flaxoos:ktor-server-task-scheduling-mongodb:2.1.2")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.1")
    implementation("com.rometools:rome:2.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.18.3")
    implementation("io.ktor:ktor-client-java:3.1.2")
    implementation("org.apache.pdfbox:pdfbox:2.0.34")
    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")
    implementation("org.apache.poi:poi-scratchpad:5.4.1")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")

    implementation("io.github.smiley4:ktor-openapi:5.0.2")
    implementation("io.github.smiley4:schema-kenerator-core:2.1.2")
    implementation("io.github.smiley4:schema-kenerator-swagger:2.1.2")
    runtimeOnly("io.github.smiley4:ktor-swagger-ui:5.0.2")

//    testImplementation("io.ktor:ktor-server-test-host")
//    testImplementation("io.ktor:ktor-server-test-host-jvm:3.1.2")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.20")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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

plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("maven-publish")
}

group = "dev.surly"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1")
    api("org.slf4j:slf4j-simple:2.0.16")
    api("io.github.oshai:kotlin-logging-jvm:7.0.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "dev.surly"
            artifactId = "kotlin-minimal-server"
            version = "0.1.0"
        }
    }
}

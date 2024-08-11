import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("com.vanniktech.maven.publish") version "0.28.0"
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


mavenPublishing {
    coordinates("dev.surly", "kotlin-minimal-server", "0.1.0")
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    pom {
        name.set("Kotlin Minimal Server")
        description.set("A near zero dependency Kotlin web server")
        inceptionYear.set("2024")
        url.set("https://github.com/thesurlydev/kotlin-minimal-server/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("thesurlydev")
                name.set("Shane Witbeck")
                url.set("https://github.com/thesurlydev/")
            }
        }
        scm {
            url.set("https://github.com/thesurlydev/kotlin-minimal-server/")
            connection.set("scm:git:git://github.com/thesurlydev/kotlin-minimal-server.git")
            developerConnection.set("scm:git:ssh://git@github.com/thesurlydev/kotlin-minimal-server.git")
        }
    }
}
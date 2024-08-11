# kotlin-minimal-server

A near zero dependency Kotlin web server.

[kotlin-minimal-example](https://github.com/thesurlydev/kotlin-minimal-example) is an example implementation.

## Goals

* No annotations
* No reflection
* Minimal dependencies


## Compile and Package

```shell
./gradlew clean build
```

## Dependency

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.surly:kotlin-minimal-server:0.1.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'dev.surly:kotlin-minimal-server:0.1.0'
```

### Maven

```xml
<dependency>
    <groupId>dev.surly</groupId>
    <artifactId>kotlin-minimal-server</artifactId>
    <version>0.1.0</version>
</dependency>
```
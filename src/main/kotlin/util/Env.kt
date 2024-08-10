package util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}

/*
System properties are used to store environment variables.
 */
@Suppress("Unused")
class Env {

  companion object {
    fun loadEnvFile(path: String) {
      val envFile = File(path)
      if (envFile.exists()) {
        var count = 0
        envFile.forEachLine { line ->
          if (line.isBlank() || line.startsWith("#")) {
            return@forEachLine
          }
          count++
          val (key, value) = line.split("=", limit = 2)
          setEnv(key.trim(), value.trim())
        }
        log.info { "Loaded $count environment variables from $path file" }
      } else {
        log.warn { "$path file not found" }
      }
    }

    fun getEnv(key: String): String? {
      return System.getProperty(key)
    }

    fun setEnv(key: String, value: String) {
      System.setProperty(key, value)
    }
  }
}
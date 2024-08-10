package util

import java.util.concurrent.ConcurrentHashMap

object ResourceLoader {
    private val cache = ConcurrentHashMap<String, String>()

    fun loadResourceAsString(filePath: String): String {
        return cache.computeIfAbsent(filePath) { key ->
            object {}.javaClass.getResourceAsStream(key)
                ?.bufferedReader()
                .use { reader ->
                    reader?.readText().orEmpty()
                }
        }
    }
}

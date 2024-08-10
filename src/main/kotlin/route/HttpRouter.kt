package route

import DOT
import http.server.ContentType.Companion.fromExtension
import http.server.HttpMethod
import http.server.HttpRequest
import http.server.HttpResponse
import http.server.HttpResponse.Companion.notFound
import http.server.HttpResponse.Companion.ok
import util.ResourceLoader
import java.util.*

const val INDEX_HTML = "index.html"
const val HEALTH_CHECK_BODY = """{ "status": "UP" }"""

class HttpRouter {

    val routes = mutableMapOf<String, MutableMap<String, RequestHandler>>()

    private fun addRoute(method: String, path: String, handler: RequestHandler) {
        routes.computeIfAbsent(method) { mutableMapOf() }[path] = handler
    }

    fun enableHealthCheck(path: String = "/health"): HttpRouter {
        get(path) { _ -> ok().json(HEALTH_CHECK_BODY.trimIndent().toByteArray()) }
        return this
    }

    fun delete(path: String, handler: (HttpRequest) -> HttpResponse): HttpRouter {
        return register(HttpMethod.DELETE, path, handler)
    }

    fun get(path: String, handler: RequestHandler): HttpRouter {
        return register(HttpMethod.GET, path, handler)
    }

    fun post(path: String, handler: (HttpRequest) -> HttpResponse): HttpRouter {
        return register(HttpMethod.POST, path, handler)
    }

    private fun register(
        method: HttpMethod,
        path: String,
        handler: RequestHandler,
    ): HttpRouter {
        addRoute(method.toString(), path, handler)
        return this
    }

    private fun handleStaticFile(requestedPath: String): HttpResponse {
        val normalizedPath = requestedPath.normalizePath()
        val fileContent = ResourceLoader.loadResourceAsString(normalizedPath)
        if (fileContent.isEmpty()) {
            return notFound()
        }
        val requestedExtension = normalizedPath.substringAfterLast(DOT, "html")
        return ok().contentType(fromExtension(requestedExtension)).body(fileContent.toByteArray())
    }

    private fun String.normalizePath(): String = if (this.isEmpty() || this == "/") {
        "/static/$INDEX_HTML"
    } else {
        "/static/$this"
    }
}

typealias RequestHandler = (HttpRequest) -> HttpResponse

inline fun <reified T : Any> controllerMethodHandler(crossinline method: (T) -> HttpResponse): RequestHandler {
    return { request ->
        val paramValue = request.pathParams.values.first()
        val convertedValue = paramValue.let {
            when (T::class) {
                UUID::class -> UUID.fromString(it)
                else -> it
            }
        }
        method(convertedValue as T)
    }
}
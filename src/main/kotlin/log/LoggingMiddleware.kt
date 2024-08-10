package log

import http.server.HttpRequest
import http.server.HttpResponse
import middleware.Middleware

@Suppress("Unused")
class LoggingMiddleware : Middleware {
  override fun handleRequest(request: HttpRequest, response: HttpResponse, next: () -> Unit) {
    val startTime = System.currentTimeMillis()
    next()
    val endTime = System.currentTimeMillis() - startTime
    log.info { "${request.method} ${request.uri()} ${response.statusCode} ${endTime}ms" }
  }
}
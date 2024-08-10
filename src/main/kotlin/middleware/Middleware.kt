package middleware

import http.server.HttpRequest
import http.server.HttpResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

interface Middleware {
  /**
   * Process an HTTP request and response.
   *
   * @param request The HTTP request information.
   * @param response The HTTP response object.
   * @param next A function that invokes the next middleware in the chain.
   */
  fun handleRequest(request: HttpRequest, response: HttpResponse, next: () -> Unit)

  val log: KLogger
    get() = KotlinLogging.logger {}
}
package route

import BRACKET_CLOSE
import BRACKET_OPEN
import SLASH
import http.server.HttpRequest
import http.server.HttpResponse
import middleware.Middleware

class RoutingMiddleware(private val router: HttpRouter) : Middleware {
  override fun handleRequest(request: HttpRequest, response: HttpResponse, next: () -> Unit) {
    router.routes[request.method]?.forEach { (routePath, handler) ->
      val pathParams = matchPath(routePath, request.path)
      if (pathParams != null) {
        request.pathParams = pathParams
        val result = handler.invoke(request)
        response.statusCode = result.statusCode
        response.headers.putAll(result.headers)
        response.body = result.body
        return
      }
    }
    response.statusCode = 404
  }

  private fun matchPath(routePath: String, requestPath: String): Map<String, String>? {
    val routeParts = routePath.split(SLASH).filter { it.isNotEmpty() }
    val requestParts = requestPath.split(SLASH).filter { it.isNotEmpty() }

    if (routeParts.size != requestParts.size) return null

    val pathParams = mutableMapOf<String, String>()
    for (i in routeParts.indices) {
      if (routeParts[i].startsWith(BRACKET_OPEN) && routeParts[i].endsWith(BRACKET_CLOSE)) {
        val paramName = routeParts[i].substring(1, routeParts[i].length - 1)
        pathParams[paramName] = requestParts[i]
      } else if (routeParts[i] != requestParts[i]) {
        return null
      }
    }
    return pathParams
  }
}
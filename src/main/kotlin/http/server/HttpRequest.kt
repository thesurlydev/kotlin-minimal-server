package http.server

data class HttpRequest(
  val method: String,
  val path: String,
  val httpVersion: String,
  val headers: Map<String, String>,
  val query: Map<String, String> = emptyMap(),
  val body: String? = null,
  var pathParams: Map<String, String> = emptyMap(),
) {
  fun uri(): String {
    val queryString = if (query.isNotEmpty()) {
      query.map { "${it.key}=${it.value}" }.joinToString("&", "?")
    } else {
      ""
    }
    return "$path$queryString"
  }
}


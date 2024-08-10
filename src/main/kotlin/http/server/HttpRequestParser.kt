package http.server

import COLON
import CONTENT_LENGTH_HEADER_KEY
import SPACE
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object HttpRequestParser {
  fun parse(inputStream: InputStream): HttpRequest? {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val requestLine = reader.readLine() ?: return null
    val requestLineParts = requestLine.split(SPACE)
    if (requestLineParts.size < 3) throw IllegalArgumentException("Invalid Request Line Format")

    val method = requestLineParts[0]
    val uri = requestLineParts[1]

    val (query, path) = parseUri(uri)
    val headers = parseHeaders(reader)
    val body: String? = parseBody(method, headers, reader)
    val httpVersion = requestLineParts[2]

    return HttpRequest(method, path, httpVersion, headers, mutableMapOf(), body, query)
  }

  private fun parseUri(uri: String): Pair<MutableMap<String, String>, String> {
    val query = mutableMapOf<String, String>()
    val path = if (uri.contains("?")) {
      val parts = uri.split("?")
      val queryParams = parts[1].split("&")
      queryParams.forEach {
        val param = it.split("=")
        query[param[0]] = param[1]
      }
      parts[0]
    } else {
      uri
    }
    return Pair(query, path)
  }

  private fun parseHeaders(reader: BufferedReader): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()

    // Read headers
    while (true) {
      val line = reader.readLine() ?: break
      if (line.isEmpty()) break // Headers section is terminated by an empty line

      val header = line.split(COLON, limit = 2)
      if (header.size == 2) {
        headers[header[0].trim()] = header[1].trim()
      }
    }
    return headers
  }

  private fun parseBody(
    method: String,
    headers: MutableMap<String, String>,
    reader: BufferedReader,
  ): String? {
    var body: String? = null
    // Only read the body for PATCH, POST or PUT requests
    if (method.equals(HttpMethod.POST.toString(), ignoreCase = true)
      || method.equals(HttpMethod.PUT.toString(), ignoreCase = true)
      || method.equals(HttpMethod.PATCH.toString(), ignoreCase = true)
    ) {
      // Read body if any. Consider content-length to avoid reading indefinitely
      val contentLength = headers[CONTENT_LENGTH_HEADER_KEY]?.toIntOrNull()
      if (contentLength != null && contentLength > 0) {
        body = CharArray(contentLength).also { reader.read(it) }.concatToString()
      }
    }
    return body
  }
}
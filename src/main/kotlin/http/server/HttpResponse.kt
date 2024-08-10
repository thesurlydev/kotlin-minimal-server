package http.server

import NEW_LINE
import util.ResourceLoader
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val HTTP_1_1 = "HTTP/1.1"
const val REASON_PHRASE_OK = "OK"
const val REASON_PHRASE_BAD_REQUEST = "Bad Request"
const val REASON_PHRASE_NOT_FOUND = "Not Found"
const val REASON_PHRASE_INTERNAL_SERVER_ERROR = "Internal Server Error"

data class HttpResponse(
  var statusCode: Int? = null,
  var contentType: ContentType? = null,
  var headers: MutableMap<String, String> = mutableMapOf(),
  var body: ByteArray? = null,
) {
  private fun statusLine(): String {
    return when (statusCode) {
      200 -> "$HTTP_1_1 $statusCode $REASON_PHRASE_OK"
      400 -> "$HTTP_1_1 $statusCode $REASON_PHRASE_BAD_REQUEST"
      404 -> "$HTTP_1_1 $statusCode $REASON_PHRASE_NOT_FOUND"
      500 -> "$HTTP_1_1 $statusCode $REASON_PHRASE_INTERNAL_SERVER_ERROR"
      else -> "$HTTP_1_1 $statusCode"
    }
  }

  companion object {
    fun ok(): HttpResponse {
      return HttpResponse(200)
    }

    fun badRequest(): HttpResponse {
      return HttpResponse(400)
    }

    fun notFound(): HttpResponse {
      return HttpResponse(404)
    }

    fun internalServerError(): HttpResponse {
      return HttpResponse(500)
    }

    fun internalServerError(msg: String): HttpResponse {
      return HttpResponse(500, body = msg.toByteArray())
    }

    fun staticHtml(resourcePath: String): HttpResponse {
      val resource = ResourceLoader.loadResourceAsString(resourcePath)
      return if (resource.isNotEmpty()) {
        ok().html(resource.toByteArray())
      } else {
        notFound()
      }
    }

    fun html(html: String): HttpResponse {
      return ok().html(html.toByteArray())
    }

    fun html(html: ByteArray): HttpResponse {
      return ok().html(html)
    }
  }

  private fun addHeader(key: String, value: String) {
    headers = headers.plus(Pair(key, value)).toMutableMap()
  }

  fun addMinimalHeaders() {
    if (!headers.containsKey("Connection")) {
      addHeader("Connection", "close")
    }
    if (!headers.containsKey(SERVER_HEADER_KEY)) {
      addHeader(SERVER_HEADER_KEY, "kotlin-minimal")
    }
    if (!headers.containsKey(CONTENT_TYPE_HEADER_KEY)) {
      addHeader(CONTENT_TYPE_HEADER_KEY, DEFAULT_CONTENT_TYPE)
    }
    if (!headers.containsKey(CONTENT_LENGTH_HEADER_KEY)) {
      val contentLength = body?.size ?: 0
      addHeader(CONTENT_LENGTH_HEADER_KEY, contentLength.toString())
    }
    if (!headers.containsKey(DATE_HEADER_KEY)) {
      addHeader(DATE_HEADER_KEY, getCurrentDateTimeFormatted())
    }
  }

  private fun getCurrentDateTimeFormatted(): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
    val dateTime = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC)
    return dateTime.format(formatter)
  }

  fun contentType(contentType: ContentType): HttpResponse {
    val newHeaders = headers.toMutableMap()
    newHeaders[CONTENT_TYPE_HEADER_KEY] = contentType.value
    return copy(headers = newHeaders, contentType = contentType)
  }

  fun header(key: String, value: String): HttpResponse {
    val newHeaders = headers.toMutableMap()
    newHeaders[key] = value
    val contentType = ContentType.fromHeader(value)
    return copy(headers = newHeaders, contentType = contentType)
  }

  fun headers(headers: Map<String, String>): HttpResponse {
    val newHeaders = this.headers.toMutableMap()
    newHeaders.putAll(headers)
    return copy(headers = newHeaders)
  }

  fun body(body: ByteArray): HttpResponse {
    return copy(body = body)
  }

  fun html(html: ByteArray): HttpResponse {
    return copy(body = html).contentType(ContentType.HTML)
  }

  fun text(text: ByteArray): HttpResponse {
    return copy(body = text).contentType(ContentType.TEXT)
  }

  fun css(css: ByteArray): HttpResponse {
    return copy(body = css).contentType(ContentType.CSS)
  }

  fun json(json: ByteArray): HttpResponse {
    return copy(body = json).contentType(ContentType.JSON)
  }


  fun writeToOutput(outputStream: OutputStream) {
    ByteArrayOutputStream().use { buffer ->
      // Write status line
      buffer.write("${statusLine()}$NEW_LINE".toByteArray(UTF_8))

      // Write headers
      for ((key, value) in headers.toSortedMap()) {
        buffer.write("$key: $value$NEW_LINE".toByteArray(UTF_8))
      }

      // write body if present
      body?.let {
        // Header and body separator
        buffer.write(NEW_LINE.toByteArray(UTF_8))
        // Write body
        buffer.write(it)
      }

      // Write the complete response to the outputStream1
      outputStream.write(buffer.toByteArray())
    }
    outputStream.flush()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as HttpResponse

    if (statusCode != other.statusCode) return false
    if (contentType != other.contentType) return false
    if (headers != other.headers) return false
    if (body != null) {
      if (other.body == null) return false
      if (!body.contentEquals(other.body)) return false
    } else if (other.body != null) return false

    return true
  }

  override fun hashCode(): Int {
    var result = statusCode ?: 0
    result = 31 * result + (contentType?.hashCode() ?: 0)
    result = 31 * result + headers.hashCode()
    result = 31 * result + (body?.contentHashCode() ?: 0)
    return result
  }
}
package http.server

const val DEFAULT_CHARSET = "UTF-8"
const val SERVER_HEADER_KEY = "Server"
const val CONTENT_LENGTH_HEADER_KEY = "Content-Length"
const val CONTENT_TYPE_HEADER_KEY = "Content-Type"
const val DATE_HEADER_KEY = "Date"
const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
const val CONTENT_TYPE_HTML = "text/html; charset=$DEFAULT_CHARSET"
const val CONTENT_TYPE_PLAIN_TEXT = "text/plain; charset=$DEFAULT_CHARSET"
const val CONTENT_TYPE_JSON = "application/json; charset=$DEFAULT_CHARSET"
const val CONTENT_TYPE_CSS = "text/css; charset=$DEFAULT_CHARSET"
const val DEFAULT_CONTENT_TYPE = CONTENT_TYPE_JSON

enum class ContentType(val value: String, val extensions: Set<String>? = null) {
  CSS(CONTENT_TYPE_CSS, setOf("css")),
  HTML(CONTENT_TYPE_HTML, setOf("html", "htm")),
  JSON(CONTENT_TYPE_JSON),
  FORM(CONTENT_TYPE_FORM),
  TEXT(CONTENT_TYPE_PLAIN_TEXT, setOf("txt"));

  companion object {
    fun fromHeader(headerValue: String): ContentType = when {
      headerValue.contains("text/html") -> HTML
      headerValue.contains("text/plain") -> TEXT
      headerValue.contains("application/json") -> JSON
      headerValue.contains("text/css") -> CSS
      headerValue.contains(CONTENT_TYPE_FORM) -> FORM
      else -> throw IllegalArgumentException("Unsupported content type: $headerValue")
    }

    fun fromExtension(extension: String): ContentType = entries.find { it.extensions?.contains(extension) == true }
      ?: throw IllegalArgumentException("Unsupported file extension: $extension")
  }
}

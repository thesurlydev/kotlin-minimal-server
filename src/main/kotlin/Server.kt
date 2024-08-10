import http.server.HTTP_1_1
import http.server.HttpRequest
import http.server.HttpRequestParser
import http.server.HttpRequestParsingException
import http.server.HttpResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import middleware.Middleware
import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

@Suppress("Unused")
class Server(private val port: Int) : CoroutineScope {
  private lateinit var serverSocket: ServerSocket
  private val serverJob = Job()
  private val middlewares = mutableListOf<Middleware>()

  fun add(middleware: Middleware): Server {
    this.middlewares.add(middleware)
    return this
  }

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + serverJob

  fun start() {
    launch {
      serverSocket = ServerSocket(port)
      while (isActive) {
        try {
          val clientSocket = serverSocket.accept()
          launch { handleRequest(clientSocket) }
        } catch (e: Exception) {
          logger.error(e) { "Server exception: ${e.message}" }
        }
      }
    }
  }

  private suspend fun handleRequest(clientSocket: Socket) {
    withContext(Dispatchers.IO) {
      try {
        clientSocket.use { socket ->
          try {
            val inputStream = socket.getInputStream()
            val httpRequest: HttpRequest? = HttpRequestParser.parse(inputStream)
            if (httpRequest == null) {
              val s = inputStream.bufferedReader().use { it.readText() }
              logger.warn { "Failed to parse request: $s" }
              return@use
            }
            val httpResponse = HttpResponse()

            fun next(index: Int) {
              if (index < middlewares.size) {
                middlewares[index].handleRequest(httpRequest, httpResponse) {
                  next(index + 1)
                }
              }
            }

            next(0)

            httpResponse.addMinimalHeaders()
            httpResponse.writeToOutput(socket.getOutputStream())

          } catch (e: Exception) {
            logger.error(e) { "Error handling request: ${e.message}" }
            when (e) {
              is HttpRequestParsingException -> sendErrorResponse(
                socket.getOutputStream(),
                400,
                "Bad Request"
              )

              else -> sendErrorResponse(socket.getOutputStream(), 500, "Internal Server Error")
            }
          }
        }
      } catch (e: IOException) {
        logger.error(e) { "I/O error with client socket: ${e.message}" }
      } catch (e: Exception) {
        logger.error(e) { "Unexpected error: ${e.message}" }
      }
    }
  }

  private fun sendErrorResponse(outputStream: OutputStream, statusCode: Int, statusMessage: String) {
    val response =
      "$HTTP_1_1 $statusCode $statusMessage\r\n$CONTENT_LENGTH_HEADER_KEY: 0\r\nConnection: close\r\n\r\n"
    try {
      outputStream.write(response.toByteArray())
      outputStream.flush()
    } catch (e: IOException) {
      logger.error(e) { "Failed to send error response: ${e.message}" }
    }
  }

  fun stop() {
    serverJob.cancel()
    serverSocket.close()
    logger.info { "Server stopped" }
  }
}
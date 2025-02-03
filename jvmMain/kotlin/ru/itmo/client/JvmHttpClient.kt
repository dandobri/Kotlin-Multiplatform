package ru.itmo.client

import java.net.http.HttpClient as JHttpClient
import java.net.http.HttpRequest as JHttpRequest
import java.net.http.HttpResponse as JHttpResponse
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JvmHttpClient : HttpClient {

    private val client = JHttpClient.newBuilder().build()

    override suspend fun request(method: HttpMethod, request: HttpRequest): HttpResponse {
        val builder = JHttpRequest.newBuilder(URI.create(request.url))
        request.headers.value.forEach { (key, value) -> builder.header(key, value) }
        when (method) {
            HttpMethod.GET -> builder.GET()
            HttpMethod.POST -> builder.POST(JHttpRequest.BodyPublishers.ofByteArray(request.body ?: byteArrayOf(0)))
            HttpMethod.PUT -> builder.PUT(JHttpRequest.BodyPublishers.ofByteArray(request.body ?: byteArrayOf(0)))
            HttpMethod.DELETE -> builder.DELETE()
        }
        val response = withContext(Dispatchers.IO) {
            client.send(builder.build(), JHttpResponse.BodyHandlers.ofByteArray())
        }
        return HttpResponse(
            status = HttpStatus(response.statusCode()),
            headers = HttpHeaders(response.headers().map().mapValues { it.value.joinToString(",") }),
            body = response.body()
        )
    }

    override fun close() {
        // no recourses for close
    }
}

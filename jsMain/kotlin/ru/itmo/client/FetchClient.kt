package ru.itmo.client

import kotlin.js.Promise
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

private enum class Platform { Node, Browser }

private val platform: Platform
    get() {
        val hasNodeApi = js(
            """
            (typeof process !== 'undefined' 
                && process.versions != null 
                && process.versions.node != null) ||
            (typeof window !== 'undefined' 
                && typeof window.process !== 'undefined' 
                && window.process.versions != null 
                && window.process.versions.node != null)
            """
        ) as Boolean
        return if (hasNodeApi) Platform.Node else Platform.Browser
    }

private val nodeFetch: dynamic
    get() = js("eval('require')('node-fetch')")

private fun RequestInit.asNodeOptions(): dynamic =
    js("Object").assign(js("Object").create(null), this)

class JSHttpClient : HttpClient {
    override suspend fun request(method: HttpMethod, request: HttpRequest): HttpResponse {
        return when (platform) {
            Platform.Node -> performNodeFetch(method, request)
            Platform.Browser -> performBrowserFetch(method, request)
        }
    }
    private suspend fun performNodeFetch(method: HttpMethod, request: HttpRequest): HttpResponse {
        val header = createHeaders(request)
        val body = createBody(request)
        val response = nodeFetch(request.url, RequestInit(
            method = method.name,
            headers = header,
            body = body
        ).asNodeOptions()).unsafeCast<Promise<dynamic>>().await()
        return createResponse(response)
    }

    private suspend fun performBrowserFetch(method: HttpMethod, request: HttpRequest): HttpResponse {
        val header = createHeaders(request)
        val body = createBody(request)
        val response = kotlinx.browser.window.fetch(
            request.url,
            RequestInit(
                method = method.name,
                headers = header,
                body = body
            )
        ).unsafeCast<Promise<dynamic>>().await()
        return createResponse(response)
    }
    private fun createHeaders(request: HttpRequest): Headers {
        val header = Headers()
        request.headers.value.forEach { (key, value) -> header.append(key, value) }
        return header
    }
    private fun createBody(request: HttpRequest): Int8Array? {
        return request.body?.let { Int8Array(it.toTypedArray()) }
    }
    private suspend fun createResponse(response: dynamic): HttpResponse {
        val responseHeaders = buildMap {
            response.headers.forEach { key, value ->
                put(key as String, value as String)
            }
        }
        val responseBody = response.text().unsafeCast<Promise<String>>().await()
        return HttpResponse(
            status = HttpStatus(response.status as Int),
            headers = HttpHeaders(responseHeaders),
            body = responseBody.encodeToByteArray(),
        )
    }
    override fun close() {
        // no recourses to close
    }
}

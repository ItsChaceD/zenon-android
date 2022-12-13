package network.zenon.android.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

@OptIn(KtorExperimentalAPI::class)
class WsClient: Client {
    private var httpClient: HttpClient? = null
    private var wsClient: DefaultClientWebSocketSession? = null
    private var listeners = mutableListOf<RequestListener>()

    override fun connect(url: String) {
        httpClient = HttpClient(CIO) {
            engine {
                requestTimeout = 5000
            }
            install(WebSockets)
        }
        runBlocking {
            httpClient?.ws(
                method = HttpMethod.Get,
                host = url,
                path = "/"
            ) {
                wsClient = this
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val jsonObject = JSONObject(frame.readText())
                        val method = jsonObject.getString("method")
                        val params = jsonObject.get("params")
                        listeners.forEach { it.handleRequest(method, params) }
                    }
                }
            }
        }
    }

    override fun close() {
        wsClient?.outgoing?.close()
        httpClient?.close()
    }

    override fun sendRequest(method: String, params: Any): Any? {
        if (wsClient == null) {
            return null
        }
        val jsonObject = JSONObject()
        jsonObject.put("method", method)
        jsonObject.put("params", params)
        val request = jsonObject.toString()
        var response: Any? = null
        runBlocking {
            wsClient?.send(Frame.Text(request))
            val frame = wsClient?.incoming?.receive()
            if (frame is Frame.Text) {
                response = JSONObject(frame.readText()).get("result")
            }
        }
        return response
    }

    override fun sendNotification(method: String, params: Any) {
        if (wsClient == null) {
            return
        }
        val jsonObject = JSONObject()
        jsonObject.put("method", method)
        jsonObject.put("params", params)
        val request = jsonObject.toString()
        runBlocking {
            wsClient?.send(Frame.Text(request))
        }
    }

    override fun addListener(toAdd: RequestListener) {
        if (!listeners.contains(toAdd)) {
            listeners.add(toAdd)
        }
    }

    override fun removeListener(toRemove: RequestListener) {
        listeners.remove(toRemove)
    }
}
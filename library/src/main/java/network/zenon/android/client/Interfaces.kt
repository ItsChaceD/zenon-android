package network.zenon.android.client

interface Client {
    fun connect(url: String)
    fun close()
    fun sendRequest(method: String, params: Any): Any?
    fun sendNotification(method: String, params: Any)
    fun addListener(toAdd: RequestListener)
    fun removeListener(toRemove: RequestListener)
}

interface RequestListener {
    fun handleRequest(method: String, params: Any)
}

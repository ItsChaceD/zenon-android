package network.zenon.android.client

import android.net.Uri

fun validateWsConnectionURL(url: String): Boolean {
    val uri = Uri.parse(url)
    return (uri != null) &&
            uri.isAbsolute &&
            uri.port != -1 &&
            (uri.scheme == "ws" || uri.scheme == "wss")
}
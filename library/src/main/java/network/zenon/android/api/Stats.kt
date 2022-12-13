package network.zenon.android.api

import network.zenon.android.client.Client
import network.zenon.android.model.NetworkInfo
import network.zenon.android.model.OsInfo
import network.zenon.android.model.ProcessInfo
import network.zenon.android.model.SyncInfo
import org.json.JSONObject

class StatsApi {
    lateinit var client: Client

    fun osInfo(): OsInfo {
        val response = client.sendRequest("stats.osInfo", emptyList<Any>())
        return OsInfo.fromJson(response as JSONObject)
    }

    fun processInfo(): ProcessInfo {
        val response = client.sendRequest("stats.processInfo", emptyList<Any>())
        return ProcessInfo.fromJson(response as JSONObject)
    }

    fun networkInfo(): NetworkInfo {
        val response = client.sendRequest("stats.networkInfo", emptyList<Any>())
        return NetworkInfo.fromJson(response as JSONObject)
    }

    fun syncInfo(): SyncInfo {
        val response = client.sendRequest("stats.syncInfo", emptyList<Any>())
        return SyncInfo.fromJson(response as JSONObject)
    }
}
package network.zenon.android.model

import org.json.JSONObject

data class Peer(var publicKey: String, var ip: String) {
    companion object {
        fun fromJson(jsonObject: JSONObject) : Peer {
            return Peer(
                publicKey = jsonObject.getString("publicKey"),
                ip = jsonObject.getString("ip")
            )
        }
    }
}

data class NetworkInfo(var numPeers: Int, var self: Peer, var peers: List<Peer>) {
    companion object {
        fun fromJson(jsonObject: JSONObject) : NetworkInfo {
            val numPeers = jsonObject.getInt("numPeers")
            val self = Peer.fromJson(jsonObject.getJSONObject("self"))
            val peers = mutableListOf<Peer>()
            val peersJsonArray = jsonObject.getJSONArray("peers")
            for (i in 0 until peersJsonArray.length()) {
                val peerJsonObject = peersJsonArray.getJSONObject(i)
                peers.add(Peer.fromJson(peerJsonObject))
            }

            return NetworkInfo(
                numPeers = numPeers,
                self = self,
                peers = peers
            )
        }
    }
}

data class ProcessInfo(val commit: String, val version: String) {
    companion object {
        fun fromJson(jsonObject: JSONObject) : ProcessInfo {
            return ProcessInfo(
                commit = jsonObject.getString("commit"),
                version = jsonObject.getString("version")
            )
        }
    }
}

data class OsInfo(
    var os: String,
    var platform: String,
    var platformVersion: String,
    var kernelVersion: String,
    var memoryTotal: Int,
    var memoryFree: Int,
    var numCPU: Int,
    var numGoroutine: Int
) {
    companion object {
        fun fromJson(jsonObject: JSONObject) : OsInfo {
            return OsInfo(
                os =jsonObject.getString("os"),
                platform = jsonObject.getString("platform"),
                platformVersion = jsonObject.getString("platformVersion"),
                kernelVersion = jsonObject.getString("kernelVersion"),
                memoryTotal = jsonObject.getInt("memoryTotal"),
                memoryFree = jsonObject.getInt("memoryFree"),
                numCPU = jsonObject.getInt("numCPU"),
                numGoroutine = jsonObject.getInt("numGoroutine")
            )
        }
    }
}

enum class SyncState {
    UNKNOWN,
    SYNCING,
    SYNC_DONE,
    NOT_ENOUGH_PEERS
}

data class SyncInfo(var state: SyncState, var currentHeight: Int, var targetHeight: Int) {
    companion object {
        fun fromJson(jsonObject: JSONObject) : SyncInfo {
            val state: SyncState = when (jsonObject.getString("state")) {
                "UNKNOWN" -> SyncState.UNKNOWN
                "SYNCING" -> SyncState.SYNCING
                "SYNC_DONE" -> SyncState.SYNC_DONE
                "NOT_ENOUGH_PEERS" -> SyncState.NOT_ENOUGH_PEERS
                else -> SyncState.UNKNOWN
            }

            return SyncInfo(
                state = state,
                currentHeight = jsonObject.getInt("currentHeight"),
                targetHeight = jsonObject.getInt("targetHeight")
            )
        }
    }
}
package network.zenon.android.model.embedded

import network.zenon.android.model.primitives.Address
import org.json.JSONObject

data class SentinelInfo(
    var owner: Address,
    var registrationTimestamp: Int,
    var isRevocable: Boolean,
    var revokeCooldown: Int,
    var active: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): SentinelInfo {
            return SentinelInfo(
                owner = Address.parse(json.getString("owner")),
                registrationTimestamp = json.getInt("registrationTimestamp"),
                isRevocable = json.getBoolean("isRevocable"),
                revokeCooldown = json.getInt("revokeCooldown"),
                active = json.getBoolean("active")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("owner", owner.toString())
        json.put("registrationTimestamp", registrationTimestamp)
        json.put("isRevocable", isRevocable)
        json.put("revokeCooldown", revokeCooldown)
        json.put("active", active)
        return json
    }
}

data class SentinelInfoList(
    var count: Int,
    var list: List<SentinelInfo>
) {
    companion object {
        fun fromJson(json: JSONObject): SentinelInfoList {
            val listJson = json.getJSONArray("list")
            val list = mutableListOf<SentinelInfo>()
            for(i in 0 until listJson.length()) {
                list.add(SentinelInfo.fromJson(listJson.getJSONObject(i)))
            }

            return SentinelInfoList(
                count = json.getInt("count"),
                list = list
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        val mappedList = list.map { it.toJson() }
        json.put("count", count)
        json.put("list", mappedList)
        return json
    }
}
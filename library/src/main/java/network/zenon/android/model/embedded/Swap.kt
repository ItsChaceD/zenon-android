package network.zenon.android.model.embedded

import network.zenon.android.model.primitives.Hash
import org.json.JSONObject

data class SwapAssetEntry(
    var keyIdHash: Hash,
    var qsr: Int,
    var znn: Int
) {
    companion object {
        fun fromJson(keyIdHash: Hash, json: JSONObject): SwapAssetEntry {
            return SwapAssetEntry(
                keyIdHash = keyIdHash,
                qsr = json.getInt("qsr"),
                znn = json.getInt("znn")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("keyIdHash", keyIdHash.toString())
        json.put("qsr", qsr)
        json.put("znn", znn)
        return json
    }

    fun hasBalance(): Boolean {
        return qsr > 0 || znn > 0
    }
}

data class SwapLegacyPillarEntry(
    var numPillars: Int,
    var keyIdHash: Hash
) {
    companion object {
        fun fromJson(json: JSONObject): SwapLegacyPillarEntry {
            return SwapLegacyPillarEntry(
                numPillars = json.getInt("numPillars"),
                keyIdHash = Hash.parse(json.getString("keyIdHash"))
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("numPillars", numPillars)
        json.put("keyIdHash", keyIdHash.toString())
        return json
    }
}

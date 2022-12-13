package network.zenon.android.model.primitives

import network.zenon.android.utils.BytesUtils
import org.json.JSONObject

val EMPTY_HASH_HEIGHT = HashHeight(EMPTY_HASH, 0)

data class HashHeight(
    var hash: Hash?,
    var height: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): HashHeight {
            return HashHeight(
                hash = Hash.parse(json.getString("hash")),
                height = json.getInt("height")
            )
        }
    }

    fun toJson(): JSONObject {
        val data = JSONObject()
        data.put("hash", hash.toString())
        data.put("height", height)
        return data
    }

    fun getBytes(): ByteArray {
        return hash!!.hash + BytesUtils.intToBytes(height!!)
    }

    override fun toString(): String {
        return toJson().toString()
    }
}
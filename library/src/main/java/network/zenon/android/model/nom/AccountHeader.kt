package network.zenon.android.model.nom

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import org.json.JSONObject

data class AccountHeader(
    // Added here for simplicity. Is not part of the RPC response
    var address: Address? =  null,
    var hash: Hash?,
    var height: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): AccountHeader {
            return AccountHeader(
                address = Address.parse(json.optString("address")),
                hash = Hash.parse(json.optString("hash")),
                height = json.optInt("height")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("address", address.toString())
        json.put("hash", hash.toString())
        json.put("height", height)
        return json
    }

    override fun toString(): String {
        return toJson().toString()
    }
}
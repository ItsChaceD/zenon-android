package network.zenon.android.model.embedded

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import org.json.JSONObject

data class StakeList(
    var totalAmount: Int,
    var totalWeightedAmount: Int,
    var count: Int,
    var list: List<StakeEntry>
) {
    companion object {
        fun fromJson(json: JSONObject): StakeList {
            val listJson = json.getJSONArray("list")
            val list = mutableListOf<StakeEntry>()
            for(i in 0 until listJson.length()) {
                list.add(StakeEntry.fromJson(listJson.getJSONObject(i)))
            }

            return StakeList(
                totalAmount = json.getInt("totalAmount"),
                totalWeightedAmount = json.getInt("totalWeightedAmount"),
                count = json.getInt("count"),
                list = list
            )
        }
    }
}

data class StakeEntry(
    val amount: Int,
    val weightedAmount: Int,
    val startTimestamp: Int,
    val expirationTimestamp: Int,
    val address: Address,
    val id: Hash
) {
    companion object {
        fun fromJson(json: JSONObject): StakeEntry {
            return StakeEntry(
                amount = json.getInt("amount"),
                weightedAmount = json.getInt("weightedAmount"),
                startTimestamp = json.getInt("startTimestamp"),
                expirationTimestamp = json.getInt("expirationTimestamp"),
                address = Address.parse(json.getString("address")),
                id = Hash.parse(json.getString("id")),
            )
        }
    }
}
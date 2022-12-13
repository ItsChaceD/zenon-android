package network.zenon.android.model.embedded

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import org.json.JSONObject

data class UncollectedReward(
    val address: Address,
    val znnAmount: Int,
    val qsrAmount: Int
) {
    companion object {
        fun fromJson(json: JSONObject): UncollectedReward {
            return UncollectedReward(
                address = Address.parse(json.getString("address")),
                znnAmount = json.getInt("znnAmount"),
                qsrAmount = json.getInt("qsrAmount")
            )
        }
    }
}

data class RewardHistoryEntry(
    val epoch: Int,
    val znnAmount: Int,
    val qsrAmount: Int
) {
    companion object {
        fun fromJson(json: JSONObject): RewardHistoryEntry {
            return RewardHistoryEntry(
                epoch = json.getInt("epoch"),
                znnAmount = json.getInt("znnAmount"),
                qsrAmount = json.getInt("qsrAmount")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("epoch", epoch)
        json.put("znnAmount", znnAmount)
        json.put("qsrAmount", qsrAmount)
        return json
    }
}

data class RewardHistoryList(
    var count: Int,
    var list: List<RewardHistoryEntry>
) {
    companion object {
        fun fromJson(json: JSONObject): RewardHistoryList {
            val count = json.optInt("count")
            val list = mutableListOf<RewardHistoryEntry>()
            val listJson = json.optJSONArray("list")

            if(null != listJson) {
                for(i in 0 until listJson.length()) {
                    list.add(RewardHistoryEntry.fromJson(listJson.getJSONObject(i)))
                }
            }

            return RewardHistoryList(
                count = count,
                list = list.toList()
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("count", count)
        val mappedList = list.map { it.toJson() }
        json.put("list", mappedList)
        return json
    }
}

data class VoteBreakdown(
    var id: Hash,
    var yes: Int,
    var no: Int,
    var total: Int
) {
    companion object {
        fun fromJson(json: JSONObject): VoteBreakdown {
            return VoteBreakdown(
                id = Hash.parse(json.getString("id")),
                yes = json.getInt("yes"),
                no = json.getInt("no"),
                total = json.getInt("total")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id.toString())
        json.put("yes", yes)
        json.put("no", no)
        json.put("total", total)
        return json
    }

    override fun toString(): String {
        return toJson().toString()
    }
}

data class PillarVote(
    var id: Hash,
    var name: String,
    var vote: Int
) {
    companion object {
        fun fromJson(json: JSONObject): PillarVote {
            return PillarVote(
                id = Hash.parse(json.getString("id")),
                name = json.getString("name"),
                vote = json.getInt("vote")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id.toString())
        json.put("name", name)
        json.put("vote", vote)
        return json
    }
}
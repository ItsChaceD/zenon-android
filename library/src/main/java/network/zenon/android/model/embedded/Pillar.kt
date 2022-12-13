package network.zenon.android.model.embedded

import network.zenon.android.model.primitives.Address
import org.json.JSONObject

class PillarInfo(
    var name: String,
    var rank: Int,
    var type: Int,
    var ownerAddress: Address,
    var producerAddress: Address,
    var withdrawAddress: Address,
    var giveMomentumRewardPercentage: Int,
    var giveDelegateRewardPercentage: Int,
    var isRevocable: Boolean,
    var revokeCooldown: Int,
    var revokeTimestamp: Int,
    var currentStats: PillarEpochStats,
    var weight: Int,
    var producedMomentums: Int,
    var expectedMomentums: Int,
) {
    companion object {
        private const val UNKNOWN_TYPE = 0
        private const val LEGACY_PILLAR_TYPE = 1
        private const val REGULAR_PILLAR_TYPE = 2
        
        fun fromJson(json: JSONObject): PillarInfo {
            val currentStats = PillarEpochStats.fromJson(json.getJSONObject("currentStats"))
            return PillarInfo(
                name = json.getString("name"),
                rank = json.getInt("rank"),
                type = json.optInt("type", UNKNOWN_TYPE),
                ownerAddress = Address.parse(json.getString("ownerAddress")),
                producerAddress = Address.parse(json.getString("producerAddress")),
                withdrawAddress = Address.parse(json.getString("withdrawAddress")),
                giveMomentumRewardPercentage = json.getInt("giveMomentumRewardPercentage"),
                giveDelegateRewardPercentage = json.getInt("giveDelegateRewardPercentage"),
                isRevocable = json.getBoolean("isRevocable"),
                revokeCooldown = json.getInt("revokeCooldown"),
                revokeTimestamp = json.getInt("revokeTimestamp"),
                currentStats = currentStats,
                weight = json.getInt("weight"),
                producedMomentums = currentStats.producedMomentums,
                expectedMomentums = currentStats.expectedMomentums
            )
        }
    }
    
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("rank", rank)
        json.put("type", type)
        json.put("ownerAddress", ownerAddress.toString())
        json.put("producerAddress", producerAddress.toString())
        json.put("withdrawAddress", withdrawAddress.toString())
        json.put("giveMomentumRewardPercentage", giveMomentumRewardPercentage)
        json.put("giveDelegateRewardPercentage", giveDelegateRewardPercentage)
        json.put("isRevocable", isRevocable)
        json.put("revokeCooldown", revokeCooldown)
        json.put("revokeTimestamp", revokeTimestamp)
        json.put("currentStats", currentStats.toJson())
        json.put("weight", weight)
        return json
    }
}

data class PillarInfoList(
    var count: Int,
    var list: List<PillarInfo>
) {
    companion object {
        fun fromJson(json: JSONObject): PillarInfoList {
            val listJson = json.getJSONArray("list")
            val list = mutableListOf<PillarInfo>()
            for(i in 0 until listJson.length()) {
                list.add(PillarInfo.fromJson(listJson.getJSONObject(i)))
            }

            return PillarInfoList(
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

data class PillarEpochStats(
    var producedMomentums: Int,
    var expectedMomentums: Int
) {
    companion object {
        fun fromJson(json: JSONObject): PillarEpochStats {
            return PillarEpochStats(
                producedMomentums = json.getInt("producedMomentums"),
                expectedMomentums = json.getInt("expectedMomentums")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("producedMomentums", producedMomentums)
        json.put("expectedMomentums", expectedMomentums)
        return json
    }
}

data class PillarEpochHistory(
    var name: String,
    var epoch: Int,
    var giveBlockRewardPercentage: Int,
    var giveDelegateRewardPercentage: Int,
    var producedBlockNum: Int,
    var expectedBlockNum: Int,
    var weight: Int
) {
    companion object {
        fun fromJson(json: JSONObject): PillarEpochHistory {
            return PillarEpochHistory(
                name = json.getString("name"),
                epoch = json.getInt("epoch"),
                giveBlockRewardPercentage = json.getInt("giveBlockRewardPercentage"),
                giveDelegateRewardPercentage = json.getInt("giveDelegateRewardPercentage"),
                producedBlockNum = json.getInt("producedBlockNum"),
                expectedBlockNum = json.getInt("expectedBlockNum"),
                weight = json.getInt("weight")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("epoch", epoch)
        json.put("giveBlockRewardPercentage", giveBlockRewardPercentage)
        json.put("giveDelegateRewardPercentage", giveDelegateRewardPercentage)
        json.put("producedBlockNum", producedBlockNum)
        json.put("expectedBlockNum", expectedBlockNum)
        json.put("weight", weight)
        return json
    }
}

data class PillarEpochHistoryList(
    var count: Int,
    var list: List<PillarEpochHistory>
) {
    companion object {
        fun fromJson(json: JSONObject): PillarEpochHistoryList {
            val listJson = json.getJSONArray("list")
            val list = mutableListOf<PillarEpochHistory>()
            for(i in 0 until listJson.length()) {
                list.add(PillarEpochHistory.fromJson(listJson.getJSONObject(i)))
            }

            return PillarEpochHistoryList(
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

data class DelegationInfo(
    var name: String,
    var status: Int,
    var weight: Int,
    var weightWithDecimals: Number? = null
) {
    companion object {
        fun fromJson(json: JSONObject): DelegationInfo {
            return DelegationInfo(
                name = json.getString("name"),
                status = json.getInt("status"),
                weight = json.getInt("weight")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("status", status)
        json.put("weight", weight)
        return json
    }

    fun isPillarActive(): Boolean {
        return status == 1
    }
}
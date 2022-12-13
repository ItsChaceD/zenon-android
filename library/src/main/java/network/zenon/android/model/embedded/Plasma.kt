package network.zenon.android.model.embedded

import network.zenon.android.model.nom.BlockTypeEnum
import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import network.zenon.android.utils.BytesUtils
import org.json.JSONObject

data class FusionEntryList(
    var qsrAmount: Int,
    var count: Int,
    var list: List<FusionEntry>
) {
    companion object {
        fun fromJson(json: JSONObject): FusionEntryList {
            val listJson = json.getJSONArray("list")
            val list = mutableListOf<FusionEntry>()
            for(i in 0 until listJson.length()) {
                list.add(FusionEntry.fromJson(listJson.getJSONObject(i)))
            }

            return FusionEntryList(
                qsrAmount = json.getInt("qsrAmount"),
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
        json.put("qsrAmount", qsrAmount)
        return json
    }
}

data class FusionEntry(
    var qsrAmount: Int,
    var beneficiary: Address,
    var expirationHeight: Int,
    var id: Hash,
    var isRevocable: Boolean? = null
) {
    companion object {
        fun fromJson(json: JSONObject): FusionEntry {
            return FusionEntry(
                qsrAmount = json.getInt("qsrAmount"),
                beneficiary = Address.parse(json.getString("beneficiary")),
                expirationHeight = json.getInt("expirationHeight"),
                id = Hash.parse(json.getString("id")),
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("qsrAmount", qsrAmount)
        json.put("beneficiary", beneficiary.toString())
        json.put("expirationHeight", expirationHeight)
        json.put("id", id.toString())
        return json
    }
}

data class PlasmaInfo(
    val currentPlasma: Int,
    val maxPlasma: Int,
    val qsrAmount: Int
) {
    companion object {
        fun fromJson(json: JSONObject): PlasmaInfo {
            return PlasmaInfo(
                currentPlasma = json.getInt("currentPlasma"),
                maxPlasma = json.getInt("maxPlasma"),
                qsrAmount = json.getInt("qsrAmount"),
            )
        }
    }
}

class GetRequiredParam(
    var address: Address,
    var blockType: BlockTypeEnum,
    var toAddress: Address?,
    var data: ByteArray?
) {
    init {
        if(blockType == BlockTypeEnum.USER_RECEIVE) {
            toAddress = address
        }
    }

    companion object {
        fun fromJson(json: JSONObject): GetRequiredParam {
            val toAddress = json.optString("toAddress")
            val data = json.optString("data")

            return GetRequiredParam(
                address = Address.parse(json.getString("address")),
                blockType = BlockTypeEnum.values().getOrElse(
                    json.optInt("blockType", -1)
                ) { BlockTypeEnum.UNKNOWN },
                toAddress = if(toAddress.isNotEmpty()) Address.parse(toAddress) else null,
                data = if(data.isNotEmpty()) BytesUtils.base64ToBytes(data) else null
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("address", address.toString())
        json.put("blockType", blockType.ordinal)

        if(null != toAddress) {
            json.put("toAddress", toAddress.toString())
        }

        if(null != data) {
            json.put("data", BytesUtils.bytesToBase64(data!!))
        }

        return json
    }

    override fun toString(): String {
        return toJson().toString()
    }
}

data class GetRequiredResponse(
    var availablePlasma: Int,
    var basePlasma: Int,
    var requiredDifficulty: Int
) {
    companion object {
        fun fromJson(json: JSONObject): GetRequiredResponse {
            return GetRequiredResponse(
                availablePlasma = json.getInt("availablePlasma"),
                basePlasma = json.getInt("basePlasma"),
                requiredDifficulty = json.getInt("requiredDifficulty")
            )
        }
    }
}
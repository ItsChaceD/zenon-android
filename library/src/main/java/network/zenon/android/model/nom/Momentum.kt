package network.zenon.android.model.nom

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import network.zenon.android.utils.BytesUtils
import org.json.JSONObject

class Momentum(
    var version: Int,
    var chainIdentifier: Int,
    var hash: Hash,
    var previousHash: Hash,
    var height: Int,
    var timestamp: Int,
    var data: ByteArray,
    var content: List<AccountHeader>,
    var changesHash: Hash?,
    var publicKey: String,
    var signature: String,
    var producer: Address
) {
    companion object {
        fun fromJson(json: JSONObject): Momentum {
            val version = json.optInt("version")
            val chainIdentifier = json.optInt("chainIdentifier")
            val hash = Hash.parse(json.optString("hash"))
            val previousHash = Hash.parse(json.optString("previousHash"))
            val height = json.optInt("height")
            val timestamp = json.optInt("timestamp")


            val dataAsBase64 = json.optString("data")
            val data = if(dataAsBase64.isNotEmpty())
                BytesUtils.base64ToBytes(dataAsBase64)
                else byteArrayOf()

            val content = mutableListOf<AccountHeader>()

            val contentList = json.optJSONArray("content")
            if(null != contentList) {
                for (i in 0  until contentList.length()) {
                    content.add(
                        AccountHeader.fromJson(contentList.getJSONObject(i))
                    )
                }
            }

            val changesHash = Hash.parse(json.optString("changesHash"))
            val publicKey = json.optString("publicKey", "")
            val signature = json.optString("signature", "")
            val producer = Address.parse(json.optString("producer"))

            return Momentum(
                version = version,
                chainIdentifier = chainIdentifier,
                hash = hash,
                previousHash = previousHash,
                height = height,
                timestamp = timestamp,
                data = data,
                content = content.toList(),
                changesHash = changesHash,
                publicKey = publicKey,
                signature = signature,
                producer = producer,
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("version", version)
        json.put("chainIdentifier", chainIdentifier)
        json.put("hash", hash.toString())
        json.put("previousHash", previousHash.toString())
        json.put("height", height)
        json.put("timestamp", timestamp)
        json.put("data", if(data.isNotEmpty()) BytesUtils.bytesToBase64(data) else "")
        val mappedContent = content.map { it.toString() }
        json.put("content", mappedContent)
        json.put("changesHash", changesHash.toString())
        json.put("publicKey", publicKey)
        json.put("signature", signature)
        json.put("producer", producer.toString())
        return json
    }
}

data class MomentumList(
    var count: Int,
    var list: List<Momentum>
) {
    companion object {
        fun fromJson(json: JSONObject): MomentumList {
            val count = json.optInt("count")
            val list = mutableListOf<Momentum>()
            val listJson = json.optJSONArray("list")

            if(null != listJson) {
                for(i in 0 until listJson.length()) {
                    list.add(Momentum.fromJson(listJson.getJSONObject(i)))
                }
            }

            return MomentumList(
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

data class MomentumShort(
    val hash: Hash?,
    val height: Int?,
    val timestamp: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): MomentumShort {
            return MomentumShort(
                hash = Hash.parse(json.optString("hash")),
                height = json.optInt("height"),
                timestamp = json.optInt("timestamp")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("hash", hash.toString())
        json.put("height", height)
        json.put("timestamp", timestamp)
        return json
    }
}

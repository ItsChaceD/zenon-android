package network.zenon.android.model.nom

import org.json.JSONObject

class DetailedMomentum {
    lateinit var blocks: List<AccountBlock>
    lateinit var momentum: Momentum

    companion object {
        fun fromJson(json: JSONObject): DetailedMomentum {
            val detailedMomentum = DetailedMomentum()
            val blocks = mutableListOf<AccountBlock>()
            val blocksJson = json.optJSONArray("blocks")

            if(null != blocksJson) {
                for(i in 0 until blocksJson.length()) {
                    blocks.add(AccountBlock.fromJson(blocksJson.getJSONObject(i)))
                }
            }

            detailedMomentum.blocks = blocks.toList()
            detailedMomentum.momentum = Momentum.fromJson(json.getJSONObject("momentum"))
            return detailedMomentum
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        val mappedBlocks = blocks.map { it.toJson() }
        json.put("blocks", mappedBlocks)
        json.put("momentum", momentum.toJson())
        return json
    }
}

data class DetailedMomentumList(
    var count: Int?,
    var list: List<DetailedMomentum>?
) {
    companion object {
        fun fromJson(json: JSONObject): DetailedMomentumList {
            val count = json.optInt("count")
            val list = mutableListOf<DetailedMomentum>()
            val listJson = json.optJSONArray("list")

            if(null != listJson) {
                for(i in 0 until listJson.length()) {
                    list.add(DetailedMomentum.fromJson(listJson.getJSONObject(i)))
                }
            }

            return DetailedMomentumList(
                count = count,
                list = list.toList()
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("count", count)
        if(null != list) {
            val mappedList = list!!.map { it.toJson() }
            json.put("list", mappedList)
        }
        return json
    }
}

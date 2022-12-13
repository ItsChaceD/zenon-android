package network.zenon.android.model.nom

import network.zenon.android.model.primitives.Hash
import org.json.JSONObject

data class AccountBlockConfirmationDetail(
    var numConfirmations: Int,
    var momentumHeight: Int,
    var momentumHash: Hash,
    var momentumTimestamp: Int
) {

    companion object {
        fun fromJson(json: JSONObject): AccountBlockConfirmationDetail {
            return AccountBlockConfirmationDetail(
                numConfirmations = json.optInt("numConfirmations"),
                momentumHeight = json.optInt("momentumHeight"),
                momentumHash = Hash.parse(json.optString("momentumHash")),
                momentumTimestamp = json.optInt("momentumTimestamp")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("numConfirmations", numConfirmations)
        json.put("momentumHeight", momentumHeight)
        json.put("momentumHash", momentumHash.toString())
        json.put("momentumTimestamp", momentumTimestamp)
        return json
    }
}

class AccountBlock: AccountBlockTemplate() {
    var descendantBlocks: List<AccountBlock> = listOf()
    var basePlasma: Int = 0
    var usedPlasma: Int = 0
    lateinit var changesHash: Hash
    var token: Token? = null
    // Available if account-block is confirmed, null otherwise
    var confirmationDetail: AccountBlockConfirmationDetail? = null

    var pairedAccountBlock: AccountBlock? = null

    companion object: AccountBlockTemplate() {
        fun fromJson(json: JSONObject): AccountBlock {
            val block: AccountBlock = AccountBlockTemplate.fromJson(json) as AccountBlock

            val descendantBlocksJson = json.optJSONArray("descendantBlocks")
            if(null != descendantBlocksJson) {
                val tempList = mutableListOf<AccountBlock>()

                for(i in 0 until descendantBlocksJson.length()) {
                    tempList.add(fromJson(descendantBlocksJson.getJSONObject(i)))
                }

                block.descendantBlocks = tempList.toList()
            }

            block.basePlasma = json.optInt("basePlasma", block.basePlasma)
            block.usedPlasma = json.optInt("usedPlasma", block.usedPlasma)
            block.changesHash = Hash.parse(json.optString("changesHash"))

            val tokenJson = json.optJSONObject("token")
            block.token = if(null != tokenJson) Token.fromJson(tokenJson) else null

            val confirmationDetailJson = json.optJSONObject("confirmationDetail")
            block.confirmationDetail = if(null != confirmationDetailJson)
                AccountBlockConfirmationDetail.fromJson(confirmationDetailJson)
            else null

            val pairedAccountBlockJson = json.optJSONObject("pairedAccountBlock")
            block.pairedAccountBlock = if(null != pairedAccountBlockJson)
                fromJson(pairedAccountBlockJson)
                else null

            return block
        }
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()

        val descendantBlocksMap = descendantBlocks.map { it.toJson() }

        json.put("descendantBlocks", descendantBlocksMap)
        json.put("usedPlasma", usedPlasma)
        json.put("basePlasma", basePlasma)
        json.put("changesHash", changesHash.toString())

        json.put("token", if(null != token) token!!.toJson() else null)
        json.put("confirmationDetail",
            if(null != confirmationDetail) confirmationDetail!!.toJson() else null)
        json.put("pairedAccountBlock",
            if(null != pairedAccountBlock) pairedAccountBlock!!.toJson() else null)
        return json
    }

    fun isCompleted(): Boolean {
        return null != confirmationDetail
    }
}

data class AccountBlockList(
    var count: Int?,
    var list: List<AccountBlock>?,
    // If true, there are more than `count` elements, but only these can be retrieved
    var more: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): AccountBlockList {
            val count = json.optInt("count")
            val more = json.optBoolean("more")
            val list = mutableListOf<AccountBlock>()

            val jsonList = json.optJSONArray("list")

            if(null != jsonList) {
                for(i in 0 until jsonList.length()) {
                    list.add(AccountBlock.fromJson(jsonList.getJSONObject(i)))
                }
            }

            return AccountBlockList(
                count = count,
                list = list.toList(),
                more = more
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
        json.put("more", more)
        return json
    }
}
package network.zenon.android.model.nom

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.TokenStandard
import org.json.JSONObject
import kotlin.math.pow

data class Token(
    var name: String,
    var symbol: String,
    var domain: String,
    var totalSupply: Int,
    var decimals: Int,
    var owner: Address,
    var tokenStandard: TokenStandard,
    var maxSupply: Int,
    var isBurnable: Boolean,
    var isMintable: Boolean,
    var isUtility: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): Token {
            return Token(
                name = json.optString("name"),
                symbol = json.optString("symbol"),
                domain = json.optString("domain"),
                totalSupply = json.optInt("totalSupply"),
                decimals = json.optInt("decimals"),
                owner = Address.parse(json.optString("owner")),
                tokenStandard = TokenStandard.parse(json.optString("tokenStandard")),
                maxSupply = json.optInt("maxSupply"),
                isBurnable = json.optBoolean("isBurnable"),
                isMintable = json.optBoolean("isMintable"),
                isUtility = json.optBoolean("isUtility")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("symbol", symbol)
        json.put("domain", domain)
        json.put("totalSupply", totalSupply)
        json.put("decimals", decimals)
        json.put("owner", owner.toString())
        json.put("tokenStandard", tokenStandard.toString())
        json.put("maxSupply", maxSupply)
        json.put("isBurnable", isBurnable)
        json.put("isMintable", isMintable)
        json.put("isUtility", isUtility)
        return json
    }

    fun decimalsExponent(): Int {
        return 10.0.pow(decimals.toDouble()).toInt()
    }

    override fun equals(other: Any?): Boolean {
        return other is Token && other.tokenStandard == tokenStandard
    }

    override fun hashCode(): Int {
        return tokenStandard.toString().hashCode()
    }
}


data class TokenList(
    var count: Int? = null,
    var list: List<Token>? = null
) {
    companion object {
        private var count: Int? = null
        private var list: List<Token>? = null

        fun fromJson(json: JSONObject): TokenList {
            count = json.optInt("count")
            val jsonList = json.optJSONArray("list")
            if(null != jsonList) {
                val tempList = mutableListOf<Token>()

                for(i in 0 until jsonList.length()) {
                    tempList.add(Token.fromJson(jsonList.getJSONObject(i)))
                }

                list = tempList.toList()
            }

            return TokenList(count, list)
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("count", count)
        if(null != list) {
            json.put("list", list!!.map { it.toJson() })
        }
        return json
    }
}
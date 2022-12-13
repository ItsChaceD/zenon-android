package network.zenon.android.model.nom

import network.zenon.android.model.primitives.QSR_ZTS
import network.zenon.android.model.primitives.TokenStandard
import network.zenon.android.model.primitives.ZNN_ZTS
import network.zenon.android.utils.AmountUtils
import org.json.JSONObject

class AccountInfo(
    var address: String?,
    var blockCount: Int?,
    var balanceInfoList: List<BalanceInfoListItem>?
) {
    companion object {
        fun fromJson(json: JSONObject): AccountInfo {
            val address = json.optString("address")
            val blockCount = json.optInt("accountHeight")
            val balanceInfoList = mutableListOf<BalanceInfoListItem>()

            if(blockCount > 0) {
                val balanceInfoListJson = json.optJSONObject("balanceInfoMap")

                if(null != balanceInfoListJson) {
                    for(key in balanceInfoListJson.keys()) {
                        balanceInfoList.add(
                            BalanceInfoListItem.fromJson(balanceInfoListJson.getJSONObject(key))
                        )
                    }
                }
            }

            return AccountInfo(
                address = address,
                blockCount = blockCount,
                balanceInfoList = balanceInfoList.toList()
            )
        }
    }

    fun znn(): Int {
        return getBalance(ZNN_ZTS)
    }

    fun qsr(): Int {
        return getBalance(QSR_ZTS)
    }

    fun getBalance(tokenStandard: TokenStandard): Int {
        val info = balanceInfoList?.firstOrNull {
            it.token?.tokenStandard == tokenStandard
        }

        return info?.balance ?: 0
    }

    fun getBalanceWithDecimals(tokenStandard: TokenStandard): Number {
        val info = balanceInfoList?.firstOrNull {
            it.token?.tokenStandard == tokenStandard
        }

        return info?.balanceWithDecimals ?: 0
    }

    fun findTokenByTokenStandard(tokenStandard: TokenStandard): Token? {
        return balanceInfoList?.firstOrNull {
            it.token?.tokenStandard == tokenStandard
        }?.token
    }
}

data class BalanceInfoListItem(
    var token: Token?,
    var balance: Int?,
    var balanceWithDecimals: Number?,
    var balanceFormatted: String?
) {
    constructor(token: Token?, balance: Int): this(
        token = token,
        balance = balance,
        balanceWithDecimals = getBalanceWithDecimals(token, balance),
        balanceFormatted = "${getBalanceWithDecimals(token, balance)} ${token?.symbol ?: ""}"
    )

    companion object {
        internal fun getBalanceWithDecimals(token: Token?, balance: Int): Number {
            return AmountUtils.addDecimals(
                balance.toLong(),
                token?.decimals?.toLong() ?: 8
            )
        }

        fun fromJson(json: JSONObject): BalanceInfoListItem {
            val tokenJson = json.optJSONObject("token")
            return BalanceInfoListItem(
                token = if(null != tokenJson) Token.fromJson(tokenJson) else null,
                balance = json.optInt("balance")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        if(null != token) {
            json.put("token", token!!.toJson())
        }
        json.put("balance", balance)
        return json
    }
}
package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.embedded.Definitions
import network.zenon.android.embedded.TOKEN_ZTS_ISSUE_FEE_IN_ZNN
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.nom.Token
import network.zenon.android.model.nom.TokenList
import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.TOKEN_ADDRESS
import network.zenon.android.model.primitives.TokenStandard
import network.zenon.android.model.primitives.ZNN_ZTS
import org.json.JSONObject

class TokenApi {
    lateinit var client: Client

    // RPC
    fun getAll(pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): TokenList {
        val response: Any? = client.sendRequest("embedded.token.getAll", listOf(pageIndex, pageSize))
        return TokenList.fromJson(response as JSONObject)
    }

    fun getByOwner(address: Address, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): TokenList {
        val response: Any? = client.sendRequest("embedded.token.getByOwner", listOf(address.toString(), pageIndex, pageSize))
        return TokenList.fromJson(response as JSONObject)
    }

    fun getByZts(tokenStandard: TokenStandard): Token? {
        val response: Any? = client.sendRequest("embedded.token.getByZts", listOf(tokenStandard.toString()))
        return if (response == null) null else Token.fromJson(response as JSONObject)
    }

    // Contract methods
    fun issueToken(tokenName: String, tokenSymbol: String, tokenDomain: String, totalSupply: Int, maxSupply: Int, decimals: Int, mintable: Boolean, burnable: Boolean, utility: Boolean): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            TOKEN_ADDRESS,
            ZNN_ZTS,
            TOKEN_ZTS_ISSUE_FEE_IN_ZNN,
            Definitions.token.encodeFunction("IssueToken", listOf(
                tokenName,
                tokenSymbol,
                tokenDomain,
                totalSupply,
                maxSupply,
                decimals,
                mintable,
                burnable,
                utility
            ))
        )
    }

    fun mintToken(tokenStandard: TokenStandard, amount: Int, receiveAddress: Address): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            TOKEN_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.token.encodeFunction("Mint", listOf(tokenStandard, amount, receiveAddress))
        )
    }

    fun burnToken(tokenStandard: TokenStandard, amount: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            TOKEN_ADDRESS,
            tokenStandard,
            amount,
            Definitions.token.encodeFunction("Burn", listOf<Any>())
        )
    }

    fun updateToken(tokenStandard: TokenStandard, owner: Address, isMintable: Boolean, isBurnable: Boolean): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            TOKEN_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.token.encodeFunction("UpdateToken", listOf(tokenStandard, owner, isMintable, isBurnable))
        )
    }
}
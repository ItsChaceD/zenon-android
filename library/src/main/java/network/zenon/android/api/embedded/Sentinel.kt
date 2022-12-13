package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.embedded.Definitions
import network.zenon.android.embedded.SENTINEL_REGISTER_ZNN_AMOUNT
import network.zenon.android.model.embedded.RewardHistoryList
import network.zenon.android.model.embedded.SentinelInfo
import network.zenon.android.model.embedded.SentinelInfoList
import network.zenon.android.model.embedded.UncollectedReward
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.QSR_ZTS
import network.zenon.android.model.primitives.SENTINEL_ADDRESS
import network.zenon.android.model.primitives.ZNN_ZTS
import org.json.JSONObject

class SentinelApi {
    lateinit var client: Client

    // RPC
    fun getAllActive(pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): SentinelInfoList {
        val response = client.sendRequest("embedded.sentinel.getAllActive", listOf(pageIndex, pageSize))
        return SentinelInfoList.fromJson(response as JSONObject)
    }

    fun getByOwner(owner: Address): SentinelInfo? {
        val response = client.sendRequest("embedded.sentinel.getByOwner", listOf(owner.toString()))
        return response?.let { SentinelInfo.fromJson(response as JSONObject) }
    }

    // Common RPC
    fun getDepositedQsr(address: Address): Int {
        return client.sendRequest("embedded.sentinel.getDepositedQsr", listOf(address.toString())) as Int
    }

    fun getUncollectedReward(address: Address): UncollectedReward {
        val response =
            client.sendRequest("embedded.sentinel.getUncollectedReward", listOf(address.toString()))
        return UncollectedReward.fromJson(response as JSONObject)
    }

    fun getFrontierRewardByPage(
        address: Address,
        pageIndex: Int = 0,
        pageSize: Int = RPC_MAX_PAGE_SIZE
    ): RewardHistoryList {
        val response = client.sendRequest(
            "embedded.sentinel.getFrontierRewardByPage",
            listOf(address.toString(), pageIndex, pageSize)
        )
        return RewardHistoryList.fromJson(response as JSONObject)
    }

    // Contract methods
    fun register(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            SENTINEL_ADDRESS,
            ZNN_ZTS,
            SENTINEL_REGISTER_ZNN_AMOUNT,
            Definitions.sentinel.encodeFunction("Register", listOf<Any>())
        )
    }

    fun revoke(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            SENTINEL_ADDRESS, ZNN_ZTS, 0,
            Definitions.sentinel.encodeFunction("Revoke", listOf<Any>())
        )
    }

    // Common contract methods
    fun collectReward(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            SENTINEL_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.common.encodeFunction("CollectReward", listOf<Any>())
        )
    }

    fun depositQsr(amount: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            SENTINEL_ADDRESS,
            QSR_ZTS,
            amount,
            Definitions.common.encodeFunction("DepositQsr", listOf<Any>())
        )
    }

    fun withdrawQsr(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            SENTINEL_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.common.encodeFunction("WithdrawQsr", listOf<Any>())
        )
    }
}
package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.embedded.Definitions
import network.zenon.android.model.embedded.RewardHistoryList
import network.zenon.android.model.embedded.StakeList
import network.zenon.android.model.embedded.UncollectedReward
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import network.zenon.android.model.primitives.STAKE_ADDRESS
import network.zenon.android.model.primitives.ZNN_ZTS
import org.json.JSONObject

class StakeApi {
    lateinit var client: Client

    // RPC
    fun getEntriesByAddress(address: Address, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): StakeList {
        val response = client.sendRequest("embedded.stake.getEntriesByAddress", listOf(address.toString(), pageIndex, pageSize))
        return StakeList.fromJson(response as JSONObject)
    }

    // Common RPC
    fun getUncollectedReward(address: Address): UncollectedReward {
        val response = client.sendRequest("embedded.stake.getUncollectedReward", listOf(address.toString()))
        return UncollectedReward.fromJson(response as JSONObject)
    }

    fun getFrontierRewardByPage(address: Address, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): RewardHistoryList {
        val response = client.sendRequest("embedded.stake.getFrontierRewardByPage", listOf(address.toString(), pageIndex, pageSize))
        return RewardHistoryList.fromJson(response as JSONObject)
    }

    // Contract methods
    fun stake(durationInSec: Int, amount: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(STAKE_ADDRESS, ZNN_ZTS, amount, Definitions.stake.encodeFunction("Stake", listOf(durationInSec.toString())))
    }

    fun cancel(id: Hash): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(STAKE_ADDRESS, ZNN_ZTS, 0, Definitions.stake.encodeFunction("Cancel", listOf(id.hash)))
    }

    // Common contract methods
    fun collectReward(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(STAKE_ADDRESS, ZNN_ZTS, 0, Definitions.common.encodeFunction("CollectReward", emptyList<Any>()))
    }
}
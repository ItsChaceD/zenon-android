package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.embedded.Definitions
import network.zenon.android.embedded.PILLAR_REGISTER_ZNN_AMOUNT
import network.zenon.android.model.embedded.*
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.PILLAR_ADDRESS
import network.zenon.android.model.primitives.QSR_ZTS
import network.zenon.android.model.primitives.ZNN_ZTS
import org.json.JSONObject

class PillarApi {
    lateinit var client: Client

    // Common RPC
    fun getDepositedQsr(address: Address): Int {
        return client.sendRequest("embedded.pillar.getDepositedQsr", listOf(address.toString())) as Int
    }

    fun getUncollectedReward(address: Address): UncollectedReward {
        val response = client.sendRequest("embedded.pillar.getUncollectedReward", listOf(address.toString())) as JSONObject
        return UncollectedReward.fromJson(response)
    }

    fun getFrontierRewardByPage(address: Address, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): RewardHistoryList {
        val response = client.sendRequest("embedded.pillar.getFrontierRewardByPage", listOf(address.toString(), pageIndex, pageSize)) as JSONObject
        return RewardHistoryList.fromJson(response)
    }

    // RPC
    fun getQsrRegistrationCost(): Int {
        return client.sendRequest("embedded.pillar.getQsrRegistrationCost", listOf<Any>()) as Int
    }

    fun getAll(pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): PillarInfoList {
        val response = client.sendRequest("embedded.pillar.getAll", listOf(pageIndex, pageSize)) as JSONObject
        return PillarInfoList.fromJson(response)
    }

    fun getByOwner(address: Address): List<PillarInfo> {
        val response = client.sendRequest("embedded.pillar.getByOwner", listOf(address.toString())) as List<*>
        return response.map { PillarInfo.fromJson(it as JSONObject) }
    }

    fun getByName(name: String): PillarInfo? {
        val response = client.sendRequest("embedded.pillar.getByName", listOf(name)) as JSONObject?
        return response?.let { PillarInfo.fromJson(it) }
    }

    fun checkNameAvailability(name: String): Boolean {
        return client.sendRequest("embedded.pillar.checkNameAvailability", listOf(name)) as Boolean
    }

    fun getDelegatedPillar(address: Address): DelegationInfo? {
        val response = client.sendRequest("embedded.pillar.getDelegatedPillar", listOf(address.toString())) as JSONObject?
        return response?.let { DelegationInfo.fromJson(it) }
    }

    fun getPillarEpochHistory(name: String, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): PillarEpochHistoryList {
        val response = client.sendRequest("embedded.pillar.getPillarEpochHistory", listOf(name, pageIndex, pageSize)) as JSONObject
        return PillarEpochHistoryList.fromJson(response)
    }

    fun getPillarsHistoryByEpoch(epoch: Int, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): PillarEpochHistoryList {
        val response = client.sendRequest("embedded.pillar.getPillarsHistoryByEpoch", listOf(epoch, pageIndex, pageSize)) as JSONObject
        return PillarEpochHistoryList.fromJson(response)
    }

    // Contract methods
    fun register(name: String, producerAddress: Address, rewardAddress: Address, giveBlockRewardPercentage: Int = 0, giveDelegateRewardPercentage: Int = 100): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            PILLAR_ADDRESS,
            ZNN_ZTS,
            PILLAR_REGISTER_ZNN_AMOUNT,
            Definitions.pillar.encodeFunction("Register", listOf(
                name,
                producerAddress,
                rewardAddress,
                giveBlockRewardPercentage,
                giveDelegateRewardPercentage
            ))
        )
    }

    fun registerLegacy(name: String, producerAddress: Address, rewardAddress: Address, publicKey: String, signature: String, giveBlockRewardPercentage: Int = 0, giveDelegateRewardPercentage: Int = 100): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            PILLAR_ADDRESS,
            ZNN_ZTS,
            PILLAR_REGISTER_ZNN_AMOUNT,
            Definitions.pillar.encodeFunction("RegisterLegacy", listOf(
                name,
                producerAddress,
                rewardAddress,
                giveBlockRewardPercentage,
                giveDelegateRewardPercentage,
                publicKey,
                signature
            ))
        )
    }

    fun updatePillar(name: String, producerAddress: Address, rewardAddress: Address, giveBlockRewardPercentage: Int, giveDelegateRewardPercentage: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            PILLAR_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.pillar.encodeFunction("UpdatePillar", listOf(
                name,
                producerAddress,
                rewardAddress,
                giveBlockRewardPercentage,
                giveDelegateRewardPercentage
            ))
        )
    }

    fun revoke(name: String): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PILLAR_ADDRESS, ZNN_ZTS, 0, Definitions.pillar.encodeFunction("Revoke", listOf(name)))
    }

    fun delegate(name: String): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PILLAR_ADDRESS, ZNN_ZTS, 0, Definitions.pillar.encodeFunction("Delegate", listOf(name)))
    }

    fun undelegate(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PILLAR_ADDRESS, ZNN_ZTS, 0, Definitions.pillar.encodeFunction("Undelegate", listOf<Any>()))
    }

    // Common contract methods
    fun collectReward(): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PILLAR_ADDRESS, ZNN_ZTS, 0, Definitions.common.encodeFunction("CollectReward", listOf<Any>()))
    }

    fun depositQsr(amount: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PILLAR_ADDRESS, QSR_ZTS, 0, Definitions.common.encodeFunction("DepositQsr", listOf<Any>()))
    }

    fun withdrawQsr(amount: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PILLAR_ADDRESS, QSR_ZTS, 0, Definitions.common.encodeFunction("WithdrawQsr", listOf<Any>()))
    }
}
package network.zenon.android.api

import network.zenon.android.client.Client
import network.zenon.android.client.MEMORY_POOL_PAGE_SIZE
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.model.nom.*
import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import org.json.JSONObject

class LedgerApi {
    lateinit var client: Client

    /**
     * This method returns null if the account-block was accepted
     */
    fun publishRawTransaction(accountBlockTemplate: AccountBlockTemplate): Any? {
        return client.sendRequest("ledger.publishRawTransaction", listOf(accountBlockTemplate.toJson()))
    }

    fun getUnconfirmedBlocksByAddress(address: Address, pageIndex: Int = 0, pageSize: Int = MEMORY_POOL_PAGE_SIZE): AccountBlockList {
        val response = client.sendRequest("ledger.getUnconfirmedBlocksByAddress", listOf(address.toString(), pageIndex, pageSize)) as JSONObject
        return AccountBlockList.fromJson(response)
    }

    fun getUnreceivedBlocksByAddress(address: Address, pageIndex: Int = 0, pageSize: Int = MEMORY_POOL_PAGE_SIZE): AccountBlockList {
        val response = client.sendRequest("ledger.getUnreceivedBlocksByAddress", listOf(address.toString(), pageIndex, pageSize)) as JSONObject
        return AccountBlockList.fromJson(response)
    }

    // Blocks
    fun getFrontierAccountBlock(address: Address?): AccountBlock? {
        val response = client.sendRequest("ledger.getFrontierAccountBlock", listOf(address.toString()))
        return if (response == null) null else AccountBlock.fromJson(response as JSONObject)
    }

    fun getAccountBlockByHash(hash: Hash?): AccountBlock? {
        val response = client.sendRequest("ledger.getAccountBlockByHash", listOf(hash.toString()))
        return if (response == null) null else AccountBlock.fromJson(response as JSONObject)
    }

    fun getAccountBlocksByHeight(address: Address, height: Int = 1, count: Int = RPC_MAX_PAGE_SIZE): AccountBlockList {
        val response = client.sendRequest("ledger.getAccountBlocksByHeight", listOf(address.toString(), height, count)) as JSONObject
        return AccountBlockList.fromJson(response)
    }

    /**
     * pageIndex = 0 returns the most recent account blocks sorted descending by height
     */
    fun getAccountBlocksByPage(address: Address, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): AccountBlockList {
        val response = client.sendRequest("ledger.getAccountBlocksByPage", listOf(address.toString(), pageIndex, pageSize)) as JSONObject
        return AccountBlockList.fromJson(response)
    }

    // Momentum
    fun getFrontierMomentum(): Momentum {
        val response = client.sendRequest("ledger.getFrontierMomentum", emptyList<Any>()) as JSONObject
        return Momentum.fromJson(response)
    }

    fun getMomentumBeforeTime(time: Int): Momentum? {
        val response = client.sendRequest("ledger.getMomentumBeforeTime", listOf(time))
        return if (response == null) null else Momentum.fromJson(response as JSONObject)
    }

    fun getMomentumByHash(hash: Hash): Momentum? {
        val response = client.sendRequest("ledger.getMomentumByHash", listOf(hash.toString()))
        return if (response == null) null else Momentum.fromJson(response as JSONObject)
    }

    fun getMomentumsByHeight(height: Int, count: Int): MomentumList {
        val amendedHeight = if (height < 1) 1 else height
        val amendedCount = if (count > RPC_MAX_PAGE_SIZE) RPC_MAX_PAGE_SIZE else count
        val response = client.sendRequest("ledger.getMomentumsByHeight", listOf(amendedHeight, amendedCount)) as JSONObject
        return MomentumList.fromJson(response)
    }

    /**
     * pageIndex = 0 returns the most recent momentums sorted descending by height
     */
    fun getMomentumsByPage(pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): MomentumList {
        val response = client.sendRequest("ledger.getMomentumsByPage", listOf(pageIndex, pageSize)) as JSONObject
        return MomentumList.fromJson(response)
    }

    fun getDetailedMomentumsByHeight(height: Int, count: Int): DetailedMomentumList {
        val amendedHeight = if (height < 1) 1 else height
        val amendedCount = if (count > RPC_MAX_PAGE_SIZE) RPC_MAX_PAGE_SIZE else count
        val response = client.sendRequest("ledger.getDetailedMomentumsByHeight", listOf(amendedHeight, amendedCount)) as JSONObject
        return DetailedMomentumList.fromJson(response)
    }

    // Account info
    fun getAccountInfoByAddress(address: Address): AccountInfo {
        val response = client.sendRequest("ledger.getAccountInfoByAddress", listOf(address.toString())) as JSONObject
        return AccountInfo.fromJson(response)
    }
}
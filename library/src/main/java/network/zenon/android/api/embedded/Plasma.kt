package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.embedded.Definitions
import network.zenon.android.model.embedded.FusionEntryList
import network.zenon.android.model.embedded.GetRequiredParam
import network.zenon.android.model.embedded.GetRequiredResponse
import network.zenon.android.model.embedded.PlasmaInfo
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.primitives.*
import org.json.JSONObject

class PlasmaApi {
    lateinit var client: Client

    // RPC
    fun get(address: Address): PlasmaInfo {
        val response = client.sendRequest("embedded.plasma.get", listOf(address.toString()))
        return PlasmaInfo.fromJson(response as JSONObject)
    }

    fun getEntriesByAddress(address: Address, pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): FusionEntryList {
        val response = client.sendRequest("embedded.plasma.getEntriesByAddress", listOf(address.toString(), pageIndex, pageSize))
        return FusionEntryList.fromJson(response as JSONObject)
    }

    fun getRequiredFusionAmount(requiredPlasma: Int): Int {
        return client.sendRequest("embedded.plasma.getRequiredFusionAmount", listOf(requiredPlasma)) as Int
    }

    fun getPlasmaByQsr(qsrAmount: Double): Int {
        return qsrAmount.toInt() * 2100
    }

    fun getRequiredPoWForAccountBlock(powParam: GetRequiredParam): GetRequiredResponse {
        val response = client.sendRequest("embedded.plasma.getRequiredPoWForAccountBlock", listOf(powParam.toJson()))
        return GetRequiredResponse.fromJson(response as JSONObject)
    }

    // Contract methods
    fun fuse(beneficiary: Address, amount: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PLASMA_ADDRESS, QSR_ZTS, amount, Definitions.plasma.encodeFunction("Fuse", listOf(beneficiary)))
    }

    fun cancel(id: Hash): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(PLASMA_ADDRESS, ZNN_ZTS, 0, Definitions.plasma.encodeFunction("CancelFuse", listOf(id.hash)))
    }
}
package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.embedded.*
import network.zenon.android.model.embedded.SwapAssetEntry
import network.zenon.android.model.embedded.SwapLegacyPillarEntry
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.primitives.Hash
import network.zenon.android.model.primitives.SWAP_ADDRESS
import network.zenon.android.model.primitives.ZNN_ZTS
import org.json.JSONObject

class SwapApi {
    lateinit var client: Client

    // RPC
    fun getAssetsByKeyIdHash(keyIdHash: String): SwapAssetEntry {
        val response = client.sendRequest("embedded.swap.getAssetsByKeyIdHash", arrayOf(keyIdHash))
        return SwapAssetEntry.fromJson(Hash.parse(keyIdHash), response as JSONObject)
    }

    fun getAssets(): Map<String, SwapAssetEntry> {
        val response = client.sendRequest("embedded.swap.getAssets", arrayOf<Any>()) as Map<String, JSONObject>
        return response.mapValues { SwapAssetEntry.fromJson(Hash.parse(it.key), it.value) }
    }

    fun getLegacyPillars(): List<SwapLegacyPillarEntry> {
        val response = client.sendRequest("embedded.swap.getLegacyPillars", arrayOf<Any>()) as List<JSONObject>
        return response.map { SwapLegacyPillarEntry.fromJson(it) }
    }

    // Contract methods
    fun retrieveAssets(pubKey: String, signature: String): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            SWAP_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.swap.encodeFunction("RetrieveAssets", arrayOf(pubKey, signature))
        )
    }

    fun getSwapDecayPercentage(currentTimestamp: Int): Int {
        var percentageToGive = 100
        val currentEpoch = (currentTimestamp - GENESIS_TIMESTAMP) / 86400
        percentageToGive = if (currentTimestamp < SWAP_ASSET_DECAY_TIMESTAMP_START) {
            100
        } else {
            val numTicks: Int =
                ((currentEpoch - SWAP_ASSET_DECAY_EPOCHS_OFFSET + 1) / SWAP_ASSET_DECAY_TICK_EPOCHS)
            val decayFactor = SWAP_ASSET_DECAY_TICK_VALUE_PERCENTAGE * numTicks
            if (decayFactor > 100) {
                0
            } else {
                100 - decayFactor
            }
        }
        return 100 - percentageToGive
    }
}
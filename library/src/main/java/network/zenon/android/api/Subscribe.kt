package network.zenon.android.api

import network.zenon.android.client.Client
import network.zenon.android.model.primitives.Address

class SubscribeApi {
    lateinit var client: Client

    fun toMomentums(): String? {
        return client.sendRequest("ledger.subscribe", listOf("momentums")) as String?
    }

    fun toAllAccountBlocks(): String? {
        return client.sendRequest("ledger.subscribe", listOf("allAccountBlocks")) as String?
    }

    fun toAccountBlocksByAddress(address: Address): String? {
        return client.sendRequest("ledger.subscribe", listOf("accountBlocksByAddress", address.toString())) as String?
    }

    fun toUnreceivedAccountBlocksByAddress(address: Address): String? {
        return client.sendRequest("ledger.subscribe", listOf("unreceivedAccountBlocksByAddress", address.toString())) as String?
    }
}
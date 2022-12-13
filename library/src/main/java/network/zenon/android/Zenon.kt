package network.zenon.android

import network.zenon.android.api.EmbeddedApi
import network.zenon.android.api.LedgerApi
import network.zenon.android.api.StatsApi
import network.zenon.android.api.SubscribeApi
import network.zenon.android.client.WsClient
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.pow.PowStatus
import network.zenon.android.utils.BlockUtils
import network.zenon.android.wallet.KeyPair
import network.zenon.android.wallet.KeyStore
import network.zenon.android.wallet.KeyStoreManager
import java.nio.file.Path

val NO_KEY_PAIR_SELECTED_EXCEPTION = ZnnSdkException("No default keyPair selected")

class Zenon private constructor() {
    var defaultKeyPair: KeyPair? = null
    var defaultKeyStore: KeyStore? = null
    var defaultKeyStorePath: Path? = null

    var wsClient: WsClient
    var keyStoreManager: KeyStoreManager

    var ledger: LedgerApi
    var stats: StatsApi
    var embedded: EmbeddedApi
    var subscribe: SubscribeApi

    init {
        keyStoreManager = KeyStoreManager(walletPath = ZNN_DEFAULT_WALLET_DIRECTORY)
        wsClient = WsClient()
        ledger = LedgerApi()
        stats = StatsApi()
        embedded = EmbeddedApi()
        subscribe = SubscribeApi()
        ledger.client = wsClient
        stats.client = wsClient
        embedded.client = wsClient
        subscribe.client = wsClient
    }

    companion object {
        val instance = Zenon()
    }

    fun send(
        transaction: AccountBlockTemplate,
        currentKeyPair: KeyPair? = this.defaultKeyPair,
        generatingPowCallback: (PowStatus) -> Unit = {},
        waitForRequiredPlasma: Boolean = false
    ): AccountBlockTemplate {
        currentKeyPair ?: throw NO_KEY_PAIR_SELECTED_EXCEPTION
        return BlockUtils.send(
            transaction,
            currentKeyPair,
            generatingPowCallback = generatingPowCallback,
            waitForRequiredPlasma = waitForRequiredPlasma
        )
    }

    fun requiresPoW(
        transaction: AccountBlockTemplate,
        blockSigningKey: KeyPair? = this.defaultKeyPair
    ): Boolean {
        blockSigningKey ?: throw NO_KEY_PAIR_SELECTED_EXCEPTION
        return BlockUtils.requiresPoW(transaction, blockSigningKey = blockSigningKey)
    }
}
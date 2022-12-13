package network.zenon.android.wallet

/// BIP44 https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki
///
/// m / purpose' / coin_type' / account' / change / address_index

object Derivation {
    const val COIN_TYPE = "73404"
    const val DERIVATION_PATH = "m/44'/$COIN_TYPE'"

    fun getDerivationAccount(account: Int = 0): String {
        return "$DERIVATION_PATH/$account'"
    }
}
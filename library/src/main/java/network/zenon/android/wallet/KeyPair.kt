package network.zenon.android.wallet

import network.zenon.android.crypto.Crypto
import network.zenon.android.model.primitives.Address

class KeyPair(
    var privateKey: ByteArray?,
    var publicKey: ByteArray = Crypto.getPublicKey(privateKey!!),
    var address: Address = Address.fromPublicKey(publicKey)
) {
    fun sign(message: ByteArray): ByteArray {
        return Crypto.sign(message, privateKey, publicKey)
    }

    fun verify(signature: ByteArray, message: ByteArray): Boolean {
        return Crypto.verify(signature, message, publicKey)
    }

    fun generatePublicKey(privateKey: ByteArray): ByteArray {
        return Crypto.getPublicKey(privateKey)
    }
}

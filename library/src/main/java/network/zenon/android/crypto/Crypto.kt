package network.zenon.android.crypto

import okio.HashingSink.Companion.sha512
import okio.blackholeSink
import okio.buffer
import org.bouncycastle.jcajce.provider.digest.SHA3


object Crypto {
    private val ed25519HashFunc = fun(m: ByteArray?): ByteArray {
        sha512(blackholeSink()).use { hashingSink ->
            hashingSink.buffer().use { sink ->
                sink.write(m!!)
                sink.close()
                return hashingSink.hash.toByteArray()
            }
        }
    }

    fun getPublicKey(privateKey: ByteArray): ByteArray {
        return Ed25519.publicKey(ed25519HashFunc, privateKey)
    }

    fun sign(message: ByteArray, privateKey: ByteArray?, publicKey: ByteArray): ByteArray {
        return Ed25519.signature(
            ed25519HashFunc,
            message,
            privateKey,
            publicKey
        )
    }

    fun verify(signature: ByteArray, message: ByteArray, publicKey: ByteArray): Boolean {
        return Ed25519.checkvalid(
            ed25519HashFunc,
            signature,
            message,
            publicKey
        )
    }

    fun deriveKey(path: String, seed: String): ByteArray {
        return Ed25519.derivePath(path, seed).key!!
    }

    fun digest(data: ByteArray, digestSize: Int = 32): ByteArray {
        return SHA3.DigestSHA3(digestSize * 8).digest(data)
    }
}
package network.zenon.android.model.primitives

import network.zenon.android.crypto.Crypto
import network.zenon.android.utils.BytesUtils

val EMPTY_HASH = Hash.parse("0000000000000000000000000000000000000000000000000000000000000000")

class Hash(val hash: ByteArray) {
    companion object {
        private const val LENGTH = 32

        fun fromBytes(bytes: ByteArray): Hash {
            if(bytes.size != LENGTH) {
                throw IllegalArgumentException("Invalid hash length.")
            }

            return Hash(bytes)
        }

        fun parse(hashString: String): Hash {
            if(hashString.length != 2 * LENGTH) {
                throw IllegalArgumentException("Invalid hash length.")
            }

            return Hash(BytesUtils.hexToBytes(hashString))
        }

        fun digest(bytes: ByteArray): Hash {
            return Hash(Crypto.digest(bytes, LENGTH))
        }
    }

    override fun equals(other: Any?): Boolean {
        return (
            other is Hash &&
            other.hash.contentEquals(hash)
        )
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        return BytesUtils.bytesToHex(hash).lowercase()
    }

    fun toShortString(): String {
        val longString = toString()
        return (
            longString.substring(0, 6) +
            "..." +
            longString.substring(longString.length - 6)
        )
    }
}
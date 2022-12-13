@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection",
    "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection",
    "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection",
    "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection",
    "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection",
    "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection"
)

package network.zenon.android.model.primitives

import org.bouncycastle.jcajce.provider.digest.SHA3

val EMPTY_ADDRESS = Address.parse("z1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqsggv2f")
val PLASMA_ADDRESS = Address.parse("z1qxemdeddedxplasmaxxxxxxxxxxxxxxxxsctrp")
val PILLAR_ADDRESS = Address.parse("z1qxemdeddedxpyllarxxxxxxxxxxxxxxxsy3fmg")
val TOKEN_ADDRESS = Address.parse("z1qxemdeddedxt0kenxxxxxxxxxxxxxxxxh9amk0")
val SENTINEL_ADDRESS = Address.parse("z1qxemdeddedxsentynelxxxxxxxxxxxxxwy0r2r")
val SWAP_ADDRESS = Address.parse("z1qxemdeddedxswapxxxxxxxxxxxxxxxxxxl4yww")
val STAKE_ADDRESS = Address.parse("z1qxemdeddedxstakexxxxxxxxxxxxxxxxjv8v62")
val SPORK_ADDRESS = Address.parse("z1qxemdeddedxsp0rkxxxxxxxxxxxxxxxx956u48")
val ACCELERATOR_ADDRESS = Address.parse("z1qxemdeddedxaccelerat0rxxxxxxxxxxp4tk22")
val BRIDGE_ADDRESS = Address.parse("z1qzlytaqdahg5t02nz5096frflfv7dm3y7yxmg7")

val EMBEDDED_CONTRACT_ADDRESSES = listOf(
    PLASMA_ADDRESS,
    PILLAR_ADDRESS,
    TOKEN_ADDRESS,
    SENTINEL_ADDRESS,
    SWAP_ADDRESS,
    STAKE_ADDRESS,
    ACCELERATOR_ADDRESS
)

class Address(
    var hrp: String?,
    var core: ByteArray?
) {
    companion object {
        const val PREFIX = "z"
        const val USER_BYTE = 0.toByte()
        const val CONTRACT_BYTE = 1.toByte()
        const val CORE_SIZE = 20

        fun parse(address: String): Address {
            val bech32 = Bech32Codec.decode(address)
            val core = Bech32Codec.convertBech32Bits(bech32.data, 5, 8, false)
            return Address(bech32.hrp, core)
        }

        fun fromPublicKey(publicKey: ByteArray): Address {
            val digest = SHA3.DigestSHA3(256).digest(publicKey).take(19)
            return Address(PREFIX, byteArrayOf(USER_BYTE) + digest)
        }

        fun isValid(address: String): Boolean {
            return try {
                val a = parse(address)
                a.toString() == address
            } catch(e: Exception) {
                false
            }
        }
    }

    fun getBytes(): ByteArray? {
        return core
    }

    override fun equals(other: Any?): Boolean {
        return (other is Address)
                && (hrp == other.hrp)
                && core.contentEquals(other.core)
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        val bech32 = Bech32(hrp!!, Bech32Codec.convertBech32Bits(
            core!!, 8, 5, true
        ))
        return Bech32Codec.encode(bech32)
    }

    fun toShortString(): String {
        val longString = toString()
        return longString.substring(0, 7) +
                "..." +
                longString.substring(longString.length - 6)
    }

    fun isEmbedded(): Boolean {
        return EMBEDDED_CONTRACT_ADDRESSES.contains(this)
    }
}
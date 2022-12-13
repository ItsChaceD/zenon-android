package network.zenon.android.model.primitives

const val ZNN_TOKEN_STANDARD = "zts1znnxxxxxxxxxxxxx9z4ulx"
const val QSR_TOKEN_STANDARD = "zts1qsrxxxxxxxxxxxxxmrhjll"
const val EMPTY_TOKEN_STANDARD = "zts1qqqqqqqqqqqqqqqqtq587y"

val ZNN_ZTS = TokenStandard.parse(ZNN_TOKEN_STANDARD)
val QSR_ZTS = TokenStandard.parse(QSR_TOKEN_STANDARD)
val EMPTY_ZTS = TokenStandard.parse(EMPTY_TOKEN_STANDARD)

class TokenStandard(
    var hrp: String,
    var core: ByteArray
) {
    init {
        if (hrp != PREFIX) {
            throw IllegalArgumentException("Invalid ZTS prefix. Expected $PREFIX but got $hrp")
        }

        if (core.size != CORE_SIZE) {
            throw IllegalArgumentException("Invalid ZTS size. Expected $CORE_SIZE but got ${core.size}")
        }
    }

    companion object {
        const val PREFIX = "zts"
        const val CORE_SIZE = 10

        lateinit var hrp: String
        lateinit var core: ByteArray

        fun parse(tokenStandard: String): TokenStandard {
            val bech32 = Bech32Codec.decode(tokenStandard)
            hrp = bech32.hrp
            core = Bech32Codec.convertBech32Bits(bech32.data, 5, 8, false)
            return TokenStandard(hrp, core)
        }

        fun fromBytes(bytes: ByteArray): TokenStandard {
            hrp = PREFIX
            core = bytes
            return TokenStandard(hrp, core)
        }

        fun bySymbol(symbol: String): TokenStandard {
            return if(symbol.lowercase().contentEquals("znn")) {
                ZNN_ZTS
            } else if(symbol.lowercase().contentEquals("qsr")) {
                QSR_ZTS
            } else {
                throw IllegalArgumentException("TokenStandard.bySymbol supports only znn/qsr.")
            }
        }
    }

    fun getBytes(): ByteArray {
        return core
    }

    override fun equals(other: Any?): Boolean {
        return (
            other is TokenStandard &&
            other.toString().contentEquals(toString())
        )
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        val bech32 = Bech32(hrp, Bech32Codec.convertBech32Bits(core, 8, 5, true))
        return Bech32Codec.encode(bech32)
    }
}
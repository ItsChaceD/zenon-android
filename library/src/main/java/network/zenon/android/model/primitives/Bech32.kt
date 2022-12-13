package network.zenon.android.model.primitives

import android.nfc.FormatException

class Bech32(val hrp: String, val data: ByteArray)

object Bech32Codec {
    fun decode(encoded: String): Bech32 {
        return Bech32Impl.bech32Decode(encoded)
    }

    fun encode(bech32: Bech32): String {
        return Bech32Impl.bech32Encode(bech32.hrp, bech32.data)
    }

    fun convertBech32Bits(data: ByteArray, from: Int, to: Int, pad: Boolean): ByteArray {
        var acc = 0
        var bits = 0
        var result = ByteArray(0)
        val maxv = (1 shl to) - 1

        data.forEach { v ->
            acc = (acc shl from) or (v.toUByte().toInt())
            bits += from
            while (bits >= to) {
                bits -= to
                result += ((acc shr bits) and maxv).toByte()
            }
        }

        if (pad) {
            if (bits > 0) {
                result += ((acc shl (to - bits)) and maxv).toByte()
            }
        } else if (bits >= from) {
            throw IllegalArgumentException("Illegal zero padding")
        } else if (((acc shl (to - bits)) and maxv) != 0) {
            throw IllegalArgumentException("Non zero")
        }

        return result
    }
}

private class Bech32Impl {
    companion object {
        private const val CHARSET: String = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"
        private val BYTESET: ByteArray = CHARSET.encodeToByteArray()

        private fun polymod(values: ByteArray): Int {
            var chk = 1

            for(value in values) {
                val top = chk.ushr(25) and 0xff
                chk = chk and 0x1ffffff shl 5 xor (value.toUByte().toInt() and 0xff)
                if (top and 1 != 0) chk = chk xor 0x3b6a57b2
                if (top and 2 != 0) chk = chk xor 0x26508e6d
                if (top and 4 != 0) chk = chk xor 0x1ea119fa
                if (top and 8 != 0) chk = chk xor 0x3d4233dd
                if (top and 16 != 0) chk = chk xor 0x2a1462b3
            }

            return chk
        }

        private fun expandHrp(hrp: String): ByteArray {
            val hrpLength = hrp.length
            val ret = ByteArray(hrpLength * 2 + 1)
            for(i in 0 until hrpLength) {
                val c = hrp[i].code and 0x7f
                ret[i] = (c.ushr(5) and 0x07).toByte()
                ret[i + hrpLength + 1] = (c and 0x1f).toByte()
            }
            ret[hrpLength] = 0
            return ret
        }

        private fun verifyChecksum(hrp: String, values: ByteArray): Boolean {
            val hrpExpanded = expandHrp(hrp)
            val combined = ByteArray(hrpExpanded.size + values.size)
            hrpExpanded.copyInto(combined)
            values.copyInto(combined, destinationOffset = hrpExpanded.size)
            return polymod(combined) == 1
        }

        private fun createChecksum(hrp: String, values: ByteArray): ByteArray {
            val hrpExpanded = expandHrp(hrp)
            val enc = ByteArray(hrpExpanded.size + values.size + 6)
            hrpExpanded.copyInto(enc)
            values.copyInto(enc, startIndex = 0, destinationOffset = hrpExpanded.size)

            val mod = polymod(enc) xor 1
            val ret = ByteArray(6)
            for(i in 0..5) {
                ret[i] = (mod.ushr(5 * (5 - i)) and 31).toByte()
            }
            return ret
        }

        fun bech32Encode(hrp: String, data: ByteArray): String {
            val combined = data + createChecksum(hrp.lowercase(), data)
            val sb = StringBuilder(hrp.length + 1 + combined.size)
            sb.append(hrp)
            sb.append('1')
            for(b in combined) {
                sb.append(CHARSET[b.toUByte().toInt()])
            }
            return sb.toString()
        }

        fun bech32Decode(bech: String): Bech32 {
            var lower = false
            var upper = false

            if(bech.length < 8 || bech.length > 90) {
                throw FormatException("Bech data is an invalid length. " +
                        "Must be between 8 and 90 characters. " +
                        "(Length = ${bech.length})."
                )
            }
            for((i, c) in bech.withIndex()) {
                if(c.code < 33 || c.code > 126) {
                    throw FormatException("Bech character is out of range." +
                            "Character code must be between 33 and 126. " +
                            "(Invalid Character $c with Code ${c.code} found at Position ${i})."
                    )
                }

                if(c in 'a'..'z') {
                    if (upper) {
                        throw FormatException(
                            "Invalid Character $c with Code ${c.code} found at Position ${i}."
                        )
                    }
                    lower = true
                }

                if(c in 'A'..'Z') {
                    if(lower) {
                        throw FormatException(
                            "Invalid Character $c with Code ${c.code} found at Position ${i}."
                        )
                    }
                    upper = true
                }
            }


            val pos = bech.lastIndexOf('1')

            if(pos < 1) {
                throw FormatException(
                    "Bech is missing human-readable part."
                )
            }

            val dataPartLength = bech.length - 1 - pos
            if(dataPartLength < 6) {
                throw FormatException(
                    "Data part is too short (must be longer than 6 but length is: $dataPartLength)."
                )
            }

            val values = ByteArray(dataPartLength)
            for(i in 0 until dataPartLength) {
                val c = bech[i + pos + 1]
                if(BYTESET[c.code].toUByte().toInt() == -1) {
                    throw FormatException(
                        "Invalid Character $c with Code ${c.code} found at Position ${i + pos + 1}."
                    )
                }

                values[i] = BYTESET[c.code]
            }

            val hrp = bech.substring(0, pos).lowercase()

            if(!verifyChecksum(hrp, values)) {
                throw FormatException(
                    "Checksum does not validate."
                )
            }

            return Bech32(hrp, values.copyOfRange(0, values.size - 6))
        }
    }
}
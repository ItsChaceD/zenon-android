package network.zenon.android.utils

import android.util.Base64
import java.math.BigInteger
import kotlin.math.min

object BytesUtils {
    fun decodeBigInt(bytes: ByteArray): BigInteger {
        var result = BigInteger.valueOf(0)

        for(byte in bytes) {
            result *= BigInteger.valueOf(256)
            result += BigInteger.valueOf(byte.toLong())
        }

        return result
    }

    fun encodeBigInt(initialNumber: BigInteger): ByteArray {
        var n = initialNumber
        val size = (n.bitLength() + 7).ushr(3)
        val result = ByteArray(size)
        result.fill(0.toByte())

        val byteMask = BigInteger.valueOf(0xff)
        for(i in 0..size) {
            result[size - i - 1] = (n and byteMask).toByte()
            n = BigInteger.valueOf(n.toLong().ushr(8))
        }

        return result
    }

    fun bigIntToBytes(b: BigInteger, numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        bytes.fill(0.toByte())

        val biBytes = encodeBigInt(b)
        val start = if(biBytes.size == numBytes + 1) 1 else 0
        val length = min(biBytes.size, numBytes)

        biBytes.copyInto(bytes, numBytes - length, start, length)

        return bytes
    }

    fun bigIntToBytesSigned(b: BigInteger, numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        bytes.fill(
            if(b.signum() < 0)  0xFF.toByte()
            else    0x00.toByte()
        )

        val biBytes = encodeBigInt(b)
        val start = if(biBytes.size == numBytes + 1) 1 else 0
        val length = min(biBytes.size, numBytes)

        biBytes.copyInto(bytes, numBytes - length, start, length)

        return bytes
    }

    fun bytesToBigInt(bb: ByteArray): BigInteger {
        return if(bb.isEmpty()) BigInteger.valueOf(0) else decodeBigInt(bb)
    }

    fun leftPadBytes(bytes: ByteArray, size: Int): ByteArray {
        if(bytes.size >= size) {
            return bytes
        }

        val result = ByteArray(size)
        result.fill(0.toByte())

        bytes.copyInto(result, size - bytes.size, 0, bytes.size)

        return result
    }

    fun intToBytes(intValue: Int): ByteArray {
        val bytes = ByteArray(4)
        bytes[0] = (intValue.ushr(24)).toByte()
        bytes[1] = (intValue.ushr(16)).toByte()
        bytes[2] = (intValue.ushr(8)).toByte()
        bytes[3] = intValue.toByte()
        return bytes
    }

    fun longToBytes(longValue: Long): ByteArray {
        val buffer = ByteArray(8)

        for(i in 0..8) {
            val offset = 64 - (i + 1) * 8
            buffer[i] = ((longValue.ushr(offset)) and 0xff).toByte()
        }

        return buffer
    }

    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hexToBytes(value: String): ByteArray {
        check(value.length % 2 == 0) { "Must have an even length" }

        return value.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun base64ToBytes(base64Str: String): ByteArray {
        return if(base64Str.isEmpty()) byteArrayOf() else
            Base64.decode(base64Str, Base64.DEFAULT)
    }

    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}
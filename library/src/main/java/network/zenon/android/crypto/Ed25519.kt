package network.zenon.android.crypto

import network.zenon.android.utils.BytesUtils
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import java.math.BigInteger
import java.nio.ByteBuffer
import kotlin.experimental.or

typealias HashFunc = (m: ByteArray?) -> ByteArray

class KeyData(
    val key: ByteArray?,
    val chainCode: ByteArray?
)

const val ED25519_CURVE = "ed25519 seed"
const val HARDENED_OFFSET = 0x80000000

object Ed25519 {
    private const val b = 256
    private val q = BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819949")
    private val qm2 = BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819947")
    private val qp3 = BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819952")
    private val l = BigInteger("7237005577332262213973186563042994240857116359379907606001950938285454250989")
    private val d = BigInteger("-4513249062541557337682894930092624173785641285191125241628941591882900924598840740")
    private val I = BigInteger("19681161376707505956807079304988542015446066515923890162744021073123829784752")
    private val by = BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960")
    private val bx = BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202")
    private val B: List<BigInteger> = listOf(bx % q, by % q)
    private val un = BigInteger(
    "57896044618658097711785492504343953926634992332820282019728792003956564819967")

    private val zero: BigInteger = BigInteger.valueOf(0)
    private val one: BigInteger = BigInteger.valueOf(1)
    private val two: BigInteger = BigInteger.valueOf(2)
    private val eight = BigInteger.valueOf(8)

    fun hash(f: HashFunc, m: ByteArray?): ByteArray {
        return f(m)
    }

    fun expmod(b: BigInteger, e: BigInteger, m: BigInteger): BigInteger {
        return b.modPow(e, m)
    }

    fun inv(x: BigInteger): BigInteger {
        return x.modInverse(q)
    }

    fun xrecover(y: BigInteger): BigInteger {
        val y2 = y * y
        val xx = (y2 - one) * (inv(d * y2 + one))
        var x = expmod(xx, qp3 / eight, q)
        if ((x * x - xx) % q != zero) x = x * I % q
        if (x % two != zero) x = q - x
        return x
    }

    fun edwards(P: List<BigInteger>, Q: List<BigInteger>): List<BigInteger> {
        val x1 = P[0]
        val y1 = P[1]
        val x2 = Q[0]
        val y2 = Q[1]
        val dtemp = d * x1 * x2 * y1 * y2
        val x3 = ((x1 * y2 + x2 * y1)) * inv(one + dtemp)
        val y3 = ((y1 * y2 + x1 * x2)) * inv(one - dtemp)

        return listOf(x3 % q, y3 % q)
    }

    fun scalarmult(P: List<BigInteger>, e: BigInteger): List<BigInteger> {
        if (e == zero) {
            return listOf(zero, one)
        }
        var Q = scalarmult(P, e / two)
        Q = edwards(Q, Q)
        if (e % two != zero) Q = edwards(Q, P)
        return Q
    }

    fun encodeint(y: BigInteger): ByteArray {
        return toBytes(y)
    }

    fun encodepoint(P: List<BigInteger>): ByteArray {
        val x = P[0]
        val y = P[1]
        val out = encodeint(y)
        out[out.size - 1] = out[out.size - 1] or
            if (x % two != zero) 0x80.toByte() else 0
        return out
    }

    fun bit(h: ByteArray, i: Int): Int {
        return h[i / 8].toUByte().toInt() shr (i % 8) and 1
    }

    fun publicKey(f: HashFunc, sk: ByteArray): ByteArray {
        val h = hash(f, sk)
        var a = two.pow(b - 2)
        for(i in 3 until b - 2) {
            val apart = two.pow(i) * BigInteger.valueOf(bit(h, i).toLong())
            a += apart
        }
        val A = scalarmult(B, a)
        return encodepoint(A)
    }

    fun hint(f: HashFunc, m: ByteArray): BigInteger {
        val h = hash(f, m)
        var hsum = zero
        for(i in 0 until 2 * b) {
            hsum += two.pow(i) * BigInteger.valueOf(bit(h, i).toLong())
        }
        return hsum
    }

    fun signature(f: HashFunc, m: ByteArray, sk: ByteArray?, pk: ByteArray): ByteArray {
        val h = hash(f, sk)
        var a = two.pow(b - 2)
        for(i in 3 until (b - 2)) {
            a += (two.pow(i) * BigInteger.valueOf(bit(h, i).toLong()))
        }

        val rsub = ByteArray(b / 8 + m.size)
        var j = 0
        for(i in b / 8 until b / 8 + (b / 4 - b / 8)) {
            rsub[j] = h[i]
            j++
        }
        for(element in m) {
            rsub[j] = element
            j++
        }
        val r = hint(f, rsub)
        val R = scalarmult(B, r)
        val stemp = ByteArray(32 + pk.size + m.size)

        val point = encodepoint(R)
        j = 0
        for(element in point) {
            stemp[j] = element
            j++
        }
        for(element in pk) {
            stemp[j] = element
            j++
        }
        for(element in m) {
            stemp[j] = element
            j++
        }
        val x = hint(f, stemp)
        val S = (r + (x * a)) % l
        val ur = encodepoint(R)
        val us = encodeint(S)
        val out = ByteArray(ur.size + us.size)
        j = 0
        for(element in ur) {
            out[j] = element
            j++
        }
        for(element in us) {
            out[j] = element
            j++
        }
        return out
    }

    fun isoncurve(P: List<BigInteger>): Boolean {
        val x = P[0]
        val y = P[1]

        val xx = x * x
        val yy = y * y
        val dxxyy = d * yy * xx
        return (-xx + yy - one - dxxyy) % q == zero
    }

    fun decodeint(s: ByteArray): BigInteger {
        return fromBytes(s) and un
    }

    fun decodepoint(s: ByteArray): List<BigInteger> {
        val ybyte = ByteArray(s.size)
        for (i in s.indices) {
            ybyte[i] = s[s.size - 1 - i]
        }
        val fb = fromBytes(s)
        val y = fb and un
        var x = xrecover(y)
        if((x % two != zero) || 0 != bit(s, b - 1)) {
            x = q - x
        }
        val P = listOf(x, y)
        assert(isoncurve(P))
        return P
    }

    fun checkvalid(f: HashFunc, s: ByteArray, m: ByteArray, pk: ByteArray): Boolean {
        assert(s.size == b / 4)
        assert(pk.size == b / 8)

        val rbyte = copyRange(s, 0, b / 8)
        val R = decodepoint(rbyte)
        val A = decodepoint(pk)

        val sbyte = copyRange(s, b / 8, b / 4)
        val S = decodeint(sbyte)

        val stemp = ByteArray(32 + pk.size + m.size)
        val point = encodepoint(R)
        var j = 0
        for (element in point) {
            stemp[j] = element
            j++
        }
        for (element in pk) {
            stemp[j] = element
            j++
        }
        for (element in m) {
            stemp[j] = element
            j++
        }
        val x = hint(f, stemp)
        val ra = scalarmult(B, S)
        val rb = edwards(R, scalarmult(A, x))
        if (ra[0] != rb[0] || ra[1] != rb[1]) {
            return false
        }
        return true
    }

    private fun fromBytes(bytes: ByteArray): BigInteger {
        fun read(start: Int, end: Int): BigInteger {
            if (end - start <= 4) {
                var result = 0
                for(i in end - 1 downTo start) {
                    result = result * 256 + bytes[i]
                }
                return BigInteger.valueOf(result.toLong())
            }
            val mid = start + ((end - start) shr 1)
            return read(start, mid) +
                    read(mid, end) * (one shl ((mid - start) * 8))
        }

        return read(0, bytes.size)
    }

    private fun toBytes(number: BigInteger): ByteArray {
        val bytes = 32
        val b256 = BigInteger.valueOf(256)
        val result = ByteArray(bytes)
        var num = number
        for(i in 0 until bytes) {
            result[i] = num.remainder(b256).toInt().toByte()
            num = num shr 8
        }
        return result
    }

    private fun copyRange(src: ByteArray, from: Int, to: Int): ByteArray {
        val dst = ByteArray(to - from)
        for((j, i) in (from until to).withIndex()) {
            dst[j] = src[i]
        }
        return dst
    }

    val CURVE_BYTES = ED25519_CURVE.toByteArray()
    val PATH_REGEX = Regex("""^(m/)?(\d+'?/)*\d+'?$""")

    private fun getKeys(data: ByteArray, keyParameter: ByteArray): KeyData {
        val digest = SHA512Digest()
        val hmac = HMac(digest)
        hmac.init(KeyParameter(keyParameter))
        val i = hmac.process(data)
        val il = i.take(32).toByteArray()
        val ir = i.slice(32 until i.size).toByteArray()

        return KeyData(
            key = il,
            chainCode = ir
        )
    }

    private fun getCKDPriv(data: KeyData, index: Long): KeyData {
        var dataBytes = ByteArray(37)
        dataBytes[0] = 0x00
        for(i in 1 until 33) {
            dataBytes[i] = data.key!![i - 1]
        }
        dataBytes = ByteBuffer.wrap(dataBytes).putInt(33, index.toInt()).array()
        return getKeys(dataBytes, data.chainCode!!)
    }

    fun getMasterKeyFromSeed(seed: String): KeyData {
        val seedBytes = BytesUtils.hexToBytes(seed)
        return getKeys(seedBytes, CURVE_BYTES)
    }

    fun derivePath(path: String, seed: String): KeyData {
        if(!PATH_REGEX.containsMatchIn(path)) {
            throw IllegalArgumentException("Invalid derivation path. Expected BIP32 path format")
        }

        val master = getMasterKeyFromSeed(seed)
        var segments = path.split("/")
        segments = segments.subList(1, segments.size)

        return segments.fold(master) { prevKeyData, indexStr ->
            val index = Integer.parseInt(indexStr.substring(0, indexStr.length - 1))
            getCKDPriv(prevKeyData, index + HARDENED_OFFSET)
        }
    }
}

fun HMac.process(data: ByteArray): ByteArray {
    update(data, 0, data.size)
    val out = ByteArray(macSize)
    val len = doFinal(out, 0)
    return out.take(len).toByteArray()
}
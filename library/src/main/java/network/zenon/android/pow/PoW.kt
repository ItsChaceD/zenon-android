package network.zenon.android.pow

import network.zenon.android.model.primitives.Hash
import network.zenon.android.utils.BytesUtils
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom


enum class PowStatus {
    GENERATING,
    DONE
}

object PoW {
    private const val OUT_SIZE = 8
    private const val IN_SIZE = 32
    private const val DATA_SIZE = 40
    private val SHA_ALG: MessageDigest = MessageDigest.getInstance("SHA3-256")

    fun generate(hash: Hash, difficulty: Long): String {
        return BytesUtils.bytesToHex(generateInternal(hash.hash, difficulty))
    }

    fun benchmark(difficulty: Long): String {
        return BytesUtils.bytesToHex(benchmarkInternal(difficulty))
    }

    private fun generateInternal(hash: ByteArray, difficulty: Long): ByteArray {
        val target = getTarget(difficulty)
        val entropy = randomSeed
        var data = getData(entropy, hash)
        val h = ByteArray(OUT_SIZE)
        while (true) {
            hash(h, data)
            if (greater(h, target)) {
                return dataToNonce(data)
            }
            if (!nextData(data, entropy.size)) {
                data = getData(randomSeed, hash)
            }
        }
    }

    private fun benchmarkInternal(difficulty: Long): ByteArray {
        val target = getTarget(difficulty)
        var data = getData(ByteArray(OUT_SIZE), ByteArray(IN_SIZE))
        val h = ByteArray(OUT_SIZE)
        while (true) {
            hash(h, data)
            if (greater(h, target)) {
                return dataToNonce(data)
            }
            if (!nextData(data, OUT_SIZE)) {
                data = ByteArray(OUT_SIZE)
            }
        }
    }

    private fun hash(hash: ByteArray, data: ByteArray) {
        SHA_ALG.reset()
        val digest: ByteArray = SHA_ALG.digest(data)
        System.arraycopy(digest, 0, hash, 0, 8)
    }

    private fun getTarget(difficulty: Long): ByteArray {
        // set big to 1 << 64
        var big: BigInteger = BigInteger.valueOf(1L shl 62)
        big = big.multiply(BigInteger.valueOf(4))
        var x: BigInteger = big.divide(BigInteger.valueOf(difficulty))
        x = big.min(x)
        val h = ByteArray(OUT_SIZE)
        // set little ending encoding
        var i = 0
        while (i < 8) {
            h[i] = x.shiftRight(i * 8).toByte()
            i += 1
        }
        return h
    }

    private fun nextData(data: ByteArray, max_size: Int): Boolean {
        for(i in 0 until max_size) {
            data[i] = (data[i] + (1.toByte())).toByte()
            if (data[i] != 0.toByte()) {
                return true
            }
        }
        return false
    }

    private fun greater(a: ByteArray, b: ByteArray): Boolean {
        for (i in 7 downTo 0) {
            if (a[i] == b[i]) {
                continue
            }
            return a[i] > b[i]
        }
        return true
    }

    private val randomSeed: ByteArray get() = SecureRandom().generateSeed(OUT_SIZE)

    private fun getData(entropy: ByteArray, hash: ByteArray): ByteArray {
        val data = ByteArray(DATA_SIZE)
        run {
            var i = 0
            while (i < entropy.size) {
                data[i] = entropy[i]
                i += 1
            }
        }
        var i = 0
        while (i < hash.size) {
            data[i + entropy.size] = hash[i]
            i += 1
        }
        return data
    }

    private fun dataToNonce(data: ByteArray): ByteArray {
        val hash = ByteArray(8)
        var i = 0
        while (i < hash.size) {
            hash[i] = data[i]
            i += 1
        }
        return hash
    }
}
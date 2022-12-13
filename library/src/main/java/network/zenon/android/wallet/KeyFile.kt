package network.zenon.android.wallet

import network.zenon.android.model.primitives.Address
import network.zenon.android.utils.BytesUtils
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private object AesGcmUtils {
    fun encrypt(input: ByteArray?, key: ByteArray?, IV: ByteArray?): ByteArray {
        // Get Cipher Instance
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // Create SecretKeySpec
        val keySpec = SecretKeySpec(key, "AES")

        // Create GCMParameterSpec
        val gcmParameterSpec = GCMParameterSpec(16 * 8, IV)

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec)

        // Update Additional Authentication Data
        cipher.updateAAD("zenon".toByteArray())

        // Perform Encryption
        return cipher.doFinal(input)
    }

    fun decrypt(cipherData: ByteArray?, key: ByteArray?, IV: ByteArray?): ByteArray {
        // Get Cipher Instance
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // Create SecretKeySpec
        val keySpec = SecretKeySpec(key, "AES")

        // Create GCMParameterSpec
        val gcmParameterSpec = GCMParameterSpec(16 * 8, IV)

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec)

        // Update Additional Authentication Data
        cipher.updateAAD("zenon".toByteArray())

        // Perform Decryption
        return cipher.doFinal(cipherData)
    }
}

private object Argon2Utils {
    fun hash(password: ByteArray?, salt: ByteArray?): ByteArray {
        val hash = ByteArray(32)
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withParallelism(4)
            .withMemoryAsKB(64 * 1024)
            .withIterations(1)
            .build()
        val generator = Argon2BytesGenerator()
        generator.init(params)
        generator.generateBytes(password, hash)
        return hash
    }
}

class KeyFile(
    var baseAddress: Address?,
    var crypto: CryptoData?,
    var timestamp: Int?,
    var version: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): KeyFile {
            val baseAddress = Address.parse(json.optString("baseAddress"))
            val crypto = json.optJSONObject("crypto")?.let { CryptoData.fromJson(it) }
            val timestamp = json.optInt("timestamp")
            val version = json.optInt("version")

            return KeyFile(baseAddress, crypto, timestamp, version)
        }

        fun encrypt(store: KeyStore, password: String): KeyFile {
            val stored = KeyFile(
                baseAddress = store.getKeyPair().address,
                timestamp = (System.currentTimeMillis() / 1000.0).toInt(),
                version = 1,
                crypto = CryptoData(
                    argon2Params = Argon2Params(salt = byteArrayOf()),
                    cipherData = byteArrayOf(),
                    cipherName = "aes-256-gcm",
                    kdf = "argon2.IDKey",
                    nonce = byteArrayOf()
                )
            )

            return stored.encryptEntropy(store, password)
        }
    }

    private fun encryptEntropy(store: KeyStore, password: String): KeyFile {
        val salt = ByteArray(16)
        val entropy = BytesUtils.hexToBytes(store.entropy!!)
        val key = Argon2Utils.hash(password.toByteArray(), salt)
        val nonce = ByteArray(12)

        SecureRandom().nextBytes(salt)
        SecureRandom().nextBytes(nonce)

        this.crypto!!.argon2Params = Argon2Params(salt = salt)
        this.crypto!!.cipherData = AesGcmUtils.encrypt(entropy, key, nonce)
        this.crypto!!.nonce = nonce

        return this
    }

    fun decrypt(password: String): KeyStore {
        val key = Argon2Utils.hash(password.toByteArray(), this.crypto!!.argon2Params!!.salt)
        val entropy = AesGcmUtils.decrypt(this.crypto!!.cipherData, key, this.crypto!!.nonce)
        return KeyStore().fromEntropy(BytesUtils.bytesToHex(entropy))
    }

    override fun toString(): String {
        return toJson().toString()
    }

    fun toJson(): JSONObject {
        val json = JSONObject()

        json.put("baseAddress", baseAddress.toString())
        json.put("crypto", crypto?.toJson())
        json.put("timestamp", timestamp)
        json.put("version", version)

        return json
    }
}

private fun fromHexString(s: String): ByteArray {
    return BytesUtils.hexToBytes(s.substring(2))
}

private fun toHexString(bytes: ByteArray): String {
    return "0x${BytesUtils.bytesToHex(bytes)}"
}

class Argon2Params(
    var salt: ByteArray?
) {
    companion object {
        fun fromJson(json: JSONObject): Argon2Params {
            return Argon2Params(
                salt = fromHexString(json.getString("salt"))
            )
        }
    }

    fun toJson(): JSONObject {
        val data = JSONObject()
        data.put("salt", toHexString(salt!!))
        return data
    }
}

class CryptoData(
    var argon2Params: Argon2Params?,
    var cipherData: ByteArray?,
    var cipherName: String?,
    var kdf: String?,
    var nonce: ByteArray?
) {
    companion object {
        fun fromJson(json: JSONObject): CryptoData {
            val argon2Params = json.optJSONObject("argon2Params")?.let { Argon2Params.fromJson(it) }
            val cipherData = fromHexString(json.optString("cipherData"))
            val cipherName = json.optString("cipherName")
            val kdf = json.optString("kdf")
            val nonce = fromHexString(json.optString("nonce"))

            return CryptoData(argon2Params, cipherData, cipherName, kdf, nonce)
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()

        json.put("argon2Params", argon2Params?.toJson())
        json.put("cipherData", toHexString(cipherData!!))
        json.put("cipherName", cipherName)
        json.put("kdf", kdf)
        json.put("nonce", toHexString(nonce!!))

        return json
    }
}



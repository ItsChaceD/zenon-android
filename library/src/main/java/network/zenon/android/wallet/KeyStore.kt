package network.zenon.android.wallet

import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.toSeed
import network.zenon.android.crypto.Crypto
import network.zenon.android.model.primitives.Address
import network.zenon.android.utils.BytesUtils
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom

class KeyStore {
    var mnemonicInstance: MnemonicCode? = null
    var mnemonic: String?
        get() {
            return Mnemonic.getMnemonicWords(mnemonicInstance)
        }
        set(value) {
            if(null == value)   return
            if(!Mnemonic.validateMnemonic(value)) {
                throw IllegalArgumentException("Invalid mnemonic")
            }
            mnemonicInstance = MnemonicCode(value)

        }
    var entropy: String?
        get() {
            if(null == mnemonicInstance)    return null
            return BytesUtils.bytesToHex(mnemonicInstance!!.toEntropy())
        }
        set(value) {
            if(null == value)   return
            mnemonicInstance = MnemonicCode(BytesUtils.hexToBytes(value))
        }
    val seed: String?
        get() {
            if(null == mnemonicInstance)    return null
            return BytesUtils.bytesToHex(mnemonicInstance!!.toSeed())
        }

    companion object {
        fun newRandom(): KeyStore {
            val entropy = ByteArray(32)
            SecureRandom().nextBytes(entropy)
            return KeyStore().fromEntropy(BytesUtils.bytesToHex(entropy))
        }
    }

    fun getKeyPair(index: Int = 0): KeyPair {
        return KeyPair(
            privateKey = Crypto.deriveKey(
                Derivation.getDerivationAccount(index),
                seed!!
            )
        )
    }

    fun deriveAddressesByRange(left: Int, right: Int): List<Address> {
        val addresses = mutableListOf<Address>()
        if(null != seed) {
            for(i in left until right) {
                addresses.add(getKeyPair(i).address)
            }
        }
        return addresses.toList()
    }

    fun findAddress(address: Address, numOfAddresses: Int): FindResponse? {
        for(i in 0 until numOfAddresses) {
            val pair = getKeyPair(i)
            if(pair.address == address) {
                return FindResponse(
                    index = i,
                    keyPair = pair
                )
            }
        }

        return null
    }
}

fun KeyStore.fromMnemonic(mnemonic: String): KeyStore {
    this.mnemonicInstance = MnemonicCode(mnemonic)
    return this
}

fun KeyStore.fromEntropy(entropy: String): KeyStore {
    this.mnemonicInstance = MnemonicCode(entropy.toByteArray())
    return this
}

data class FindResponse(
    var path: File? = null,
    var index: Int?,
    var keyPair: KeyPair? = null
) {
    companion object {
        fun fromJson(json: JSONObject): FindResponse {
            return FindResponse(
                path = File(json.optString("keyStore")),
                index = json.optInt("index")
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("keyStore", path?.path)
        json.put("index", index)
        return json
    }
}
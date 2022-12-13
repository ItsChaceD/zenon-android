package network.zenon.android.wallet

import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class KeyStoreManager(
    var walletPath: Path?,
    var keyStoreInUse: KeyStore? = null
) {
    fun saveKeyStore(
        store: KeyStore,
        password: String,
        name: String? = null
    ): File {
        val fileName = name ?: store.getKeyPair(0).address.toString()
        val encrypted = KeyFile.encrypt(store, password)
        val filePath = walletPath!!.resolve(fileName)
        return Files.write(filePath, encrypted.toString().toByteArray()).toFile()
    }

    fun setKeyStore(keyStore: KeyStore) {
        keyStoreInUse = keyStore
    }

    fun getMnemonicInUse(): String? {
        if(null == keyStoreInUse) {
            throw IllegalArgumentException("The key store in use is null")
        }

        return keyStoreInUse!!.mnemonic
    }

    fun readKeyStore(password: String, keyStoreFile: File): KeyStore {
        if(!keyStoreFile.exists()) {
            throw InvalidKeyStorePath("Given key store does not exist $keyStoreFile")
        }

        val content = keyStoreFile.readText()
        return KeyFile.fromJson(JSONObject(content)).decrypt(password)
    }

    fun findKeyStore(name: String): File? {
        walletPath ?: return null
        for (file in walletPath!!.toFile().listFiles()!!) {
            if (file.name == name) {
                if (file.isFile) {
                    return file
                } else {
                    throw InvalidKeyStorePath("Given keyStore is not a file ($name)")
                }
            }
        }
        return null
    }

    fun listAllKeyStores(): List<File> {
        val keyStoreList = mutableListOf<File>()

        for(keyStore in walletPath!!.toFile().listFiles()!!) {
            if(keyStore is File) {
                keyStoreList.add(keyStore)
            }
        }

        return keyStoreList.toList()
    }

    fun createNew(password: String, name: String?): File {
        val store = KeyStore.newRandom()
        return saveKeyStore(store, password, name)
    }

    fun createFromMnemonic(mnemonic: String, password: String, name: String?): File {
        val store = KeyStore().fromMnemonic(mnemonic)
        return saveKeyStore(store, password, name)
    }
}
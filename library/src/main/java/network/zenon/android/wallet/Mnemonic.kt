package network.zenon.android.wallet

import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import network.zenon.android.LOGGER

object Mnemonic {
    fun generateMnemonic(): MnemonicCode {
        return MnemonicCode(WordCount.COUNT_24)
    }

    fun getMnemonicWords(mnemonicCode: MnemonicCode?): String? {
        return mnemonicCode?.reduce { mnemonic, word -> "$mnemonic $word" }
    }

    fun validateMnemonic(mnemonic: String): Boolean {
        return try {
            MnemonicCode(mnemonic).validate()
            true
        } catch(e: Exception) {
            LOGGER.info(e.toString())
            false
        }
    }

    fun isValidWord(word: String): Boolean {
        return EN_WORDLIST.contains(word)
    }

    fun mnemonicToEntropy(mnemonic: String): String {
        val mnemonicCode = MnemonicCode(mnemonic)
        val entropy = mnemonicCode.toEntropy()
        return entropy.joinToString(separator = " ") { "%02x".format(it) }
    }
}
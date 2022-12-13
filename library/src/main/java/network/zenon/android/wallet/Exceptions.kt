package network.zenon.android.wallet

import network.zenon.android.ZnnSdkException

class InvalidKeyStorePath(override var message: String) : Exception(message) {
    override fun toString(): String {
        return message
    }
}

class IncorrectPasswordException : ZnnSdkException("Incorrect Password")
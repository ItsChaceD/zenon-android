package network.zenon.android

import network.zenon.android.utils.BytesUtils
import network.zenon.android.wallet.KeyStore
import network.zenon.android.wallet.fromMnemonic
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MainUnitTest {
    @Test
    fun mainTest() {
        val mnemonic = "route become dream access impulse price inform obtain engage ski believe awful absent pig thing vibrant possible exotic flee pepper marble rural fire fancy"

        val keyStore = KeyStore().fromMnemonic(mnemonic)
        val keyPair = keyStore.getKeyPair(0)
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        val address = keyPair.address

        assertEquals("bc827d0a00a72354dce4c44a59485288500b49382f9ba88a016351787b7b15ca", keyStore.entropy)
        assertEquals("d6b01f96b566d7df9b5b53b1971e4baeb74cc64167a9843f82d04b2194ca4863", BytesUtils.bytesToHex(privateKey!!))
        assertEquals("3e13d7238d0e768a567dce84b54915f2323f2dcd0ef9a716d9c61abed631ba10", BytesUtils.bytesToHex(publicKey))
        assertEquals("z1qqjnwjjpnue8xmmpanz6csze6tcmtzzdtfsww7", address.toString())
        assertEquals("0025374a419f32736f61ecc5ac4059d2f1b5884d", BytesUtils.bytesToHex(address.core!!))
    }
}
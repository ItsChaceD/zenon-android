package network.zenon.android.utils

import network.zenon.android.LOGGER
import network.zenon.android.Zenon
import network.zenon.android.model.embedded.GetRequiredParam
import network.zenon.android.model.embedded.GetRequiredResponse
import network.zenon.android.model.nom.AccountBlock
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.nom.BlockTypeEnum
import network.zenon.android.model.nom.Momentum
import network.zenon.android.model.primitives.EMPTY_HASH
import network.zenon.android.model.primitives.Hash
import network.zenon.android.model.primitives.HashHeight
import network.zenon.android.pow.PoW
import network.zenon.android.pow.PowStatus
import network.zenon.android.wallet.KeyPair
import java.math.BigInteger
import java.util.function.Consumer

object BlockUtils {
    fun isSendBlock(blockType: BlockTypeEnum): Boolean {
        return blockType === BlockTypeEnum.USER_SEND || blockType === BlockTypeEnum.CONTRACT_SEND
    }

    fun isReceiveBlock(blockType: BlockTypeEnum): Boolean {
        return blockType === BlockTypeEnum.USER_RECEIVE || blockType === BlockTypeEnum.GENESIS_RECEIVE || blockType === BlockTypeEnum.CONTRACT_RECEIVE
    }

    fun getTransactionHash(transaction: AccountBlockTemplate): Hash {
        val versionBytes: ByteArray = BytesUtils.longToBytes(transaction.version.toLong())
        val chainIdentifierBytes: ByteArray =
            BytesUtils.longToBytes(transaction.chainIdentifier.toLong())
        val blockTypeBytes: ByteArray =
            BytesUtils.longToBytes(transaction.blockType.ordinal.toLong())
        val previousHashBytes: ByteArray = transaction.previousHash.hash
        val heightBytes: ByteArray = BytesUtils.longToBytes(transaction.height.toLong())
        val momentumAcknowledgedBytes: ByteArray = transaction.momentumAcknowledged.getBytes()
        val addressBytes: ByteArray? = transaction.address.getBytes()
        val toAddressBytes: ByteArray? = transaction.toAddress.getBytes()
        val amountBytes = BytesUtils.bigIntToBytes(BigInteger.valueOf(transaction.amount.toLong()), 32)
        val tokenStandardBytes: ByteArray = transaction.tokenStandard.getBytes()
        val fromBlockHashBytes: ByteArray = transaction.fromBlockHash.hash
        val descendentBlocksBytes: ByteArray = Hash.digest(ByteArray(0)).hash
        val dataBytes: ByteArray = Hash.digest(transaction.data).hash
        val fusedPlasmaBytes: ByteArray = BytesUtils.longToBytes(transaction.fusedPlasma.toLong())
        val difficultyBytes: ByteArray = BytesUtils.longToBytes(transaction.difficulty.toLong())
        val nonceBytes =
            BytesUtils.leftPadBytes(BytesUtils.hexToBytes(transaction.nonce), 8)
        val source: ByteArray = 
            versionBytes +
            chainIdentifierBytes +
            blockTypeBytes +
            previousHashBytes +
            heightBytes +
            momentumAcknowledgedBytes +
            (addressBytes ?: byteArrayOf()) +
            (toAddressBytes ?: byteArrayOf()) +
            amountBytes +
            tokenStandardBytes +
            fromBlockHashBytes +
            descendentBlocksBytes +
            dataBytes +
            fusedPlasmaBytes +
            difficultyBytes +
            nonceBytes
        return Hash.digest(source)
    }

    private fun getTransactionSignature(
        keyPair: KeyPair,
        transaction: AccountBlockTemplate
    ): ByteArray {
        return keyPair.sign(transaction.hash.hash)
    }

    private fun getPoWData(transaction: AccountBlockTemplate): Hash {
        return Hash.digest(
    (transaction.address.getBytes() ?: byteArrayOf()) + transaction.previousHash.hash
        )
    }

    private fun autofillTransactionParameters(accountBlockTemplate: AccountBlockTemplate) {
        val frontierAccountBlock: AccountBlock? = Zenon.instance.ledger.getFrontierAccountBlock(accountBlockTemplate.address)
        var height: Long = 1
        var previousHash: Hash = EMPTY_HASH
        if (frontierAccountBlock != null) {
            height = (frontierAccountBlock.height + 1).toLong()
            previousHash = frontierAccountBlock.hash
        }
        accountBlockTemplate.height = height.toInt()
        accountBlockTemplate.previousHash = previousHash
        val frontierMomentum: Momentum = Zenon.instance.ledger.getFrontierMomentum()
        accountBlockTemplate.momentumAcknowledged = HashHeight(
            frontierMomentum.hash,
            frontierMomentum.height
        )
    }

    private fun checkAndSetFields(
        transaction: AccountBlockTemplate,
        currentKeyPair: KeyPair
    ): Boolean {
        transaction.address = currentKeyPair.address
        transaction.publicKey = currentKeyPair.publicKey
        autofillTransactionParameters(transaction)
        if (!isSendBlock(transaction.blockType)) {
            if (transaction.fromBlockHash == EMPTY_HASH) {
                throw Exception()
            }

            val sendBlock: AccountBlock = Zenon.instance.ledger.getAccountBlockByHash(transaction.fromBlockHash)!!
            if (sendBlock.toAddress != transaction.address || transaction.data.isNotEmpty()) {
                throw Exception()
            }
        }

        if (transaction.difficulty > 0 && transaction.nonce == "") {
            throw Exception()
        }
        return true
    }

    private fun setDifficulty(
        transaction: AccountBlockTemplate,
        generatingPowCallback: Consumer<PowStatus>
    ): Boolean {
        return setDifficulty(transaction, generatingPowCallback, false)
    }

    private fun setDifficulty(
        transaction: AccountBlockTemplate, generatingPowCallback: Consumer<PowStatus>,
        waitForRequiredPlasma: Boolean
    ): Boolean {
        val powParam = GetRequiredParam(
            address = transaction.address,
            blockType = transaction.blockType,
            toAddress = transaction.toAddress,
            data = transaction.data
        )
        val response: GetRequiredResponse = Zenon.instance.embedded.plasma.getRequiredPoWForAccountBlock(powParam)
        if (response.requiredDifficulty != 0) {
            transaction.fusedPlasma = response.availablePlasma
            transaction.difficulty = response.requiredDifficulty
            LOGGER.info("Generating Plasma for block: hash=${getPoWData(transaction)}")
            generatingPowCallback.accept(PowStatus.GENERATING)
            val nonce: String = PoW.generate(getPoWData(transaction), transaction.difficulty.toLong())
            transaction.nonce = nonce
            generatingPowCallback.accept(PowStatus.DONE)
        } else {
            transaction.fusedPlasma = response.basePlasma
            transaction.difficulty = 0
            transaction.nonce = "0000000000000000"
        }
        return true
    }

    private fun setHashAndSignature(
        transaction: AccountBlockTemplate,
        currentKeyPair: KeyPair
    ): Boolean {
        transaction.hash = getTransactionHash(transaction)
        transaction.signature = getTransactionSignature(currentKeyPair, transaction)
        return true
    }

    fun send(
        transaction: AccountBlockTemplate, currentKeyPair: KeyPair,
        generatingPowCallback: Consumer<PowStatus>
    ): AccountBlockTemplate {
        return send(transaction, currentKeyPair, generatingPowCallback, false)
    }

    fun send(
        transaction: AccountBlockTemplate, currentKeyPair: KeyPair,
        generatingPowCallback: Consumer<PowStatus>, waitForRequiredPlasma: Boolean
    ): AccountBlockTemplate {
        checkAndSetFields(transaction, currentKeyPair)
        setDifficulty(transaction, generatingPowCallback, waitForRequiredPlasma)
        setHashAndSignature(transaction, currentKeyPair)
        Zenon.instance.ledger.publishRawTransaction(transaction)
        LOGGER.info("Published account-block")
        return transaction
    }

    fun requiresPoW(transaction: AccountBlockTemplate, blockSigningKey: KeyPair): Boolean {
        transaction.address = blockSigningKey.address
        val powParam = GetRequiredParam(
            address = transaction.address,
            blockType = transaction.blockType,
            toAddress = transaction.toAddress,
            data = transaction.data
        )
        val response: GetRequiredResponse = Zenon.instance.embedded.plasma
            .getRequiredPoWForAccountBlock(powParam)
        return response.requiredDifficulty != 0
    }
}

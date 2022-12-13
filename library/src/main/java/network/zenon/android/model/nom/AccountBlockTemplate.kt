package network.zenon.android.model.nom

import network.zenon.android.NET_ID
import network.zenon.android.model.primitives.*
import network.zenon.android.utils.BytesUtils
import org.json.JSONObject


enum class BlockTypeEnum {
    UNKNOWN,
    GENESIS_RECEIVE,
    USER_SEND,
    USER_RECEIVE,
    CONTRACT_SEND,
    CONTRACT_RECEIVE;
}

open class AccountBlockTemplate(
    var blockType: BlockTypeEnum,
    // Send information
    var toAddress: Address = EMPTY_ADDRESS,
    var amount: Int = 0,
    var tokenStandard: TokenStandard = TokenStandard.parse(EMPTY_TOKEN_STANDARD),
    // Receive information
    var fromBlockHash: Hash = EMPTY_HASH,
    var data: ByteArray = byteArrayOf()
) {
    constructor() : this(blockType = BlockTypeEnum.UNKNOWN)

    var version: Int = 1
    var chainIdentifier: Int = NET_ID

    var hash: Hash = EMPTY_HASH
    var previousHash: Hash = EMPTY_HASH
    var height: Int = 0
    var momentumAcknowledged: HashHeight = EMPTY_HASH_HEIGHT

    var address: Address = EMPTY_ADDRESS

    // PoW
    var fusedPlasma: Int = 0
    var difficulty: Int = 0

    // Hex representation of 8 byte nonce
    var nonce: String = ""

    // Verification
    var publicKey: ByteArray = byteArrayOf()
    var signature: ByteArray = byteArrayOf()

    companion object {
        fun fromJson(json: JSONObject): AccountBlockTemplate {
            val template = AccountBlockTemplate()
            template.blockType = BlockTypeEnum.values().getOrElse(
                json.optInt("blockType", -1)
            ) { BlockTypeEnum.UNKNOWN }
            template.toAddress = Address.parse(json.optString("toAddress"))
            template.amount = json.optInt("amount")
            template.tokenStandard = TokenStandard.parse(json.optString("tokenStandard"))

            template.version = json.optInt("version")

            template.fromBlockHash = Hash.parse(
                json.optString("fromBlockHash", template.fromBlockHash.toString())
            )
            template.chainIdentifier = json.optInt("chainIdentifier", template.chainIdentifier)
            template.hash = Hash.parse(json.optString("hash", template.hash.toString()))
            template.previousHash = Hash.parse(json.optString("previousHash", template.previousHash.toString()))
            template.height = json.optInt("height", template.height)
            template.momentumAcknowledged = HashHeight.fromJson(
                json.optJSONObject("momentumAcknowledged")
                    ?: template.momentumAcknowledged.toJson()
            )
            template.address = Address.parse(json.optString("address", template.address.toString()))

            template.fusedPlasma = json.optInt("fusedPlasma", template.fusedPlasma)
            template.data = if (json.optString("data").isNotEmpty())
                BytesUtils.base64ToBytes(json.getString("data"))
            else template.data
            template.difficulty = json.optInt("difficulty", template.difficulty)
            template.nonce = json.optString("nonce",  template.nonce)
            template.publicKey = if (json.optString("publicKey").isNotEmpty())
                BytesUtils.base64ToBytes(json.getString("publicKey"))
            else template.publicKey
            template.signature = if (json.optString("signature").isNotEmpty())
                BytesUtils.base64ToBytes(json.getString("signature"))
            else template.signature

            return template
        }

        fun receive(fromBlockHash: Hash): AccountBlockTemplate {
            return AccountBlockTemplate(
                blockType = BlockTypeEnum.USER_RECEIVE,
                fromBlockHash = fromBlockHash
            )
        }

        fun send(
            toAddress: Address,
            tokenStandard: TokenStandard,
            amount: Int,
            data: ByteArray = byteArrayOf()
        ): AccountBlockTemplate {
            return AccountBlockTemplate(
                blockType = BlockTypeEnum.USER_SEND,
                toAddress = toAddress,
                tokenStandard = tokenStandard,
                amount = amount,
                data = data
            )
        }

        fun callContract(
            toAddress: Address,
            tokenStandard: TokenStandard,
            amount: Int,
            data: ByteArray
        ): AccountBlockTemplate {
            return AccountBlockTemplate(
                blockType = BlockTypeEnum.USER_SEND,
                toAddress = toAddress,
                tokenStandard = tokenStandard,
                amount = amount,
                data = data
            )
        }
    }

    open fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("version", version)
        json.put("chainIdentifier", chainIdentifier)
        json.put("blockType", blockType.ordinal)
        json.put("hash", hash.toString())
        json.put("previousHash", previousHash.toString())
        json.put("height", height)
        json.put("momentumAcknowledged", momentumAcknowledged.toJson())
        json.put("address", address.toString())
        json.put("toAddress", toAddress.toString())
        json.put("amount", amount)
        json.put("tokenStandard", tokenStandard.toString())
        json.put("fromBlockHash", fromBlockHash.toString())
        json.put("data", BytesUtils.bytesToBase64(data))
        json.put("fusedPlasma", fusedPlasma)
        json.put("difficulty", difficulty)
        json.put("nonce", nonce)
        json.put("publicKey", BytesUtils.bytesToBase64(publicKey))
        json.put("signature", BytesUtils.bytesToBase64(signature))
        return json
    }
}


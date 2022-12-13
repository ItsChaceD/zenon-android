@file:Suppress("unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused"
)

package network.zenon.android.abi

import network.zenon.android.ZnnSdkException
import network.zenon.android.crypto.Crypto
import org.json.JSONArray

enum class TypeEnum {
    FUNCTION
}

class Param {
    var indexed: Boolean = false
    var name: String? = null
    lateinit var type: AbiType

    companion object {
        fun decodeList(params: List<Param>, encoded: List<*>): List<*> {
            val result = mutableListOf<Any>()
            val bytes = encoded.joinToString(",").toByteArray()
            var offset = 0

            for(param in params) {
                val decoded = if(param.type.isDynamicType())
                    param.type.decode(bytes, IntType.decodeInt(bytes, offset).toInt())
                    else param.type.decode(bytes, offset)
                result.add(decoded)

                offset += param.type.getFixedSize()!!
            }

            return result.toList()
        }
    }
}

open class Entry(
    var name: String?,
    var inputs: List<Param>?,
    var type: TypeEnum?
) {
    fun formatSignature(): String {
        var paramsTypes = ""
        for (param in inputs!!) {
            paramsTypes += "${param.type.getCanonicalName()!!},"
        }

        var x = paramsTypes
        if (x.endsWith(',')) {
            x = x.substring(0, x.length - 1)
        }

        return "${name!!}($x)"
    }

    fun fingerprintSignature(): ByteArray {
        return Crypto.digest(formatSignature().toByteArray())
    }

    open fun encodeSignature(): ByteArray {
        return fingerprintSignature()
    }

    fun encodeArguments(vararg args: Any): ByteArray {
        if (args.size > inputs!!.size) throw Error()

        var staticSize = 0
        var dynamicCnt = 0
        for (i in args.indices) {
            val type = inputs!![i].type
            if (type.isDynamicType()) {
                dynamicCnt++
            }
            staticSize += type.getFixedSize()!!
        }

        val bb = Array(args.size + dynamicCnt) { ByteArray(0) }

        for (i in args.indices + dynamicCnt) {
            bb[i] = byteArrayOf()
        }

        var curDynamicPtr = staticSize
        var curDynamicCnt = 0
        for (i in args.indices) {
            val type = inputs!![i].type
            if (type.isDynamicType()) {
                val dynBB = type.encode(args[i])
                bb[i] = IntType.encodeInt(curDynamicPtr)
                bb[args.size + curDynamicCnt] = dynBB
                curDynamicCnt++
                curDynamicPtr += dynBB.size
            } else {
                bb[i] = type.encode(args[i])
            }
        }

        return bb.flatMap { it.toList() }.toByteArray()
    }
}

class AbiFunction(
    name: String,
    inputs: List<Param>
): Entry(
    name = name,
    inputs = inputs,
    type = TypeEnum.FUNCTION
) {
    companion object {
        const val ENCODED_SIGN_LENGTH = 4

        fun extractSignature(data: ByteArray): ByteArray {
            return data.sliceArray(0 until ENCODED_SIGN_LENGTH)
        }
    }

    fun decode(encoded: ByteArray): List<*> {
        return Param.decodeList(inputs!!, encoded.slice(ENCODED_SIGN_LENGTH until encoded.size))
    }

    fun encode(vararg args: Any): ByteArray {
        return encodeSignature() + encodeArguments(args)
    }

    override fun encodeSignature(): ByteArray {
        return extractSignature(super.encodeSignature())
    }
}

class Abi(
    var entries: List<Entry> = listOf()
) {
    companion object {
        private fun parseEntries(jsonString: String): List<Entry> {
            val entries = mutableListOf<Entry>()
            val jsons = JSONArray(jsonString)

            for(i in 0 until jsons.length()) {
                val json = jsons.getJSONObject(i)

                if(json.getString("type") != "function") {
                    throw ZnnSdkException("Only ABI functions supported")
                }

                val inputs = mutableListOf<Param>()
                val jsonInputs = json.getJSONArray("inputs")

                for(j in 0 until jsonInputs.length()) {
                    val x = jsonInputs.getJSONObject(j)
                    val p = Param()
                    p.name = x.getString("name")
                    p.type = AbiType.getType(x.getString("type"))
                    inputs.add(p)
                }

                entries.add(AbiFunction(json.getString("name"), inputs))
            }

            return entries.toList()
        }

        fun fromJson(j: String): Abi {
            return Abi(
                entries = parseEntries(j)
            )
        }
    }

    fun encodeFunction(name: String, vararg args: Any): ByteArray {
        var f: AbiFunction? = null

        entries.forEach {
            if (it.name == name) {
                f = AbiFunction(name, it.inputs!!)
            }
        }

        return f!!.encode(args)
    }

    fun decodeFunction(encoded: ByteArray): Any {
        var f: AbiFunction? = null

        entries.forEach {
            if (AbiFunction.extractSignature(it.encodeSignature()).toString() ==
                AbiFunction.extractSignature(encoded).toString()) {
                f = AbiFunction(it.name!!, it.inputs!!)
            }
        }

        return f!!.decode(encoded)
    }
}
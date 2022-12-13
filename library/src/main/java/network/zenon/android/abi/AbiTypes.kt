package network.zenon.android.abi

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import network.zenon.android.model.primitives.TokenStandard
import network.zenon.android.utils.BytesUtils
import org.json.JSONArray
import java.math.BigInteger
import java.util.*

open class IntType(name: String): NumericType(name) {
    override fun getCanonicalName(): String? {
        if(name == "int")   return "int256"
        return super.getCanonicalName()
    }

    companion object {
        fun encodeInt(i: Int): ByteArray {
            return encodeIntBig(BigInteger.valueOf(i.toLong()))
        }

        fun encodeIntBig(bigInt: BigInteger): ByteArray {
            return BytesUtils.bigIntToBytesSigned(bigInt, INT32_SIZE)
        }

        fun decodeInt(encoded: ByteArray, offset: Int): BigInteger {
            return BytesUtils.decodeBigInt(encoded.sliceArray(offset until offset + 32))
        }
    }

    override fun encode(value: Any?): ByteArray {
        return encodeIntBig(encodeInternal(value))
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        return decodeInt(encoded, origOffset)
    }
}

abstract class ArrayType(name: String) : AbiType(name) {
    companion object {
        fun getType(typeName: String): ArrayType {
            val idx1 = typeName.indexOf('[')
            val idx2 = typeName.indexOf(']', idx1)
            return if (idx1 + 1 == idx2) {
                DynamicArrayType(typeName)
            } else {
                StaticArrayType(typeName)
            }
        }
    }

    private val idx = name.indexOf('[')
    private val st = name.substring(0, idx)
    private val idx2 = name.indexOf(']', idx)
    private val subDim = if (idx2 + 1 == name.length) "" else name.substring(idx2 + 1)
    open var elementType: AbiType = AbiType.getType(st + subDim)

    override fun encode(value: Any?): ByteArray {
        return when (value) {
            is List<*> -> {
                encodeList(value)
            }
            is String -> {
                val array = JSONArray(value)
                val elems = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    elems.add(array.get(i).toString())
                }
                encodeList(elems)
            }
            else -> {
                throw Error()
            }
        }
    }

    fun encodeTuple(value: Any?): ByteArray {
        val l = value as List<*>
        val elems: MutableList<ByteArray?>
        if (elementType.isDynamicType()) {
            elems = MutableList(l.size * 2) { null }
            var offset: Int = l.size * INT32_SIZE
            for (i in l.indices) {
                elems[i] = IntType.encodeInt(offset)
                val encoded: ByteArray = elementType.encode(l[i])
                elems[l.size + i] = encoded
                offset += getFixedSize()!! * ((encoded.size - 1) / getFixedSize()!! + 1)
            }
        } else {
            elems = MutableList(l.size) { null }
            for (i in l.indices) {
                elems[i] = elementType.encode(l[i])
            }
        }
        return elems.mapNotNull{ it }.fold(ByteArray(0)) { acc, element -> acc + element }
    }

    open fun decodeTuple(encoded: ByteArray, origOffset: Int): Any {
        val len = encoded.size
        var offset = origOffset
        val ret = ArrayList<ByteArray>()

        for (i in 0 until len) {
            if (elementType.isDynamicType()) {
                ret[i] = elementType.decode(
                    encoded,
                    offset + IntType.decodeInt(encoded, offset).toInt()
                ) as ByteArray
            } else {
                ret[i] = elementType.decode(encoded, offset) as ByteArray
            }

            offset += elementType.getFixedSize()!!
        }

        return ret
    }

    abstract fun encodeList(l: List<*>): ByteArray
}

class StaticArrayType(name: String) : ArrayType(name) {
    private val idx1 = name.indexOf('[')
    private val idx2 = name.indexOf(']', idx1)
    private val dim = name.substring(idx1 + 1, idx2)
    var size = dim.toInt()

    override fun getCanonicalName(): String {
        return "${elementType.getCanonicalName()}[$size]"
    }

    override fun encodeList(l: List<*>): ByteArray {
        if(l.size != size)  throw Error()
        return encodeTuple(l)
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val result: MutableList<ByteArray> = MutableList(size){ byteArrayOf() }

        for(i in 0 until size) {
            result[i] = elementType.decode(encoded, origOffset + i * elementType.getFixedSize()!!) as ByteArray
        }

        return result.toList()
    }

    override fun getFixedSize(): Int? {
        return elementType.getFixedSize()?.times(size)
    }
}

class DynamicArrayType(name: String) : ArrayType(name) {
    override fun getCanonicalName(): String {
        return elementType.getCanonicalName() + "[]"
    }

    override fun encodeList(l: List<*>): ByteArray {
        return IntType.encodeInt(l.size) + encodeTuple(l)
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val len = IntType.decodeInt(encoded, origOffset).toInt()
        var offset = origOffset + 32
        val ret = mutableListOf<ByteArray>()

        for (i in 0 until len) {
            if (elementType.isDynamicType()) {
                ret[i] = elementType.decode(
                    encoded,
                    offset + IntType.decodeInt(encoded, offset).toInt()
                ) as ByteArray
            } else {
                ret[i] = elementType.decode(encoded, offset) as ByteArray
            }

            offset += elementType.getFixedSize()!!
        }

        return ret
    }

    override fun isDynamicType(): Boolean {
        return true
    }
}

open class BytesType(name: String): AbiType(name) {
    companion object {
        fun bytes(): BytesType {
            return BytesType("bytes")
        }
    }

    override fun encode(value: Any?): ByteArray {
        val bb: ByteArray = when (value) {
            is ByteArray -> {
                value
            }
            is String -> {
                BytesUtils.hexToBytes(value)
            }
            else -> {
                throw Error()
            }
        }

        val ret = MutableList(((bb.size / INT32_SIZE) + 1) * INT32_SIZE) { 0 }

        for(i in bb.indices) {
            ret[i] = bb[i].toUByte().toInt()
        }

        return IntType.encodeInt(bb.size) + ret.fold(ByteArray(0)) { acc, element -> acc + element.toByte() }
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val len = IntType.decodeInt(encoded, origOffset).toInt()
        if(len == 0) {
            return byteArrayOf()
        }
        val offset = origOffset + 32

        val l = MutableList(len){ 0.toByte() }

        for(i in 0 until len) {
            l[i] = encoded[offset + i]
        }

        return l.toByteArray()
    }

    override fun isDynamicType(): Boolean {
        return true
    }
}

class StringType: BytesType("string") {
    override fun encode(value: Any?): ByteArray {
        if(value !is String) throw Error()
        return super.encode(value.encodeToByteArray())
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        return String(super.decode(encoded, origOffset) as ByteArray)
    }
}

open class Bytes32Type(name: String): AbiType(name) {
    override fun encode(value: Any?): ByteArray {
        when (value) {
            is Number -> {
                val bigInt = BigInteger.valueOf(value.toLong())
                return IntType.encodeIntBig(bigInt)
            }
            is String -> {
                val ret = MutableList(INT32_SIZE){0.toByte()}
                val bytes = BytesUtils.hexToBytes(value)

                for(i in bytes.indices) {
                    ret[i] = bytes[i]
                }

                return ret.toByteArray()
            }
            is ByteArray -> {
                val ret = MutableList(INT32_SIZE){0.toByte()}

                for (i in value.indices) {
                    ret[i] = value[i]
                }

                return ret.toByteArray()
            }
            else -> throw Error()
        }

    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val ret = MutableList(INT32_SIZE){0.toByte()}

        for(i in 0 until getFixedSize()!!) {
            ret[i] = encoded[origOffset + i]
        }

        return ret.toByteArray()
    }
}

class HashType(name: String): AbiType(name) {
    override fun encode(value: Any?): ByteArray {
        when (value) {
            is Number -> {
                val bigInt = BigInteger.valueOf(value.toLong())
                return IntType.encodeIntBig(bigInt)
            }
            is String -> {
                val ret = MutableList(INT32_SIZE){0.toByte()}
                val bytes = BytesUtils.hexToBytes(value)

                for(i in bytes.indices) {
                    ret[i] = bytes[i]
                }

                return ret.toByteArray()
            }
            is ByteArray -> {
                val ret = MutableList(INT32_SIZE){0.toByte()}

                for (i in value.indices) {
                    ret[i] = value[i]
                }

                return ret.toByteArray()
            }
            else -> throw Error()
        }

    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val ret = MutableList(INT32_SIZE){0.toByte()}

        for(i in 0 until getFixedSize()!!) {
            ret[i] = encoded[origOffset + i]
        }

        return Hash.digest(ret.toByteArray())
    }
}

class TokenStandardType: IntType("tokenStandard") {
    override fun encode(value: Any?): ByteArray {
        if(value is String) {
            return BytesUtils.leftPadBytes(
                TokenStandard.parse(value).getBytes(), 32
            )
        }

        if (value is TokenStandard) {
            return BytesUtils.leftPadBytes(value.getBytes(), 32)
        }

        throw Error()
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val ret = MutableList(10){0.toByte()}
        val offset = origOffset + 22

        for(i in 0 until 10) {
            ret[i] = encoded[offset +  i]
        }

        return TokenStandard.fromBytes(ret.toByteArray())
    }
}

class UnsignedIntType(name: String): NumericType(name) {
    override fun getCanonicalName(): String? {
        if(name == "uint")  return "uint256"
        return super.getCanonicalName()
    }

    companion object {
        fun decodeInt(encoded: ByteArray, offset: Int): BigInteger {
            return BytesUtils.decodeBigInt(encoded.sliceArray(offset until offset + 32))
        }

        fun encodeInt(i: Int): ByteArray {
            return encodeIntBig(BigInteger.valueOf(i.toLong()))
        }

        fun encodeIntBig(bigInt: BigInteger): ByteArray {
            if (bigInt.signum() == -1)  throw Error()
            return BytesUtils.bigIntToBytesSigned(bigInt, INT32_SIZE)
        }
    }

    override fun encode(value: Any?): ByteArray {
        return encodeIntBig(encodeInternal(value))
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        return decodeInt(encoded, origOffset)
    }
}

class BoolType: IntType("bool") {
    override fun encode(value: Any?): ByteArray {
        if(value is String) {
            return super.encode(if(value == "true") 1 else 0)
        }
        else if(value is Boolean) {
            return super.encode(if(value == true) 1 else 0)
        }

        throw Error()
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        return super.decode(encoded, origOffset).toString() != "0"
    }
}

class FunctionType: Bytes32Type("function") {
    override fun encode(value: Any?): ByteArray {
        if(value !is List<*> || value.size != 24)   throw Error()
        val bytes = value.joinToString(",").toByteArray()
        return super.encode(bytes + ByteArray(8))
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        throw NotImplementedError()
    }
}

abstract class NumericType(name: String) : AbiType(name) {
    protected fun encodeInternal(value: Any?): BigInteger {
        val bigInt: BigInteger
        when (value) {
            is String -> {
                var s = value.lowercase(Locale.getDefault()).trim()
                var radix = 10
                if (s.startsWith("0x")) {
                    s = s.substring(2)
                    radix = 16
                } else if (s.contains('a') ||
                    s.contains('b') ||
                    s.contains('c') ||
                    s.contains('d') ||
                    s.contains('e') ||
                    s.contains('f')) {
                    radix = 16
                }
                bigInt = BigInteger(s, radix)
            }
            is BigInteger -> {
                bigInt = value
            }
            is Number -> {
                bigInt = BigInteger.valueOf(value.toLong())
            }
            is List<*> -> {
                bigInt = BytesUtils.bytesToBigInt(value.joinToString(",").toByteArray())
            }
            else -> {
                throw Error()
            }
        }
        return bigInt
    }
}

class AddressType: IntType("address") {
    override fun encode(value: Any?): ByteArray {
        if(value is String) {
            return BytesUtils.leftPadBytes(Address.parse(value).getBytes()!!, 32)
        }
        else if(value is Address) {
            return BytesUtils.leftPadBytes(value.getBytes()!!, 32)
        }

        throw Error()
    }

    override fun decode(encoded: ByteArray, origOffset: Int): Any {
        val l = MutableList(20){0.toByte()}
        val offset = origOffset + 12

        for(i in 0 until 20) {
            l[i] = encoded[offset + i]
        }

        return Address(
            hrp = "z",
            core = l.toByteArray()
        )
    }
}

abstract class AbiType(initName: String) {
    open var name: String? = initName

    companion object {
        const val INT32_SIZE = 32
        fun getType(typeName: String): AbiType {
            if (typeName.contains('[')) return ArrayType.getType(typeName)
            if ("bool" == typeName) return BoolType()
            if (typeName.startsWith("int")) return IntType(typeName)
            if (typeName.startsWith("uint")) return UnsignedIntType(typeName)
            if ("address" == typeName) return AddressType()
            if ("tokenStandard" == typeName) return TokenStandardType()
            if ("string" == typeName) return StringType()
            if ("bytes" == typeName) return BytesType.bytes()
            if ("function" == (typeName)) return FunctionType()
            if ("hash" == (typeName)) return HashType(typeName)
            if (typeName.startsWith("bytes")) return Bytes32Type(typeName)

            throw UnsupportedOperationException("The type $typeName is not supported")
        }
    }

    abstract fun encode(value: Any?): ByteArray

    abstract fun decode(encoded: ByteArray, origOffset: Int = 0): Any

    open fun getCanonicalName(): String? {
        return name
    }

    open fun getFixedSize(): Int? {
        return 32
    }

    open fun isDynamicType(): Boolean {
        return false
    }

    override fun toString(): String {
        return name!!
    }
}

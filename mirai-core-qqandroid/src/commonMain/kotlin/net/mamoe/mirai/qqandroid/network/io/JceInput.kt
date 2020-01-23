package net.mamoe.mirai.qqandroid.network.io

import kotlinx.io.charsets.Charset
import kotlinx.io.core.*
import kotlinx.io.pool.ObjectPool
import net.mamoe.mirai.utils.io.readString
import net.mamoe.mirai.utils.io.toIoBuffer

@UseExperimental(ExperimentalUnsignedTypes::class)
inline class JceHead(private val value: Long) {
    constructor(tag: Int, type: Byte) : this(tag.toLong().shl(32) or type.toLong())

    val tag: Int get() = (value ushr 32).toInt()
    val type: Byte get() = value.toUInt().toByte()

    override fun toString(): String {
        return "JceHead(tag=$tag, type=$type)"
    }
}

fun ByteArray.asJceInput(charset: Charset = CharsetGBK): JceInput = JceInput(this.toIoBuffer(), charset)

@Suppress("MemberVisibilityCanBePrivate")
@UseExperimental(ExperimentalUnsignedTypes::class)
class JceInput(
    @PublishedApi
    internal val input: IoBuffer,
    private val charset: Charset = CharsetGBK,
    private val pool: ObjectPool<IoBuffer> = IoBuffer.Pool
) : Closeable {

    constructor(input: Input) : this(IoBuffer.Pool.borrow().also { input.readAvailable(it) })

    override fun close() {
        input.release(pool)
    }

    @PublishedApi
    internal fun readHead(): JceHead = input.readHead()

    @PublishedApi
    internal fun peakHead(): JceHead = input.makeView().readHead()

    private fun IoBuffer.readHead(): JceHead {
        val var2 = readUByte()
        val type = var2 and 15u
        var tag = var2.toUInt() shr 4
        if (tag == 15u)
            tag = readUByte().toUInt()
        return JceHead(tag = tag.toInt(), type = type.toByte())
    }

    fun read(default: Byte, tag: Int): Byte = readByteOrNull(tag) ?: default
    fun read(default: Short, tag: Int): Short = readShortOrNull(tag) ?: default
    fun read(default: Int, tag: Int): Int = readIntOrNull(tag) ?: default
    fun read(default: Long, tag: Int): Long = readLongOrNull(tag) ?: default
    fun read(default: Float, tag: Int): Float = readFloatOrNull(tag) ?: default
    fun read(default: Double, tag: Int): Double = readDoubleOrNull(tag) ?: default
    fun read(default: Boolean, tag: Int): Boolean = readBooleanOrNull(tag) ?: default

    fun read(default: ByteArray, tag: Int): ByteArray = readByteArrayOrNull(tag) ?: default
    fun read(default: ShortArray, tag: Int): ShortArray = readShortArrayOrNull(tag) ?: default
    fun read(default: IntArray, tag: Int): IntArray = readIntArrayOrNull(tag) ?: default
    fun read(default: LongArray, tag: Int): LongArray = readLongArrayOrNull(tag) ?: default
    fun read(default: FloatArray, tag: Int): FloatArray = readFloatArrayOrNull(tag) ?: default
    fun read(default: DoubleArray, tag: Int): DoubleArray = readDoubleArrayOrNull(tag) ?: default
    fun read(default: BooleanArray, tag: Int): BooleanArray = readBooleanArrayOrNull(tag) ?: default

    fun readBoolean(tag: Int): Boolean = readBooleanOrNull(tag) ?: error("cannot find tag $tag")
    fun readByte(tag: Int): Byte = readByteOrNull(tag) ?: error("cannot find tag $tag")
    fun readShort(tag: Int): Short = readShortOrNull(tag) ?: error("cannot find tag $tag")
    fun readInt(tag: Int): Int = readIntOrNull(tag) ?: error("cannot find tag $tag")
    fun readLong(tag: Int): Long = readLongOrNull(tag) ?: error("cannot find tag $tag")
    fun readFloat(tag: Int): Float = readFloatOrNull(tag) ?: error("cannot find tag $tag")
    fun readDouble(tag: Int): Double = readDoubleOrNull(tag) ?: error("cannot find tag $tag")

    fun readString(tag: Int): String = readStringOrNull(tag) ?: error("cannot find tag $tag")

    fun readByteArray(tag: Int): ByteArray = readByteArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun readShortArray(tag: Int): ShortArray = readShortArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun readLongArray(tag: Int): LongArray = readLongArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun readFloatArray(tag: Int): FloatArray = readFloatArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun readDoubleArray(tag: Int): DoubleArray = readDoubleArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun readIntArray(tag: Int): IntArray = readIntArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun readBooleanArray(tag: Int): BooleanArray = readBooleanArrayOrNull(tag) ?: error("cannot find tag $tag")
    fun <K, V> readMap(defaultKey: K, defaultValue: V, tag: Int): Map<K, V> = readMapOrNull(defaultKey, defaultValue, tag) ?: error("cannot find tag $tag")
    fun <T> readList(defaultElement: T, tag: Int): List<T> = readListOrNull(defaultElement, tag) ?: error("cannot find tag $tag")
    inline fun <reified T> readSimpleArray(defaultElement: T, tag: Int): Array<T> = readArrayOrNull(defaultElement, tag) ?: error("cannot find tag $tag")
    fun <J : JceStruct> readJceStruct(factory: JceStruct.Factory<J>, tag: Int): J = readJceStructOrNull(factory, tag) ?: error("cannot find tag $tag")
    fun readStringArray(tag: Int): Array<String> = readArrayOrNull("", tag) ?: error("cannot find tag $tag")

    fun readLongOrNull(tag: Int): Long? = skipToTagOrNull(tag) {
        return when (it.type.toInt()) {
            12 -> 0
            0 -> input.readByte().toLong()
            1 -> input.readShort().toLong()
            2 -> input.readInt().toLong()
            3 -> input.readLong()
            else -> error("type mismatch: ${it.type}")
        }
    }

    fun readShortOrNull(tag: Int): Short? = skipToTagOrNull(tag) {
        return when (it.type.toInt()) {
            12 -> 0
            0 -> input.readByte().toShort()
            1 -> input.readShort()
            else -> error("type mismatch: ${it.type}")
        }
    }

    fun readIntOrNull(tag: Int): Int? = skipToTagOrNull(tag) {
        return when (it.type.toInt()) {
            12 -> 0
            0 -> input.readByte().toInt()
            1 -> input.readShort().toInt()
            2 -> input.readInt()
            else -> error("type mismatch: ${it.type}")
        }
    }

    fun readByteOrNull(tag: Int): Byte? = skipToTagOrNull(tag) {
        return when (it.type.toInt()) {
            12 -> 0
            0 -> input.readByte()
            else -> error("type mismatch")
        }
    }

    fun readFloatOrNull(tag: Int): Float? = skipToTagOrNull(tag) {
        return when (it.type.toInt()) {
            12 -> 0f
            4 -> input.readFloat()
            else -> error("type mismatch: ${it.type}")
        }
    }

    fun readDoubleOrNull(tag: Int): Double? = skipToTagOrNull(tag) {
        return when (it.type.toInt()) {
            12 -> 0.0
            4 -> input.readFloat().toDouble()
            5 -> input.readDouble()
            else -> error("type mismatch: ${it.type}")
        }
    }

    fun readBooleanOrNull(tag: Int): Boolean? = this.readByteOrNull(tag)?.let { it.toInt() != 0 }

    fun readByteArrayOrNull(tag: Int): ByteArray? = skipToTagOrNull(tag) {
        when (it.type.toInt()) {
            9 -> ByteArray(readInt(0)) { readByte(0) }
            13 -> {
                val head = readHead()
                check(head.type.toInt() == 0) { "type mismatch" }
                input.readBytes(readInt(0))
            }
            else -> error("type mismatch")
        }
    }

    fun readShortArrayOrNull(tag: Int): ShortArray? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        ShortArray(readInt(0)) { readShort(0) }
    }

    fun readDoubleArrayOrNull(tag: Int): DoubleArray? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        DoubleArray(readInt(0)) { readDouble(0) }
    }

    fun readFloatArrayOrNull(tag: Int): FloatArray? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        FloatArray(readInt(0)) { readFloat(0) }
    }

    fun readIntArrayOrNull(tag: Int): IntArray? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        IntArray(readInt(0)) { readInt(0) }
    }

    fun readLongArrayOrNull(tag: Int): LongArray? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        LongArray(readInt(0)) { readLong(0) }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> readArrayOrNull(tag: Int): Array<T>? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        Array(readInt(0)) { readSimpleObject<T>(0) }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> readArrayOrNull(defaultElement: T, tag: Int): Array<T>? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        Array(readInt(0)) { readObject(defaultElement, 0) }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified J : JceStruct> readJceStructArrayOrNull(factory: JceStruct.Factory<J>, tag: Int): Array<J>? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        Array(readInt(0)) { readJceStruct(factory, 0) }
    }

    fun readBooleanArrayOrNull(tag: Int): BooleanArray? = skipToTagOrNull(tag) {
        require(it.type.toInt() == 9) { "type mismatch" }
        BooleanArray(readInt(0)) { readBoolean(0) }
    }

    fun readStringOrNull(tag: Int): String? = skipToTagOrNull(tag) { head ->
        return when (head.type.toInt()) {
            6 -> input.readString(input.readUByte().toInt(), charset = charset)
            7 -> input.readString(input.readUInt().toInt().also { require(it in 1 until 104857600) { "bad string length: $it" } }, charset = charset)
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun <K, V> readMapOrNull(defaultKey: K, defaultValue: V, tag: Int): Map<K, V>? = skipToTagOrNull(tag) {
        check(it.type.toInt() == 8) { "type mismatch: ${it.type}" }
        val size = readInt(0)
        val map = HashMap<K, V>(size)
        repeat(size) {
            map[readObject(defaultKey, 0)] = readObject(defaultValue, 1)
        }
        return map
    }

    inline fun <reified K, reified V> readMapOrNull(tag: Int): Map<K, V>? = skipToTagOrNull(tag) {
        check(it.type.toInt() == 8) { "type mismatch" }
        val size = readInt(0)
        val map = HashMap<K, V>(size)
        repeat(size) {
            map[readSimpleObject(0)] = readSimpleObject(1)
        }
        return map
    }

    fun <T> readListOrNull(defaultElement: T, tag: Int): List<T>? = skipToTagOrNull(tag) { head ->
        check(head.type.toInt() == 9) { "type mismatch" }
        val size = readInt(0)
        val list = ArrayList<T>(size)
        repeat(size) {
            list.add(readObject(defaultElement, 0))
        }
        return list
    }

    fun <J : JceStruct> readJceStructOrNull(factory: JceStruct.Factory<J>, tag: Int): J? = skipToTagOrNull(tag) { head ->
        check(head.type.toInt() == 10) { "type mismatch" }
        return factory.newInstanceFrom(this).also { skipToStructEnd() }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> readArrayOrNull(default: Array<T>, tag: Int): Array<T>? = skipToTagOrNull(tag) { head ->
        val defaultElement = default[0]
        check(head.type.toInt() == 9) { "type mismatch" }
        return Array(readInt(0)) { readObject(defaultElement, tag) as Any } as Array<T>
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun <T> readObject(default: T, tag: Int): T = when (default) {
        is Byte -> readByte(tag)
        is Boolean -> readBoolean(tag)
        is Short -> readShort(tag)
        is Int -> readInt(tag)
        is Long -> readLong(tag)
        is Float -> readFloat(tag)
        is Double -> readDouble(tag)
        is String -> readString(tag)
        is BooleanArray -> readBooleanArray(tag)
        is ShortArray -> readShortArray(tag)
        is IntArray -> readIntArray(tag)
        is LongArray -> readLongArray(tag)
        is ByteArray -> readByteArray(tag)
        is FloatArray -> readByteArray(tag)
        is DoubleArray -> readDoubleArrayOrNull(tag)
        is List<*> -> {
            readList(default, tag)
        }
        is Map<*, *> -> {
            val entry = default.entries.first()
            readMap(entry.key, entry.value, tag)
        }
        is Array<*> -> readSimpleArray(default, tag)
        else -> error("unsupported type")
    } as T

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    inline fun <reified T> readSimpleObject(tag: Int): T = when (T::class) {
        Byte::class -> readByte(tag)
        Boolean::class -> readBoolean(tag)
        Short::class -> readShort(tag)
        Int::class -> readInt(tag)
        Long::class -> readLong(tag)
        Float::class -> readFloat(tag)
        Double::class -> readDouble(tag)

        String::class -> readString(tag)

        BooleanArray::class -> readBooleanArray(tag)
        ShortArray::class -> readShortArray(tag)
        IntArray::class -> readIntArray(tag)
        LongArray::class -> readLongArray(tag)
        ByteArray::class -> readByteArray(tag)
        FloatArray::class -> readByteArray(tag)
        DoubleArray::class -> readDoubleArrayOrNull(tag)
        else -> error("Type is not supported: ${T::class.simpleName}")
    } as T

    private fun skipField() {
        skipField(readHead().type)
    }

    private fun skipToStructEnd() {
        var head: JceHead
        do {
            head = readHead()
            skipField(head.type)
        } while (head.type.toInt() != 11)
    }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    @PublishedApi
    internal fun skipField(type: Byte) = when (type.toInt()) {
        0 -> this.input.discardExact(1)
        1 -> this.input.discardExact(2)
        2 -> this.input.discardExact(4)
        3 -> this.input.discardExact(8)
        4 -> this.input.discardExact(4)
        5 -> this.input.discardExact(8)
        6 -> this.input.discardExact(this.input.readUByte().toInt())
        7 -> this.input.discardExact(this.input.readInt())
        8 -> { // map
            repeat(this.readInt(0) * 2) {
                skipField()
            }
        }
        9 -> { // list
            repeat(this.readInt(0)) {
                skipField()
            }
        }
        10 -> this.skipToStructEnd()
        11, 12 -> {

        }
        13 -> {
            val head = readHead()
            check(head.type.toInt() == 0) { "skipField with invalid type, type value: " + type + ", " + head.type }
            this.input.discardExact(this.readInt(0))
        }
        else -> error("invalid type: $type")
    }
}

private inline fun <R> JceInput.skipToTag(tag: Int, block: (JceHead) -> R): R {
    return skipToTagOrNull(tag) { block(it) } ?: error("cannot find required tag $tag")
}

@PublishedApi
internal inline fun <R> JceInput.skipToTagOrNull(tag: Int, block: (JceHead) -> R): R? {
    while (true) {
        if (this.input.endOfInput) {
            println("endOfInput")
            return null
        }

        val head = peakHead()
        if (head.tag > tag) {
            return null
        }
        readHead()
        if (head.tag == tag) {
            return block(head)
        }
        this.skipField(head.type)
    }
}
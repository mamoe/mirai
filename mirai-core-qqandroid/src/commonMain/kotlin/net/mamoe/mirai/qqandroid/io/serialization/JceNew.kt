/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization

import io.ktor.utils.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.BYTE
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.FLOAT
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.INT
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.LONG
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.SHORT
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.STRING1
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.STRING4
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.ZERO_TYPE
import net.mamoe.mirai.utils.io.readString

interface IOFormat : SerialFormat {

    fun <T> dump(serializer: SerializationStrategy<T>, input: Input): ByteArray

    fun <T> load(deserializer: DeserializationStrategy<T>, output: Output): T
}

/**
 * Jce 数据结构序列化和反序列化器.
 *
 * @author Him188
 */
class JceNew(
    override val context: SerialModule
) : SerialFormat, IOFormat {
    companion object Default : IOFormat by JceNew(EmptyModule)

    override fun <T> dump(serializer: SerializationStrategy<T>, input: Input): ByteArray {
        TODO("Not yet implemented")
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, output: Output): T {
        TODO("Not yet implemented")
    }

}

/**
 * 标注 JCE 序列化时使用的 ID
 */
@SerialInfo
annotation class JceId(val id: Int)

/**
 * 类中元素的 tag
 *
 * 保留这个结构, 为将来增加功能的兼容性.
 */
internal data class JceTag(
    val id: Int,
    val isNullable: Boolean
)

@OptIn(InternalSerializationApi::class) // 将来 kotlinx 修改后再复制过来 mirai.
private class JceDecoder(
    val jce: JceInput, override val context: SerialModule
) : TaggedDecoder<JceTag>() {
    override val updateMode: UpdateMode
        get() = UpdateMode.BANNED

    override fun SerialDescriptor.getTag(index: Int): JceTag {
        val annotations = this.getElementAnnotations(index)

        val id = (annotations.asSequence().filterIsInstance<JceId>().firstOrNull()?.id
            ?: annotations.asSequence().filterIsInstance<ProtoId>().firstOrNull()?.id) // 旧版本兼容
            ?: error("cannot find @JceId or @ProtoId for ${this.getElementName(index)} in ${this.serialName}")

        return JceTag(id, this.getElementDescriptor(index).isNullable)
    }


    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        TODO("Not yet implemented")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun decodeTaggedInt(tag: JceTag): Int =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceIntValue(it) }

    override fun decodeTaggedByte(tag: JceTag): Byte =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceByteValue(it) }

    override fun decodeTaggedBoolean(tag: JceTag): Boolean =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceBooleanValue(it) }

    override fun decodeTaggedFloat(tag: JceTag): Float =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceFloatValue(it) }

    override fun decodeTaggedDouble(tag: JceTag): Double =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceDoubleValue(it) }

    override fun decodeTaggedShort(tag: JceTag): Short =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceShortValue(it) }

    override fun decodeTaggedLong(tag: JceTag): Long =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceLongValue(it) }

    override fun decodeTaggedString(tag: JceTag): String =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceStringValue(it) }

    override fun decodeTaggedEnum(tag: JceTag, enumDescription: SerialDescriptor): Int {
        return super.decodeTaggedEnum(tag, enumDescription)
    }

    override fun decodeTaggedChar(tag: JceTag): Char {
        return super.decodeTaggedChar(tag)
    }

    override fun decodeTaggedNotNullMark(tag: JceTag): Boolean {
        println("!! decodeTaggedNotNullMark: $tag")
        return super.decodeTaggedNotNullMark(tag)
    }
}

private class JceInput(
    val input: Input, val charset: JceCharset
) {
    private var _head: JceHead? = null

    val currentHead: JceHead get() = _head ?: error("No current JceHead available")
    val currentHeadOrNull: JceHead? get() = _head

    /**
     * 读取下一个 [JceHead] 并保存. 可通过 [currentHead] 获取这个 [JceHead].
     *
     * @return 是否成功读取. 返回 `false` 时代表 [Input.endOfInput]
     */
    fun prepareNextHead(): Boolean {
        return readNextHeadButDoNotAssignTo_Head().also { _head = it; } != null
    }

    fun nextHead(): JceHead {
        check(prepareNextHead()) { "No more JceHead available" }
        return currentHead
    }

    /**
     * 直接读取下一个 [JceHead] 并返回.
     * 返回 `null` 则代表 [Input.endOfInput]
     */
    @Suppress("FunctionName")
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun readNextHeadButDoNotAssignTo_Head(): JceHead? {
        val var2 = input.readUByte()
        val type = var2 and 15u
        var tag = var2.toUInt() shr 4
        if (tag == 15u) {
            if (input.endOfInput) {
                return null
            }
            tag = input.readUByte().toUInt()
        }
        return JceHead(tag = tag.toInt(), type = type.toByte())
    }

    /**
     * 使用这个 [JceHead].
     * [block] 结束后将会 [准备下一个 [JceHead]][prepareNextHead]
     */
    inline fun <R> useHead(crossinline block: (JceHead) -> R): R {
        return currentHead.let(block).also { prepareNextHead() }
    }

    /**
     * 跳过 [JceHead] 和对应的数据值, 直到找到 [tag], 否则返回 `null`
     */
    inline fun <R> skipToTagAndUseIfPossibleOrNull(tag: Int, crossinline block: (JceHead) -> R): R? {
        return skipToHeadOrNull(tag)?.let(block).also { prepareNextHead() }
    }

    /**
     * 跳过 [JceHead] 和对应的数据值, 直到找到 [tag], 否则抛出异常
     */
    inline fun <R> skipToTagAndUseIfPossibleOrFail(
        tag: Int,
        crossinline message: () -> String = { "tag not found: $tag" },
        crossinline block: (JceHead) -> R
    ): R {
        return checkNotNull(skipToTagAndUseIfPossibleOrNull(tag, block), message)
    }

    tailrec fun skipToHeadOrNull(tag: Int): JceHead? {
        val current: JceHead = currentHead // no backing field

        return when {
            current.tag > tag -> null // tag 大了，即找不到
            current.tag == tag -> current // 满足需要.
            else -> { // tag 小了
                skipField(current.type)
                check(prepareNextHead()) { "cannot skip to tag $tag, early EOF" }
                skipToHeadOrNull(tag)
            }
        }
    }

    inline fun skipToHeadOrFail(tag: Int, message: () -> String = { "head not found: $tag" }): JceHead {
        return checkNotNull(skipToHeadOrNull(tag), message)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @PublishedApi
    internal fun skipField(type: Byte): Unit = when (type.toInt()) {
        0 -> this.input.discardExact(1)
        1 -> this.input.discardExact(2)
        2 -> this.input.discardExact(4)
        3 -> this.input.discardExact(8)
        4 -> this.input.discardExact(4)
        5 -> this.input.discardExact(8)
        6 -> this.input.discardExact(this.input.readUByte().toInt())
        7 -> this.input.discardExact(this.input.readInt())
        8 -> { // map
            repeat(skipToTagAndUseIfPossibleOrFail(0) {
                readJceIntValue(it)
            } * 2) {
                useHead { skipField(it.type) }
            }
        }
        9 -> { // list
            repeat(skipToTagAndUseIfPossibleOrFail(0) {
                readJceIntValue(it)
            }) {
                useHead { skipField(it.type) }
            }
        }
        10 -> {
            fun skipToStructEnd() {
                var head: JceHead
                do {
                    head = nextHead()
                    skipField(head.type)
                } while (head.type.toInt() != 11)
            }
            skipToStructEnd()
        }
        11, 12 -> {

        }
        13 -> {
            val head = nextHead()
            check(head.type.toInt() == 0) { "skipField with invalid type, type value: " + type + ", " + head.type }
            this.input.discardExact(
                skipToTagAndUseIfPossibleOrFail(0) {
                    readJceIntValue(it)
                }
            )
        }
        else -> error("invalid type: $type")
    }

    // region readers
    fun readJceIntValue(head: JceHead): Int {
        return when (head.type) {
            ZERO_TYPE -> 0
            BYTE -> input.readByte().toInt()
            SHORT -> input.readShort().toInt()
            INT -> input.readInt()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceShortValue(head: JceHead): Short {
        return when (head.type) {
            ZERO_TYPE -> 0
            BYTE -> input.readByte().toShort()
            SHORT -> input.readShort()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceLongValue(head: JceHead): Long {
        return when (head.type) {
            ZERO_TYPE -> 0
            BYTE -> input.readByte().toLong()
            SHORT -> input.readShort().toLong()
            INT -> input.readInt().toLong()
            LONG -> input.readLong()
            else -> error("type mismatch ${head.type}")
        }
    }

    fun readJceByteValue(head: JceHead): Byte {
        return when (head.type) {
            ZERO_TYPE -> 0
            BYTE -> input.readByte()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceFloatValue(head: JceHead): Float {
        return when (head.type) {
            ZERO_TYPE -> 0f
            FLOAT -> input.readFloat()
            else -> error("type mismatch: ${head.type}")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun readJceStringValue(head: JceHead): String {
        return when (head.type) {
            STRING1 -> input.readString(input.readUByte().toInt(), charset = charset.kotlinCharset)
            STRING4 -> input.readString(
                input.readUInt().toInt().also { require(it in 1 until 104857600) { "bad string length: $it" } },
                charset = charset.kotlinCharset
            )
            else -> error("type mismatch: ${head.type}, expecting 6 or 7 (for string)")
        }
    }

    fun readJceDoubleValue(head: JceHead): Double {
        return when (head.type.toInt()) {
            12 -> 0.0
            4 -> input.readFloat().toDouble()
            5 -> input.readDouble()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceBooleanValue(head: JceHead): Boolean {
        return readJceByteValue(head) == 0.toByte()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
inline class JceHead(private val value: Long) {
    constructor(tag: Int, type: Byte) : this(tag.toLong().shl(32) or type.toLong())

    val tag: Int get() = (value ushr 32).toInt()
    val type: Byte get() = value.toUInt().toByte()

    override fun toString(): String {
        val typeString = when (type) {
            Jce.BYTE -> "Byte"
            Jce.DOUBLE -> "Double"
            Jce.FLOAT -> "Float"
            Jce.INT -> "Int"
            Jce.LIST -> "List"
            Jce.LONG -> "Long"
            Jce.MAP -> "Map"
            Jce.SHORT -> "Short"
            Jce.SIMPLE_LIST -> "SimpleList"
            Jce.STRING1 -> "String1"
            Jce.STRING4 -> "String4"
            Jce.STRUCT_BEGIN -> "StructBegin"
            Jce.STRUCT_END -> "StructEnd"
            Jce.ZERO_TYPE -> "Zero"
            else -> error("illegal jce type: $type")
        }
        return "JceHead(tag=$tag, type=$type($typeString))"
    }
}
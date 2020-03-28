/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization.jce

import kotlinx.io.core.*
import net.mamoe.mirai.qqandroid.io.serialization.JceCharset
import net.mamoe.mirai.qqandroid.utils.io.readString


/**
 * Jce Input. 需要手动管理 head.
 */
internal class JceInput(
    val input: Input, val charset: JceCharset
) {
    private var _head: JceHead? = null

    val currentHead: JceHead get() = _head ?: throw EOFException("No current JceHead available")
    val currentHeadOrNull: JceHead? get() = _head

    init {
        prepareNextHead()
    }

    /**
     * 读取下一个 [JceHead] 并保存. 可通过 [currentHead] 获取这个 [JceHead].
     *
     * @return 是否成功读取. 返回 `false` 时代表 [Input.endOfInput]
     */
    fun prepareNextHead(): Boolean {
        return readNextHeadButDoNotAssignTo_Head().also { _head = it; } != null
    }

    fun nextHead(): JceHead {
        if (!prepareNextHead()) {
            throw EOFException("No more JceHead available")
        }
        return currentHead
    }

    /**
     * 直接读取下一个 [JceHead] 并返回.
     * 返回 `null` 则代表 [Input.endOfInput]
     */
    @Suppress("FunctionName")
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun readNextHeadButDoNotAssignTo_Head(): JceHead? {
        if (input.endOfInput) {
            return null
        }
        val var2 = input.readUByte()
        val type = var2 and 15u
        var tag = var2.toUInt() shr 4
        if (tag == 15u) {
            tag = input.readUByte().toUInt()
        }
        return JceHead(
            tag = tag.toInt(),
            type = type.toByte()
        )
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
    inline fun <R> skipToHeadAndUseIfPossibleOrNull(tag: Int, crossinline block: (JceHead) -> R): R? {
        return skipToHeadOrNull(tag)?.let(block).also { prepareNextHead() }
    }

    /**
     * 跳过 [JceHead] 和对应的数据值, 直到找到 [tag], 否则抛出异常
     */
    inline fun <R : Any> skipToHeadAndUseIfPossibleOrFail(
        tag: Int,
        crossinline message: () -> String = { "tag not found: $tag" },
        crossinline block: (JceHead) -> R
    ): R {
        return checkNotNull<R>(skipToHeadAndUseIfPossibleOrNull(tag, block), message)
    }

    tailrec fun skipToHeadOrNull(tag: Int): JceHead? {
        val current: JceHead = currentHeadOrNull ?: return null // no backing field

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

    inline fun skipToHeadOrFail(
        tag: Int,
        message: () -> String = { "head not found: $tag" }
    ): JceHead {
        return checkNotNull(skipToHeadOrNull(tag), message)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @PublishedApi
    internal fun skipField(type: Byte): Unit {
        JceDecoder.println { "skipping ${JceHead.findJceTypeName(type)}" }
        when (type) {
            Jce.BYTE -> this.input.discardExact(1)
            Jce.SHORT -> this.input.discardExact(2)
            Jce.INT -> println("readInt=" + this.input.readInt())
            Jce.LONG -> this.input.discardExact(8)
            Jce.FLOAT -> this.input.discardExact(4)
            Jce.DOUBLE -> this.input.discardExact(8)
            Jce.STRING1 -> this.input.discardExact(this.input.readUByte().toInt())
            Jce.STRING4 -> this.input.discardExact(this.input.readInt())
            Jce.MAP -> { // map
                JceDecoder.structureHierarchy++
                var count: Int = 0
                nextHead() // avoid shadowing, don't remove
                repeat(skipToHeadAndUseIfPossibleOrFail(0, message = { "tag 0 not found when skipping map" }) {
                    readJceIntValue(it).also { count = it * 2 }
                } * 2) {
                    skipField(currentHead.type)
                    if (it != count - 1) { // don't read last head
                        nextHead()
                    }
                }
                JceDecoder.structureHierarchy--
            }
            Jce.LIST -> { // list
                JceDecoder.structureHierarchy++
                var count: Int = 0
                nextHead() // avoid shadowing, don't remove
                repeat(skipToHeadAndUseIfPossibleOrFail(0, message = { "tag 0 not found when skipping list" }) { head ->
                    readJceIntValue(head).also { count = it }
                }) {
                    skipField(currentHead.type)
                    if (it != count - 1) { // don't read last head
                        nextHead()
                    }
                }
                JceDecoder.structureHierarchy--
            }
            Jce.STRUCT_BEGIN -> {
                JceDecoder.structureHierarchy++
                var head: JceHead
                do {
                    head = nextHead()
                    skipField(head.type)
                } while (head.type != Jce.STRUCT_END)
                JceDecoder.structureHierarchy--
            }
            Jce.STRUCT_END, Jce.ZERO_TYPE -> {
            }
            Jce.SIMPLE_LIST -> {
                JceDecoder.structureHierarchy++
                var head = nextHead()
                check(head.type == Jce.BYTE) { "bad simple list element type: " + head.type }
                check(head.tag == 0) { "simple list element tag must be 0, but was ${head.tag}" }

                head = nextHead()
                check(head.tag == 0) { "tag for size for simple list must be 0, but was ${head.tag}" }
                this.input.discardExact(readJceIntValue(head))
                JceDecoder.structureHierarchy--
            }
            else -> error("invalid type: $type")
        }
    }

    // region readers
    fun readJceIntValue(head: JceHead): Int {
        //println("readJceIntValue: $head")
        return when (head.type) {
            Jce.ZERO_TYPE -> 0
            Jce.BYTE -> input.readByte().toInt()
            Jce.SHORT -> input.readShort().toInt()
            Jce.INT -> input.readInt()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceShortValue(head: JceHead): Short {
        return when (head.type) {
            Jce.ZERO_TYPE -> 0
            Jce.BYTE -> input.readByte().toShort()
            Jce.SHORT -> input.readShort()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceLongValue(head: JceHead): Long {
        return when (head.type) {
            Jce.ZERO_TYPE -> 0
            Jce.BYTE -> input.readByte().toLong()
            Jce.SHORT -> input.readShort().toLong()
            Jce.INT -> input.readInt().toLong()
            Jce.LONG -> input.readLong()
            else -> error("type mismatch ${head.type}")
        }
    }

    fun readJceByteValue(head: JceHead): Byte {
        //println("readJceByteValue: $head")
        return when (head.type) {
            Jce.ZERO_TYPE -> 0
            Jce.BYTE -> input.readByte()
            else -> error("type mismatch: ${head.type}")
        }
    }

    fun readJceFloatValue(head: JceHead): Float {
        return when (head.type) {
            Jce.ZERO_TYPE -> 0f
            Jce.FLOAT -> input.readFloat()
            else -> error("type mismatch: ${head.type}")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun readJceStringValue(head: JceHead): String {
        //println("readJceStringValue: $head")
        return when (head.type) {
            Jce.STRING1 -> input.readString(input.readUByte().toInt(), charset = charset.kotlinCharset)
            Jce.STRING4 -> input.readString(
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
        return readJceByteValue(head) == 1.toByte()
    }
}

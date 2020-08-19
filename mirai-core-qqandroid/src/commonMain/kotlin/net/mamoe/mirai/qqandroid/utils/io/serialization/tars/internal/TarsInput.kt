/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils.io.serialization.tars.internal

import kotlinx.io.charsets.Charset
import kotlinx.io.core.*
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars


/**
 * Tars Input. 需要手动管理 head.
 */
internal class TarsInput(
    val input: Input, private val charset: Charset
) {
    private var _head: TarsHead? = null

    val currentHead: TarsHead get() = _head ?: throw EOFException("No current TarsHead available")
    val currentHeadOrNull: TarsHead? get() = _head

    init {
        prepareNextHead()
    }

    /**
     * 读取下一个 [TarsHead] 并保存. 可通过 [currentHead] 获取这个 [TarsHead].
     *
     * @return 是否成功读取. 返回 `false` 时代表 [Input.endOfInput]
     */
    fun prepareNextHead(): Boolean {
        return readNextHeadButDoNotAssignTo_Head().also { _head = it; } != null
    }

    fun nextHead(): TarsHead {
        if (!prepareNextHead()) {
            throw EOFException("No more TarsHead available")
        }
        return currentHead
    }

    /**
     * 直接读取下一个 [TarsHead] 并返回.
     * 返回 `null` 则代表 [Input.endOfInput]
     */
    @Suppress("FunctionName")
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun readNextHeadButDoNotAssignTo_Head(): TarsHead? {
        if (input.endOfInput) {
            return null
        }
        val var2 = input.readUByte()
        val type = var2 and 15u
        var tag = var2.toUInt() shr 4
        if (tag == 15u) {
            tag = input.readUByte().toUInt()
        }
        return TarsHead(
            tag = tag.toInt(),
            type = type.toByte()
        )
    }

    /**
     * 使用这个 [TarsHead].
     * [block] 结束后将会 [准备下一个 [TarsHead]][prepareNextHead]
     */
    inline fun <R> useHead(crossinline block: (TarsHead) -> R): R {
        return currentHead.let(block).also { prepareNextHead() }
    }

    /**
     * 跳过 [TarsHead] 和对应的数据值, 直到找到 [tag], 否则返回 `null`
     */
    inline fun <R> skipToHeadAndUseIfPossibleOrNull(tag: Int, crossinline block: (TarsHead) -> R): R? {
        return skipToHeadOrNull(tag)?.let(block).also { prepareNextHead() }
    }

    /**
     * 跳过 [TarsHead] 和对应的数据值, 直到找到 [tag], 否则抛出异常
     */
    inline fun <R : Any> skipToHeadAndUseIfPossibleOrFail(
        tag: Int,
        crossinline message: () -> String = { "tag not found: $tag" },
        crossinline block: (TarsHead) -> R
    ): R {
        return checkNotNull<R>(skipToHeadAndUseIfPossibleOrNull(tag, block), message)
    }

    tailrec fun skipToHeadOrNull(tag: Int): TarsHead? {
        val current: TarsHead = currentHeadOrNull ?: return null // no backing field

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

    @OptIn(ExperimentalUnsignedTypes::class)
    @PublishedApi
    internal fun skipField(type: Byte) {
        TarsDecoder.println {
            "skipping ${
                TarsHead.findTarsTypeName(
                    type
                )
            }"
        }
        when (type) {
            Tars.BYTE -> this.input.discardExact(1)
            Tars.SHORT -> this.input.discardExact(2)
            Tars.INT -> this.input.discardExact(4)
            Tars.LONG -> this.input.discardExact(8)
            Tars.FLOAT -> this.input.discardExact(4)
            Tars.DOUBLE -> this.input.discardExact(8)
            Tars.STRING1 -> this.input.discardExact(this.input.readUByte().toInt())
            Tars.STRING4 -> this.input.discardExact(this.input.readInt())
            Tars.MAP -> { // map
                TarsDecoder.structureHierarchy++
                var count = 0
                nextHead() // avoid shadowing, don't remove
                repeat(skipToHeadAndUseIfPossibleOrFail(0, message = { "tag 0 not found when skipping map" }) { head ->
                    readTarsIntValue(head).also { count = it * 2 }
                } * 2) {
                    skipField(currentHead.type)
                    if (it != count - 1) { // don't read last head
                        nextHead()
                    }
                }
                TarsDecoder.structureHierarchy--
            }
            Tars.LIST -> { // list
                TarsDecoder.structureHierarchy++
                var count = 0
                nextHead() // avoid shadowing, don't remove
                repeat(skipToHeadAndUseIfPossibleOrFail(0, message = { "tag 0 not found when skipping list" }) { head ->
                    readTarsIntValue(head).also { count = it }
                }) {
                    skipField(currentHead.type)
                    if (it != count - 1) { // don't read last head
                        nextHead()
                    }
                }
                if (count == 0) {

                }
                TarsDecoder.structureHierarchy--
            }
            Tars.STRUCT_BEGIN -> {
                TarsDecoder.structureHierarchy++
                var head: TarsHead
                do {
                    head = nextHead()
                    skipField(head.type)
                } while (head.type != Tars.STRUCT_END)
                TarsDecoder.structureHierarchy--
            }
            Tars.STRUCT_END, Tars.ZERO_TYPE -> {
            }
            Tars.SIMPLE_LIST -> {
                TarsDecoder.structureHierarchy++
                var head = nextHead()
                check(head.type == Tars.BYTE) { "bad simple list element type: " + head.type }
                check(head.tag == 0) { "simple list element tag must be 0, but was ${head.tag}" }

                head = nextHead()
                check(head.tag == 0) { "tag for size for simple list must be 0, but was ${head.tag}" }
                this.input.discardExact(readTarsIntValue(head))
                TarsDecoder.structureHierarchy--
            }
            else -> error("invalid type: $type")
        }
    }

    // region readers
    fun readTarsIntValue(head: TarsHead): Int {
        //println("readTarsIntValue: $head")
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte().toInt()
            Tars.SHORT -> input.readShort().toInt()
            Tars.INT -> input.readInt()
            else -> error("type mismatch: $head")
        }
    }

    fun readTarsShortValue(head: TarsHead): Short {
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte().toShort()
            Tars.SHORT -> input.readShort()
            else -> error("type mismatch: $head")
        }
    }

    fun readTarsLongValue(head: TarsHead): Long {
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte().toLong()
            Tars.SHORT -> input.readShort().toLong()
            Tars.INT -> input.readInt().toLong()
            Tars.LONG -> input.readLong()
            else -> error("type mismatch ${head.type}")
        }
    }

    fun readTarsByteValue(head: TarsHead): Byte {
        //println("readTarsByteValue: $head")
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte()
            else -> error("type mismatch: $head")
        }
    }

    fun readTarsFloatValue(head: TarsHead): Float {
        return when (head.type) {
            Tars.ZERO_TYPE -> 0f
            Tars.FLOAT -> input.readFloat()
            else -> error("type mismatch: $head")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun readTarsStringValue(head: TarsHead): String {
        //println("readTarsStringValue: $head")
        return when (head.type) {
            Tars.STRING1 -> input.readString(input.readUByte().toInt(), charset = charset)
            Tars.STRING4 -> input.readString(
                input.readUInt().toInt().also { require(it in 1 until 104857600) { "bad string length: $it" } },
                charset = charset
            )
            else -> error("type mismatch: $head, expecting 6 or 7 (for string)")
        }
    }

    fun readTarsDoubleValue(head: TarsHead): Double {
        return when (head.type.toInt()) {
            12 -> 0.0
            4 -> input.readFloat().toDouble()
            5 -> input.readDouble()
            else -> error("type mismatch: $head")
        }
    }

    fun readTarsBooleanValue(head: TarsHead): Boolean {
        return readTarsByteValue(head) == 1.toByte()
    }
}

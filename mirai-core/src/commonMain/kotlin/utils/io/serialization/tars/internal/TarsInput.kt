/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.io.serialization.tars.internal

import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.utils.io.serialization.tars.Tars


/**
 * Tars Input. 需要手动管理 head.
 */
internal class TarsInput(
    val input: Input, private val charset: Charset, private val debugLogger: DebugLogger,
) {
    private var _head: TarsHead? = null
    private var _nextHead: TarsHead? = null

    val currentHead: TarsHead get() = _head ?: throw EOFException("No current TarsHead available")
    val currentHeadOrNull: TarsHead? get() = _head

    init {
        prepareNextHead()
    }

    /**
     * 预读取下个 [TarsHead]
     *
     * 注意: 应该读完 [currentHead] 的值再调用
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun peekNextHead(): TarsHead? {
        _nextHead?.let { return it }
        return readNextHeadButDoNotAssignTo_Head(true).also { _nextHead = it; }.also {
            debugLogger.println("Peek next head: $it")
        }
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
    private fun readNextHeadButDoNotAssignTo_Head(
        ignoreNextHead: Boolean = false,
    ): TarsHead? {
        if (!ignoreNextHead) {
            val n = _nextHead
            if (n != null) {
                _nextHead = null
                return n
            }
        }
        if (input.endOfInput) {
            return null
        }
        val var2 = try {
            input.readUByte()
        } catch (e: EOFException) {
            // somehow `endOfInput` still returns false
            return null
        }
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
        crossinline block: (TarsHead) -> R,
    ): R {
        return checkNotNull(skipToHeadAndUseIfPossibleOrNull(tag, block), message)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun skipToTag(tag: Int): Boolean {
        while (true) {
            val hd = peekNextHead() ?: return false
            if (tag <= hd.tag || hd.type == 11.toByte()) {
                return tag == hd.tag
            }
            debugLogger.println("Discard $tag, $hd, ${hd.size}")
            input.discardExact(hd.size)
            skipField(hd.type)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun readInt32(tag: Int): Int {
        if (!skipToTag(tag)) return 0
        return readTarsIntValue(nextHead())
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
        debugLogger.println {
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
                debugLogger.structureHierarchy++
                repeat(readInt32(0).also {
                    debugLogger.println("SIZE = $it")
                } * 2) {
                    skipField(nextHead().type)
                }
                debugLogger.structureHierarchy--
            }
            Tars.LIST -> { // list
                debugLogger.structureHierarchy++
                repeat(readInt32(0).also {
                    debugLogger.println("SIZE = $it")
                }) {
                    skipField(nextHead().type)
                }
                debugLogger.structureHierarchy--
            }
            Tars.STRUCT_BEGIN -> {
                debugLogger.structureHierarchy++
                var head: TarsHead
                do {
                    head = nextHead()
                    if (head.type == Tars.STRUCT_END) {
                        debugLogger.structureHierarchy--
                        skipField(head.type)
                        break
                    }
                    skipField(head.type)
                } while (head.type != Tars.STRUCT_END)
            }
            Tars.STRUCT_END, Tars.ZERO_TYPE -> {
            }
            Tars.SIMPLE_LIST -> {
                debugLogger.structureHierarchy++
                var head = nextHead()
                check(head.type == Tars.BYTE) { "bad simple list element type: " + head.type + ", $head" }
                check(head.tag == 0) { "simple list element tag must be 0, but was ${head.tag}" }

                head = nextHead()
                check(head.tag == 0) { "tag for size for simple list must be 0, but was ${head.tag}" }
                this.input.discardExact(readTarsIntValue(head))
                debugLogger.structureHierarchy--
            }
            else -> error("invalid type: $type")
        }
    }

    // region readers
    fun readTarsIntValue(head: TarsHead): Int {
        //debugLogger.println("readTarsIntValue: $head")
        return readTarsIntValue(head.type, head)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun readTarsIntValue(type: Byte, head: Any = type): Int {
        return when (type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte().toInt()
            Tars.SHORT -> input.readShort().toInt()
            Tars.INT -> input.readInt()
            else -> error("type mismatch: $head, expecting int.")
        }
    }

    fun readTarsShortValue(head: TarsHead): Short {
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte().toShort()
            Tars.SHORT -> input.readShort()
            else -> error("type mismatch: $head, expecting short.")
        }
    }

    fun readTarsLongValue(head: TarsHead): Long {
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte().toLong()
            Tars.SHORT -> input.readShort().toLong()
            Tars.INT -> input.readInt().toLong()
            Tars.LONG -> input.readLong()
            else -> error("type mismatch ${head}, expecting long.")
        }
    }

    fun readTarsByteValue(head: TarsHead): Byte {
        //debugLogger.println("readTarsByteValue: $head")
        return when (head.type) {
            Tars.ZERO_TYPE -> 0
            Tars.BYTE -> input.readByte()
            else -> error("type mismatch: $head, expecting byte.")
        }
    }

    fun readTarsFloatValue(head: TarsHead): Float {
        return when (head.type) {
            Tars.ZERO_TYPE -> 0f
            Tars.FLOAT -> input.readFloat()
            else -> error("type mismatch: $head, expecting float.")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun readTarsStringValue(head: TarsHead): String {
        //debugLogger.println("readTarsStringValue: $head")
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

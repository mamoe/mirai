/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlin.jvm.JvmField

public typealias TlvMap = MutableMap<Int, ByteArray>

public fun TlvMap(): TlvMap = linkedMapOf()

@Suppress("FunctionName")
public fun Output._writeTlvMap(
    tagSize: Int,
    includeCount: Boolean = true,
    map: TlvMap,
) {
    if (includeCount) {
        when (tagSize) {
            1 -> writeByte(map.size.toByte())
            2 -> writeShort(map.size.toShort())
            4 -> writeInt(map.size)
            else -> error("Unsupported tag size: $tagSize")
        }
    }

    map.forEach { (key, value) ->
        when (tagSize) {
            1 -> writeByte(key.toByte())
            2 -> writeShort(key.toShort())
            4 -> writeInt(key)
            else -> error("Unsupported tag size: $tagSize")
        }

        writeShort(value.size.toShort())
        writeFully(value)
    }
}

@Suppress("MemberVisibilityCanBePrivate")
public class TlvMapWriter
internal constructor(
    private val tagSize: Int,
) {
    @JvmField
    internal val buffer = BytePacketBuilder()

    @JvmField
    internal var counter: Int = 0

    @PublishedApi
    @JvmField
    internal var isWriting: Boolean = false

    private fun writeKey(key: Int) {
        when (tagSize) {
            1 -> buffer.writeByte(key.toByte())
            2 -> buffer.writeShort(key.toShort())
            4 -> buffer.writeInt(key)
            else -> error("Unsupported tag size: $tagSize")
        }
        counter++
    }

    @PublishedApi
    internal fun ensureNotWriting() {
        if (isWriting) error("Cannot write a new Tlv when writing Tlv")
    }

    public fun tlv(key: Int, data: ByteArray) {
        ensureNotWriting()
        tlv0(key, data)
    }


    private fun tlv0(key: Int, data: ByteArray) {
        writeKey(key)
        buffer.writeShort(data.size.toShort())
        buffer.writeFully(data)
//        println("Writing [${key.toUHexString()}](${data.size}) => " + data.toUHexString())
    }

    public fun tlv(key: Int, data: ByteReadPacket) {
        ensureNotWriting()
        tlv0(key, data)
    }


    @PublishedApi
    internal fun tlv0(key: Int, data: ByteReadPacket) {
        writeKey(key)
        buffer.writeShort(data.remaining.toShort())

//        println("Writing [${key.toUHexString()}](${data.remaining}) => " + data.copy()
//            .use { d1 -> d1.readBytes().toUHexString() })

        buffer.writePacket(data)
    }


    public inline fun tlv(
        key: Int,
        crossinline builder: BytePacketBuilder.() -> Unit,
    ) {
        ensureNotWriting()
        try {
            isWriting = true
            buildPacket(builder).use { tlv0(key, it) }
        } finally {
            isWriting = false
        }
    }

}

public fun Output._writeTlvMap(
    tagSize: Int = 2,
    includeCount: Boolean = true,
    block: TlvMapWriter.() -> Unit
) {
    val writer = TlvMapWriter(tagSize)
    try {
        block(writer)
        if (includeCount) {
            when (tagSize) {
                1 -> writeByte(writer.counter.toByte())
                2 -> writeShort(writer.counter.toShort())
                4 -> writeInt(writer.counter)
                else -> error("Unsupported tag size: $tagSize")
            }
        }
        writer.buffer.build().use {
//            println(it.copy().use { it.readBytes().toUHexString() })

            writePacket(it)
        }
    } finally {
        writer.buffer.release()
    }
}

public fun TlvMap.getOrFail(tag: Int): ByteArray {
    return this[tag] ?: error("cannot find tlv 0x${tag.toUHexString("")}($tag)")
}

public fun TlvMap.getOrFail(tag: Int, lazyMessage: (tag: Int) -> String): ByteArray {
    return this[tag] ?: error(lazyMessage(tag))
}

@Suppress("FunctionName")
public fun Input._readTLVMap(tagSize: Int = 2, suppressDuplication: Boolean = true): TlvMap =
    _readTLVMap(true, tagSize, suppressDuplication)

@Suppress("DuplicatedCode", "FunctionName")
public fun Input._readTLVMap(
    expectingEOF: Boolean = true,
    tagSize: Int,
    suppressDuplication: Boolean = true
): TlvMap {
    val map = linkedMapOf<Int, ByteArray>()
    var key = 0

    while (kotlin.run {
            try {
                key = when (tagSize) {
                    1 -> readByte().toUByte().toInt()
                    2 -> readShort().toUShort().toInt()
                    4 -> readInt()
                    else -> error("Unsupported tag size: $tagSize")
                }
            } catch (e: Exception) { // java.nio.BufferUnderflowException is not a EOFException...
                if (expectingEOF) {
                    return map
                }
                throw e
            }
            key
        }.toUByte() != UByte.MAX_VALUE) {

        if (map.containsKey(key)) {
//            println("reading ${key.toUHexString()}")

            if (!suppressDuplication) {
                /*
                @Suppress("DEPRECATION")
                MiraiLogger.error(
                    @Suppress("IMPLICIT_CAST_TO_ANY")
                    """
                Error readTLVMap:
                duplicated key ${when (tagSize) {
                        1 -> key.toByte()
                        2 -> key.toShort()
                        4 -> key
                        else -> error("unreachable")
                    }.contentToString()}
                map=${map.contentToString()}
                duplicating value=${this.readUShortLVByteArray().toUHexString()}
                """.trimIndent()
                )*/
            } else {
                this.discardExact(this.readShort().toInt() and 0xffff)
            }
        } else {
            try {
                val len = readShort().toUShort().toInt()
                val data = this.readBytes(len)
//                println("Writing [${key.toUHexString()}]($len) => ${data.toUHexString()}")
                map[key] = data
            } catch (e: Exception) { // BufferUnderflowException, java.io.EOFException
                // if (expectingEOF) {
                //     return map
                // }
                throw e
            }
        }
    }
    return map
}

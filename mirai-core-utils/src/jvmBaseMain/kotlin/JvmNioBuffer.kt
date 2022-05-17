/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

@file:Suppress("NOTHING_TO_INLINE", "UsePropertyAccessSyntax")

package net.mamoe.mirai.utils

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

public inline var Buffer.pos: Int
    get() = position()
    set(value) {
        position(value)
    }
public inline val Buffer.remaining: Int
    get() = remaining()
public inline var Buffer.limit: Int
    get() = limit()
    set(value) {
        limit(value)
    }

public inline fun Buffer.hasRemaining(size: Int): Boolean = remaining >= size

public inline fun ByteBuffer.read(): Byte = get()
public inline fun ByteBuffer.readInt(): Int = getInt()
public inline fun ByteBuffer.readDouble(): Double = getDouble()
public inline fun ByteBuffer.readShort(): Short = getShort()
public inline fun ByteBuffer.readLong(): Long = getLong()
public inline fun ByteBuffer.readChar(): Char = getChar()
public inline fun ByteBuffer.readFloat(): Float = getFloat()

public inline fun ByteBuffer.readBytes(dst: ByteArray) {
    get(dst)
}

public fun ByteBuffer.readBytes(): ByteArray {
    val rsp = ByteArray(remaining)
    readBytes(rsp)
    return rsp
}

public fun ByteBuffer.readToChars(charset: Charset = Charsets.UTF_8): CharBuffer {
    return charset.decode(this)
}

public fun ByteBuffer.readString(charset: Charset = Charsets.UTF_8): String {
    return readToChars(charset).toString()
}

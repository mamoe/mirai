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

package net.mamoe.mirai.utils

import kotlinx.io.core.Closeable
import kotlinx.io.core.toByteArray
import kotlinx.io.core.use
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public expect val DEFAULT_BUFFER_SIZE: Int

public expect fun ByteArray.unzip(offset: Int = 0, length: Int = size - offset): ByteArray


/**
 * Localhost 解析
 */
public expect fun localIpAddress(): String

public fun String.md5(): ByteArray = toByteArray().md5()

public expect fun ByteArray.md5(offset: Int = 0, length: Int = size - offset): ByteArray

public fun String.sha1(): ByteArray = toByteArray().sha1()

public expect fun ByteArray.sha1(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun ByteArray.ungzip(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun ByteArray.gzip(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun ByteArray.zip(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun availableProcessors(): Int

public inline fun <C : Closeable, R> C.withUse(block: C.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use(block)
}

public inline fun <I : Closeable, O : Closeable, R> I.withOut(output: O, block: I.(output: O) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use { output.use { block(this, output) } }
}

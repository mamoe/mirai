/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("ByteArrayOpKt_common")

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlin.jvm.JvmName

public expect val DEFAULT_BUFFER_SIZE: Int

public fun String.md5(): ByteArray = toByteArray().md5()

public expect fun ByteArray.md5(offset: Int = 0, length: Int = size - offset): ByteArray

public fun String.sha1(): ByteArray = toByteArray().sha1()

public expect fun ByteArray.sha1(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun ByteArray.gzip(offset: Int = 0, length: Int = size - offset): ByteArray
public expect fun ByteArray.ungzip(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun ByteArray.inflate(offset: Int = 0, length: Int = size - offset): ByteArray
public expect fun ByteArray.deflate(offset: Int = 0, length: Int = size - offset): ByteArray

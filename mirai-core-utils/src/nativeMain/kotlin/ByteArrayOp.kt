/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import interop.*
import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.uint8_tVar


public actual val DEFAULT_BUFFER_SIZE: Int get() = 8192

public actual fun ByteArray.md5(offset: Int, length: Int): ByteArray = callImpl(::mirai_crypto_md5, offset, length)
public actual fun ByteArray.sha1(offset: Int, length: Int): ByteArray = callImpl(::mirai_crypto_sha1, offset, length)

public actual fun ByteArray.gzip(offset: Int, length: Int): ByteArray = callImpl(::mirai_crypto_gzip, offset, length)
public actual fun ByteArray.ungzip(offset: Int, length: Int): ByteArray =
    callImpl(::mirai_crypto_ungzip, offset, length)

public actual fun ByteArray.deflate(offset: Int, length: Int): ByteArray =
    callImpl(::mirai_crypto_deflate, offset, length)

public actual fun ByteArray.inflate(offset: Int, length: Int): ByteArray =
    callImpl(::mirai_crypto_inflate, offset, length)


private fun ByteArray.callImpl(
    fn: (CValuesRef<uint8_tVar>, UInt, CValuesRef<SizedByteArray>) -> Boolean,
    offset: Int,
    length: Int
): ByteArray {
    checkOffsetAndLength(offset, length)

    memScoped {
        val r = alloc<SizedByteArray>()
        if (!fn(toCValues().ptr.reinterpret<uint8_tVar>().plus(offset)!!, length.toUInt(), r.ptr)) {
            throw IllegalStateException("Failed platform implementation call")
        }
        try {
            return r.arr?.readBytes(r.size.toInt())!!
        } finally {
            free(r.arr)
        }
    }
}

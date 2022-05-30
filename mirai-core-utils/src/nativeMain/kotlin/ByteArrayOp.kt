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

import kotlinx.cinterop.*
import platform.zlib.*


public actual val DEFAULT_BUFFER_SIZE: Int get() = 8192

public actual fun ByteArray.md5(offset: Int, length: Int): ByteArray {
    MD5.create().run {
        update(this@md5, offset, length)
        return digest().bytes
    }
}

public actual fun ByteArray.sha1(offset: Int, length: Int): ByteArray = SHA1.create().run {
    update(this@sha1, offset, length)
    return digest().bytes
}

public actual fun ByteArray.gzip(offset: Int, length: Int): ByteArray {
    val output = ByteArray(length * 5)
    output.usePinned { out ->
        usePinned { pin ->
            memScoped {
                val z = alloc<z_stream>()
                z.avail_in = size.toUInt()
                z.next_in = pin.addressOf(0).reinterpret()
                z.avail_out = output.size.toUInt()
                val initialOutAddress = out.addressOf(0)
                z.next_out = initialOutAddress.reinterpret()
                deflateInit2(z.ptr, Z_DEFAULT_COMPRESSION, Z_DEFLATED, 15 or 16, 8, Z_DEFAULT_STRATEGY)
                deflate(z.ptr, Z_FINISH) // TODO: 2022/5/28 buf
                deflateEnd(z.ptr)

                val resultSize = z.next_out.toLong() - initialOutAddress.toLong()
                return output.copyOf(resultSize.toInt())
            }
        }
    }
}

// TODO: 2022/5/28 optimize length

public actual fun ByteArray.ungzip(offset: Int, length: Int): ByteArray {
    val output = ByteArray(length)
    output.usePinned { out ->
        usePinned { pin ->
            memScoped {
                val z = alloc<z_stream>()
                z.avail_in = size.toUInt()
                z.next_in = pin.addressOf(0).reinterpret()
                z.avail_out = output.size.toUInt()
                val initialOutAddress = out.addressOf(0)
                z.next_out = initialOutAddress.reinterpret()
                inflateInit2(z.ptr, 15 or 16)
                inflate(z.ptr, Z_FINISH)
                inflateEnd(z.ptr)

                val resultSize = z.next_out.toLong() - initialOutAddress.toLong()
                return output.copyOf(resultSize.toInt())
            }
        }
    }
}

public actual fun ByteArray.deflate(offset: Int, length: Int): ByteArray {
    val output = ByteArray(length * 2)
    output.usePinned { out ->
        usePinned { pin ->
            memScoped {
                val z = alloc<z_stream>()
                z.avail_in = size.toUInt()
                z.next_in = pin.addressOf(0).reinterpret()
                z.avail_out = output.size.toUInt()
                val initialOutAddress = out.addressOf(0)
                z.next_out = initialOutAddress.reinterpret()
                deflateInit(z.ptr, Z_DEFAULT_COMPRESSION)
                deflate(z.ptr, Z_FINISH)
                deflateEnd(z.ptr)

                val resultSize = z.next_out.toLong() - initialOutAddress.toLong()
                return output.copyOf(resultSize.toInt())
            }
        }
    }
}

public actual fun ByteArray.inflate(offset: Int, length: Int): ByteArray {
    val output = ByteArray(length)
    output.usePinned { out ->
        usePinned { pin ->
            memScoped {
                val z = alloc<z_stream>()
                z.avail_in = size.toUInt()
                z.next_in = pin.addressOf(0).reinterpret()
                z.avail_out = output.size.toUInt()
                val initialOutAddress = out.addressOf(0)
                z.next_out = initialOutAddress.reinterpret()
                inflateInit(z.ptr)
                inflate(z.ptr, Z_FINISH)
                inflateEnd(z.ptr)

                val resultSize = z.next_out.toLong() - initialOutAddress.toLong()
                return output.copyOf(resultSize.toInt())
            }
        }
    }
}


//private fun ByteArray.callImpl(
//    fn: (CValuesRef<uint8_tVar>, UInt, CValuesRef<SizedByteArray>) -> Boolean,
//    offset: Int,
//    length: Int
//): ByteArray {
//    checkOffsetAndLength(offset, length)
//
//    memScoped {
//        val r = alloc<SizedByteArray>()
//        if (!fn(toCValues().ptr.reinterpret<uint8_tVar>().plus(offset)!!, length.toUInt(), r.ptr)) {
//            throw IllegalStateException("Failed platform implementation call")
//        }
//        try {
//            return r.arr?.readBytes(r.size.toInt())!!
//        } finally {
//            free(r.arr)
//        }
//    }
//}
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*

public actual fun String.decodeBase64(): ByteArray {
    return Base64Impl.decode(this)
}

public actual fun ByteArray.encodeBase64(): String {
    return Base64Impl.encode(this)
}

/**
 * From <https://gist.github.com/EmilHernvall/953733>.
 * @author EmilHernvall
 */
private object Base64Impl {
    fun encode(data: ByteArray): String {
        val tbl = charArrayOf(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        )
        val buffer = StringBuilder()
        var pad = 0
        var i = 0
        while (i < data.size) {
            var b = data[i].toInt() and 0xFF shl 16 and 0xFFFFFF
            if (i + 1 < data.size) {
                b = b or (data[i + 1].toInt() and 0xFF shl 8)
            } else {
                pad++
            }
            if (i + 2 < data.size) {
                b = b or (data[i + 2].toInt() and 0xFF)
            } else {
                pad++
            }
            for (j in 0 until 4 - pad) {
                val c = b and 0xFC0000 shr 18
                buffer.append(tbl[c])
                b = b shl 6
            }
            i += 3
        }
        for (j in 0 until pad) {
            buffer.append("=")
        }
        return buffer.toString()
    }

    fun decode(data: String): ByteArray {
        val tbl = intArrayOf(
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
            55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2,
            3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
            20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
        )
        val bytes: ByteArray = data.toByteArray()
        return buildPacket {
            var i = 0
            while (i < bytes.size) {
                var b: Int
                b = if (tbl[bytes[i].toInt()] != -1) {
                    tbl[bytes[i].toInt()] and 0xFF shl 18
                } else {
                    i++
                    continue
                }
                var num = 0
                if (i + 1 < bytes.size && tbl[bytes[i + 1].toInt()] != -1) {
                    b = b or (tbl[bytes[i + 1].toInt()] and 0xFF shl 12)
                    num++
                }
                if (i + 2 < bytes.size && tbl[bytes[i + 2].toInt()] != -1) {
                    b = b or (tbl[bytes[i + 2].toInt()] and 0xFF shl 6)
                    num++
                }
                if (i + 3 < bytes.size && tbl[bytes[i + 3].toInt()] != -1) {
                    b = b or (tbl[bytes[i + 3].toInt()] and 0xFF)
                    num++
                }
                while (num > 0) {
                    val c = b and 0xFF0000 shr 16
                    writeByte(c.toByte())
                    b = b shl 8
                    num--
                }
                i += 4
            }
        }.readBytes()
    }
}
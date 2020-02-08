/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.InflaterInputStream

object QLogReader {
    @JvmStatic
    fun main(args: Array<String>) {

        println(readQLog(File("/Users/jiahua.liu/Downloads/wtlogin_20200129.log")))
    }

    fun readQLog(file: File): String {
        return (decompress(file.readBytes()))
    }

    fun readQLog(file: ByteArray): String {
        return (decompress(file))
    }


    fun decompress(array: ByteArray): String {
        return buildString {
            if (array.isNotEmpty()) {
                var i = 0
                var n = 0
                while (array.size > n + 3) {
                    val buf_to_int32: Int = buf_to_int32(array, n)
                    if (array.size <= n + buf_to_int32 + 3) {
                        break
                    }
                    val buf = ByteArray(buf_to_int32)
                    System.arraycopy(array, n + 4, buf, 0, buf_to_int32)
                    n += 4 + buf_to_int32
                    ++i
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val `in` = ByteArrayInputStream(buf)
                    val inflaterInputStream = InflaterInputStream(`in`)
                    val array2 = ByteArray(1024)
                    while (true) {
                        val read = inflaterInputStream.read(array2)
                        if (read == -1) {
                            break
                        }
                        byteArrayOutputStream.write(array2, 0, read)
                    }
                    append(byteArrayOutputStream.toString())
                }
            }
        }

    }

    private fun buf_to_int32(array: ByteArray, n: Int): Int {
        return (array[n].toInt() shl 24 and -0x1000000) + (array[n + 1].toInt() shl 16 and 0xFF0000) + (array[n + 2].toInt() shl 8 and 0xFF00) + (array[n + 3].toInt() shl 0 and 0xFF)
    }
}
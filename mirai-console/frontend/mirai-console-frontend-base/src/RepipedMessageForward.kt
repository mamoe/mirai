/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.frontendbase

import java.io.ByteArrayOutputStream
import java.io.OutputStream

internal class RepipedMessageForward(
    private val output: (String) -> Unit,
) : ByteArrayOutputStream(1024 * 1024) {
    internal val pipedOutputStream: OutputStream get() = this


    private var lastCheckIndex = 0

    @Synchronized
    override fun write(b: ByteArray?, off: Int, len: Int) {
        super.write(b, off, len)
        flush()

    }

    @Synchronized
    override fun write(b: Int) {
        super.write(b)
        flush()
    }

    @Synchronized
    override fun write(b: ByteArray?) {
        super.write(b)
        flush()
    }

    @Synchronized
    override fun flush() {
        topLoop@
        while (true) {

            var index = lastCheckIndex
            val end = this.count
            var lastIsLr = false // last char is '\r'
            while (index < end) {
                val c = buf[index].toInt() and 0xFF
                when (c shr 4) {
                    in 0..7 -> {
                        /* 0xxxxxxx*/

                        if (c == '\r'.code) {
                            lastIsLr = true
                        } else if (c == 10) {
                            // NEW LINE: \n
                            val strend = if (lastIsLr) {
                                index - 1
                            } else {
                                index
                            }
                            val strx = String(buf, 0, strend, Charsets.UTF_8)


                            index++
                            System.arraycopy(
                                buf, index, buf, 0, end - index
                            )

                            // A \n

                            // index = 1
                            // string with ln = 2

                            count -= index
                            lastCheckIndex = 0
                            output(strx)

                            continue@topLoop // same as return flush()

                        } else {
                            lastIsLr = false
                        }

                        index++
                    }
                    12, 13 -> {
                        /* 110x xxxx   10xx xxxx*/
                        index += 2
                        lastIsLr = false
                    }
                    14 -> {
                        /* 1110 xxxx  10xx xxxx  10xx xxxx */
                        index += 3
                        lastIsLr = false
                    }
                    else -> {
                        /* 10xx xxxx,  1111 xxxx */
                        index++// Ignored
                        lastIsLr = false
                    }
                }

            }
            lastCheckIndex = index

            break
        }
    }

}


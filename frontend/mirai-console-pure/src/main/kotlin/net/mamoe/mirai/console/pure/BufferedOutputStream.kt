/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.pure

import java.io.ByteArrayOutputStream
import java.io.OutputStream

private const val LN = 10.toByte()

internal class BufferedOutputStream @JvmOverloads constructor(
    private val size: Int = 1024 * 1024,
    private val logger: (String?) -> Unit
) : ByteArrayOutputStream(size + 1) {
    override fun write(b: Int) {
        if (this.count >= size) {
            flush()
        }
        if (b == 10) {
            flush()
        } else {
            super.write(b)
        }
    }

    override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    private fun ByteArray.findSplitter(off: Int, end: Int): Int {
        var o = off
        while (o < end) {
            if (get(o) == LN) {
                return o
            }
            o++
        }
        return -1
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val ed = off + len
        if (ed > b.size || ed < 0) {
            throw ArrayIndexOutOfBoundsException()
        }
        write0(b, off, ed)
    }

    private fun write0(b: ByteArray, off: Int, end: Int) {
        val size = end - off
        if (size < 1) return
        val spl = b.findSplitter(off, end)
        if (spl == -1) {
            val over = this.size - (size + count)
            if (over < 0) {
                // cutting
                write0(b, off, end + over)
                flush()
                write0(b, off - over, end)
            } else {
                super.write(b, off, size)
            }
        } else {
            write0(b, off, spl)
            flush()
            write0(b, spl + 1, end)
        }
    }

    override fun writeTo(out: OutputStream?) {
        throw UnsupportedOperationException()
    }

    override fun flush() {
        logger(String(buf, 0, count, Charsets.UTF_8))
        count = 0
    }
}

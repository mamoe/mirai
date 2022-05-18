/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

/*
 * Note: All the declarations in this file are copied from 'com.soywiz.korlibs.krypto'. <https://github.com/korlibs/krypto>
 *
 * The license is attached:

MIT License

Copyright (c) 2017 Carlos Ballesteros Velasco

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

----------------------------------

/**
 * Based on CryptoJS v3.1.2
 * code.google.com/p/crypto-js
 * (c) 2009-2013 by Jeff Mott. All rights reserved.
 * https://github.com/brix/crypto-js/blob/develop/LICENSE
 */
 */

internal inline fun Int.ext8(offset: Int) = (this ushr offset) and 0xFF

internal fun Int.rotateRight(amount: Int): Int = (this ushr amount) or (this shl (32 - amount))
internal fun Int.rotateLeft(bits: Int): Int = ((this shl bits) or (this ushr (32 - bits)))

internal fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, count: Int) =
    src.copyInto(dst, dstPos, srcPos, srcPos + count)

internal fun arraycopy(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, count: Int) =
    src.copyInto(dst, dstPos, srcPos, srcPos + count)

internal fun ByteArray.readU8(o: Int): Int = this[o].toInt() and 0xFF
internal fun ByteArray.readS32_be(o: Int): Int =
    (readU8(o + 3) shl 0) or (readU8(o + 2) shl 8) or (readU8(o + 1) shl 16) or (readU8(o + 0) shl 24)

internal abstract class Hasher(val chunkSize: Int, val digestSize: Int) {
    private val chunk = ByteArray(chunkSize)
    private var writtenInChunk = 0
    private var totalWritten = 0L

    fun reset(): Hasher {
        coreReset()
        writtenInChunk = 0
        totalWritten = 0L
        return this
    }

    fun update(data: ByteArray, offset: Int, count: Int): Hasher {
        var curr = offset
        var left = count
        while (left > 0) {
            val remainingInChunk = chunkSize - writtenInChunk
            val toRead = min(remainingInChunk, left)
            arraycopy(data, curr, chunk, writtenInChunk, toRead)
            left -= toRead
            curr += toRead
            writtenInChunk += toRead
            if (writtenInChunk >= chunkSize) {
                writtenInChunk -= chunkSize
                coreUpdate(chunk)
            }
        }
        totalWritten += count
        return this
    }

    fun digestOut(out: ByteArray) {
        val pad = corePadding(totalWritten)
        var padPos = 0
        while (padPos < pad.size) {
            val padSize = chunkSize - writtenInChunk
            arraycopy(pad, padPos, chunk, writtenInChunk, padSize)
            coreUpdate(chunk)
            writtenInChunk = 0
            padPos += padSize
        }

        coreDigest(out)
        coreReset()
    }

    protected abstract fun coreReset()
    protected abstract fun corePadding(totalWritten: Long): ByteArray
    protected abstract fun coreUpdate(chunk: ByteArray)
    protected abstract fun coreDigest(out: ByteArray)

    fun update(data: ByteArray) = update(data, 0, data.size)
    fun digest(): Hash = Hash(ByteArray(digestSize).also { digestOut(it) })
}

internal value class Hash(val bytes: ByteArray)

internal open class HasherFactory(val create: () -> Hasher) {
    fun digest(data: ByteArray) = create().also { it.update(data, 0, data.size) }.digest()

    inline fun digest(temp: ByteArray = ByteArray(0x1000), readBytes: (data: ByteArray) -> Int): Hash =
        this.create().also {
            while (true) {
                val count = readBytes(temp)
                if (count <= 0) break
                it.update(temp, 0, count)
            }
        }.digest()
}

internal class MD5 : Hasher(chunkSize = 64, digestSize = 16) {
    companion object : HasherFactory({ MD5() }) {
        private val S = intArrayOf(7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21)
        private val T = IntArray(64) { ((1L shl 32) * abs(sin(1.0 + it))).toLong().toInt() }
    }

    private val r = IntArray(4)
    private val o = IntArray(4)
    private val b = IntArray(16)

    init {
        coreReset()
    }

    override fun coreReset() {
        r[0] = 0x67452301
        r[1] = 0xEFCDAB89.toInt()
        r[2] = 0x98BADCFE.toInt()
        r[3] = 0x10325476
    }

    override fun coreUpdate(chunk: ByteArray) {
        for (j in 0 until 64) b[j ushr 2] = (chunk[j].toInt() shl 24) or (b[j ushr 2] ushr 8)
        for (j in 0 until 4) o[j] = r[j]
        for (j in 0 until 64) {
            val d16 = j / 16
            val f = when (d16) {
                0 -> (r[1] and r[2]) or (r[1].inv() and r[3])
                1 -> (r[1] and r[3]) or (r[2] and r[3].inv())
                2 -> r[1] xor r[2] xor r[3]
                3 -> r[2] xor (r[1] or r[3].inv())
                else -> 0
            }
            val bi = when (d16) {
                0 -> j
                1 -> (j * 5 + 1) and 0x0F
                2 -> (j * 3 + 5) and 0x0F
                3 -> (j * 7) and 0x0F
                else -> 0
            }
            val temp = r[1] + (r[0] + f + b[bi] + T[j]).rotateLeft(S[(d16 shl 2) or (j and 3)])
            r[0] = r[3]
            r[3] = r[2]
            r[2] = r[1]
            r[1] = temp
        }
        for (j in 0 until 4) r[j] += o[j]
    }

    override fun corePadding(totalWritten: Long): ByteArray {
        val numberOfBlocks = ((totalWritten + 8) / chunkSize) + 1
        val totalWrittenBits = totalWritten * 8
        return ByteArray(((numberOfBlocks * chunkSize) - totalWritten).toInt()).apply {
            this[0] = 0x80.toByte()
            for (i in 0 until 8) this[this.size - 8 + i] = (totalWrittenBits ushr (8 * i)).toByte()
        }
    }

    override fun coreDigest(out: ByteArray) {
        for (it in 0 until 16) out[it] = (r[it / 4] ushr ((it % 4) * 8)).toByte()
    }
}

internal abstract class SHA(chunkSize: Int, digestSize: Int) : Hasher(chunkSize, digestSize) {
    override fun corePadding(totalWritten: Long): ByteArray {
        val tail = totalWritten % 64
        val padding = (if (64 - tail >= 9) 64 - tail else 128 - tail)
        val pad = ByteArray(padding.toInt()).apply { this[0] = 0x80.toByte() }
        val bits = (totalWritten * 8)
        for (i in 0 until 8) pad[pad.size - 1 - i] = ((bits ushr (8 * i)) and 0xFF).toByte()
        return pad
    }
}

internal class SHA1 : SHA(chunkSize = 64, digestSize = 20) {
    companion object : HasherFactory({ SHA1() }) {
        private val H = intArrayOf(
            0x67452301L.toInt(),
            0xEFCDAB89L.toInt(),
            0x98BADCFEL.toInt(),
            0x10325476L.toInt(),
            0xC3D2E1F0L.toInt()
        )

        private const val K0020: Int = 0x5A827999L.toInt()
        private const val K2040: Int = 0x6ED9EBA1L.toInt()
        private const val K4060: Int = 0x8F1BBCDCL.toInt()
        private const val K6080: Int = 0xCA62C1D6L.toInt()
    }

    private val w = IntArray(80)
    private val h = IntArray(5)

    override fun coreReset(): Unit {
        arraycopy(H, 0, h, 0, 5)
    }

    init {
        coreReset()
    }

    override fun coreUpdate(chunk: ByteArray) {
        for (j in 0 until 16) w[j] = chunk.readS32_be(j * 4)
        for (j in 16 until 80) w[j] = (w[j - 3] xor w[j - 8] xor w[j - 14] xor w[j - 16]).rotateLeft(1)

        var a = h[0]
        var b = h[1]
        var c = h[2]
        var d = h[3]
        var e = h[4]

        for (j in 0 until 80) {
            val temp = a.rotateLeft(5) + e + w[j] + when (j / 20) {
                0 -> ((b and c) or ((b.inv()) and d)) + K0020
                1 -> (b xor c xor d) + K2040
                2 -> ((b and c) xor (b and d) xor (c and d)) + K4060
                else -> (b xor c xor d) + K6080
            }

            e = d
            d = c
            c = b.rotateLeft(30)
            b = a
            a = temp
        }

        h[0] += a
        h[1] += b
        h[2] += c
        h[3] += d
        h[4] += e
    }

    override fun coreDigest(out: ByteArray) {
        for (n in out.indices) out[n] = (h[n / 4] ushr (24 - 8 * (n % 4))).toByte()
    }
}

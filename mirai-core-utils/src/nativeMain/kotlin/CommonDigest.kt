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

internal class SHA256 : SHA(chunkSize = 64, digestSize = 32) {
    companion object : HasherFactory({ SHA256() }) {
        private val H = intArrayOf(
            0x6a09e667, -0x4498517b, 0x3c6ef372, -0x5ab00ac6,
            0x510e527f, -0x64fa9774, 0x1f83d9ab, 0x5be0cd19
        )

        private val K = intArrayOf(
            0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b,
            0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
            -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3,
            0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c,
            -0x1b64963f, -0x1041b87a, 0x0fc19dc6, 0x240ca1cc,
            0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039,
            -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
            0x650a7354, 0x766a0abb, -0x7e3d36d2, -0x6d8dd37b,
            -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d,
            -0x2e6d17e7, -0x2966f9dc, -0xbf1ca7b, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
            0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, -0x7b3787ec, -0x7338fdf8,
            -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e
        )
    }


    private val h = IntArray(8)
    private val r = IntArray(8)
    private val w = IntArray(64)

    init {
        coreReset()
    }

    override fun coreReset() {
        arraycopy(H, 0, h, 0, 8)
    }

    override fun coreUpdate(chunk: ByteArray) {
        arraycopy(h, 0, r, 0, 8)

        for (j in 0 until 16) w[j] = chunk.readS32_be(j * 4)
        for (j in 16 until 64) {
            val s0 = w[j - 15].rotateRight(7) xor w[j - 15].rotateRight(18) xor w[j - 15].ushr(3)
            val s1 = w[j - 2].rotateRight(17) xor w[j - 2].rotateRight(19) xor w[j - 2].ushr(10)
            w[j] = w[j - 16] + s0 + w[j - 7] + s1
        }

        for (j in 0 until 64) {
            val s1 = r[4].rotateRight(6) xor r[4].rotateRight(11) xor r[4].rotateRight(25)
            val ch = r[4] and r[5] xor (r[4].inv() and r[6])
            val t1 = r[7] + s1 + ch + K[j] + w[j]
            val s0 = r[0].rotateRight(2) xor r[0].rotateRight(13) xor r[0].rotateRight(22)
            val maj = r[0] and r[1] xor (r[0] and r[2]) xor (r[1] and r[2])
            val t2 = s0 + maj
            r[7] = r[6]
            r[6] = r[5]
            r[5] = r[4]
            r[4] = r[3] + t1
            r[3] = r[2]
            r[2] = r[1]
            r[1] = r[0]
            r[0] = t1 + t2

        }
        for (j in 0 until 8) h[j] += r[j]
    }

    override fun coreDigest(out: ByteArray) {
        for (n in out.indices) out[n] = (h[n / 4] ushr (24 - 8 * (n % 4))).toByte()
    }
}


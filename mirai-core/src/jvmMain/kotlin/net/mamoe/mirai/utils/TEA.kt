package net.mamoe.mirai.utils

import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.xor

/**
 * TEA 加密
 *
 * @author iweiz https://github.com/iweizime/StepChanger/blob/master/app/src/main/java/me/iweizi/stepchanger/qq/Cryptor.java
 */
object TEA {
    private const val UINT32_MASK = 0xffffffffL

    private fun doOption(data: ByteArray, key: ByteArray, encrypt: Boolean): ByteArray {
        val mRandom = Random()
        lateinit var mOutput: ByteArray
        lateinit var mInBlock: ByteArray
        var mIndexPos: Int
        lateinit var mIV: ByteArray
        var mOutPos = 0
        var mPreOutPos = 0
        var isFirstBlock = true

        val mKey = LongArray(4)

        for (i in 0..3) {
            mKey[i] = pack(key, i * 4, 4)
        }

        fun rand(): Int {
            return mRandom.nextInt()
        }

        fun encode(bytes: ByteArray): ByteArray {
            var v0 = pack(bytes, 0, 4)
            var v1 = pack(bytes, 4, 4)
            var sum: Long = 0
            val delta = 0x9e3779b9L
            for (i in 0..15) {
                sum = sum + delta and UINT32_MASK
                v0 += (v1 shl 4) + mKey[0] xor v1 + sum xor v1.ushr(5) + mKey[1]
                v0 = v0 and UINT32_MASK
                v1 += (v0 shl 4) + mKey[2] xor v0 + sum xor v0.ushr(5) + mKey[3]
                v1 = v1 and UINT32_MASK
            }
            return ByteBuffer.allocate(8).putInt(v0.toInt()).putInt(v1.toInt()).array()
        }

        fun decode(bytes: ByteArray, offset: Int): ByteArray {
            var v0 = pack(bytes, offset, 4)
            var v1 = pack(bytes, offset + 4, 4)
            val delta = 0x9e3779b9L
            var sum = delta shl 4 and UINT32_MASK
            for (i in 0..15) {
                v1 -= (v0 shl 4) + mKey[2] xor v0 + sum xor v0.ushr(5) + mKey[3]
                v1 = v1 and UINT32_MASK
                v0 -= (v1 shl 4) + mKey[0] xor v1 + sum xor v1.ushr(5) + mKey[1]
                v0 = v0 and UINT32_MASK
                sum = sum - delta and UINT32_MASK
            }
            return ByteBuffer.allocate(8).putInt(v0.toInt()).putInt(v1.toInt()).array()
        }

        fun encodeOneBlock() {
            mIndexPos = 0
            while (mIndexPos < 8) {
                mInBlock[mIndexPos] = if (isFirstBlock)
                    mInBlock[mIndexPos]
                else
                    (mInBlock[mIndexPos] xor mOutput[mPreOutPos + mIndexPos])
                mIndexPos++
            }

            System.arraycopy(encode(mInBlock), 0, mOutput, mOutPos, 8)
            mIndexPos = 0
            while (mIndexPos < 8) {
                val outPos = mOutPos + mIndexPos
                mOutput[outPos] = (mOutput[outPos] xor mIV[mIndexPos])
                mIndexPos++
            }
            System.arraycopy(mInBlock, 0, mIV, 0, 8)
            mPreOutPos = mOutPos
            mOutPos += 8
            mIndexPos = 0
            isFirstBlock = false
        }

        fun decodeOneBlock(ciphertext: ByteArray, offset: Int, len: Int): Boolean {
            mIndexPos = 0
            while (mIndexPos < 8) {
                if (mOutPos + mIndexPos < len) {
                    mIV[mIndexPos] = (mIV[mIndexPos] xor ciphertext[mOutPos + offset + mIndexPos])
                    mIndexPos++
                    continue
                }
                return true
            }

            mIV = decode(mIV, 0)
            mOutPos += 8
            mIndexPos = 0
            return true

        }

        @Suppress("NAME_SHADOWING")
        fun encrypt(plaintext: ByteArray, offset: Int, len: Int): ByteArray {
            var len = len
            var offset = offset
            mInBlock = ByteArray(8)
            mIV = ByteArray(8)
            mOutPos = 0
            mPreOutPos = 0
            isFirstBlock = true
            mIndexPos = (len + 10) % 8
            if (mIndexPos != 0) {
                mIndexPos = 8 - mIndexPos
            }
            mOutput = ByteArray(mIndexPos + len + 10)
            mInBlock[0] = (rand() and 0xf8 or mIndexPos).toByte()
            for (i in 1..mIndexPos) {
                mInBlock[i] = (rand() and 0xff).toByte()
            }
            ++mIndexPos
            for (i in 0..7) {
                mIV[i] = 0
            }

            var g = 0
            while (g < 2) {
                if (mIndexPos < 8) {
                    mInBlock[mIndexPos++] = (rand() and 0xff).toByte()
                    ++g
                }
                if (mIndexPos == 8) {
                    encodeOneBlock()
                }
            }

            while (len > 0) {
                if (mIndexPos < 8) {
                    mInBlock[mIndexPos++] = plaintext[offset++]
                }
                if (mIndexPos == 8) {
                    encodeOneBlock()
                }
                len--
            }
            g = 0
            while (g < 7) {
                if (mIndexPos < 8) {
                    mInBlock[mIndexPos++] = 0.toByte()
                }
                if (mIndexPos == 8) {
                    encodeOneBlock()
                }
                g++
            }
            return mOutput
        }

        fun decrypt(cipherText: ByteArray, offset: Int, len: Int): ByteArray? {
            require(!(len % 8 != 0 || len < 16)) { "data must len % 8 == 0 && len >= 16" }
            mIV = decode(cipherText, offset)
            mIndexPos = (mIV[0] and 7).toInt()
            var plen = len - mIndexPos - 10
            isFirstBlock = true
            if (plen < 0) {
                return null
            }
            mOutput = ByteArray(plen)
            mPreOutPos = 0
            mOutPos = 8
            ++mIndexPos
            var g = 0
            while (g < 2) {
                if (mIndexPos < 8) {
                    ++mIndexPos
                    ++g
                }
                if (mIndexPos == 8) {
                    isFirstBlock = false
                    if (!decodeOneBlock(cipherText, offset, len)) {
                        throw RuntimeException("Unable to dataDecode")
                    }
                }
            }

            var outpos = 0
            while (plen != 0) {
                if (mIndexPos < 8) {
                    mOutput[outpos++] = if (isFirstBlock)
                        mIV[mIndexPos]
                    else
                        (cipherText[mPreOutPos + offset + mIndexPos] xor mIV[mIndexPos])
                    ++mIndexPos
                }
                if (mIndexPos == 8) {
                    mPreOutPos = mOutPos - 8
                    isFirstBlock = false
                    if (!decodeOneBlock(cipherText, offset, len)) {
                        throw RuntimeException("Unable to dataDecode")
                    }
                }
                plen--
            }
            g = 0
            while (g < 7) {
                if (mIndexPos < 8) {
                    if (cipherText[mPreOutPos + offset + mIndexPos].xor(mIV[mIndexPos]).toInt() != 0) {
                        throw RuntimeException()
                    } else {
                        ++mIndexPos
                    }
                }

                if (mIndexPos == 8) {
                    mPreOutPos = mOutPos
                    if (!decodeOneBlock(cipherText, offset, len)) {
                        throw RuntimeException("Unable to dataDecode")
                    }
                }
                g++
            }
            return mOutput
        }

        return if (encrypt) {
            encrypt(data, 0, data.size)
        } else {
            try {
                return decrypt(data, 0, data.size)!!
            } catch (e: Exception) {
                //println("Source: " + data.toUHexString(" "))
                // println("Key: " + key.toUHexString(" "))
                throw e
            }
        }
    }

    fun encrypt(source: ByteArray, key: ByteArray): ByteArray {
        return doOption(source, key, true)
    }

    @Suppress("unused")
    fun encrypt(source: ByteArray, keyHex: String): ByteArray {
        return encrypt(source, keyHex.hexToBytes())
    }

    fun decrypt(source: ByteArray, key: ByteArray): ByteArray {
        return doOption(source, key, false)
    }

    fun decrypt(source: ByteArray, keyHex: String): ByteArray {
        return decrypt(source, keyHex.hexToBytes())
    }

    @Suppress("SameParameterValue")
    private fun pack(bytes: ByteArray, offset: Int, len: Int): Long {
        var result: Long = 0
        val maxOffset = if (len > 8) offset + 8 else offset + len
        for (index in offset until maxOffset) {
            result = result shl 8 or (bytes[index].toLong() and 0xffL)
        }
        return result shr 32 or (result and UINT32_MASK)
    }
}
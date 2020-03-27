@file:Suppress("NAME_SHADOWING")

package net.mamoe.mirai.qqandroid.utils.cryptor

import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.utils.io.toByteArray
import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.random.Random

// $FF: renamed from: com.tencent.qphone.base.util.b
internal class class_1457 {
    // $FF: renamed from: a byte[]
    private lateinit var field_71278: ByteArray

    // $FF: renamed from: b byte[]
    private  var field_71279: ByteArray? = null

    // $FF: renamed from: c byte[]
    private lateinit  var field_71280: ByteArray

    // $FF: renamed from: d int
    private var field_71281 = 0

    // $FF: renamed from: e int
    private var field_71282 = 0

    // $FF: renamed from: f int
    private var field_71283 = 0

    // $FF: renamed from: g int
    private var field_71284 = 0

    // $FF: renamed from: h byte[]
    private  var field_71285: ByteArray? = null

    // $FF: renamed from: i boolean
    private var field_71286 = true

    // $FF: renamed from: j int
    private var field_71287 = 0

    // $FF: renamed from: k java.util.Random
    private val field_71288: Random = Random

    // $FF: renamed from: l boolean
    private var field_71289 = true

    // $FF: renamed from: a () void
    private fun method_67415() {
        var var1: Int
        var var2: ByteArray
        field_71283 = 0
        while (field_71283 < 8) {
            if (field_71286) {
                var2 = field_71278
                var1 = field_71283
                var2[var1] = var2[var1] xor field_71279!![field_71283]
            } else {
                var2 = field_71278
                var1 = field_71283
                var2[var1] = var2[var1] xor field_71280[field_71282 + field_71283]
            }
            ++field_71283
        }
        arraycopy(method_67417(field_71278), 0, field_71280, field_71281, 8)
        field_71283 = 0
        while (field_71283 < 8) {
            var2 = field_71280
            var1 = field_71281 + field_71283
            var2[var1] = var2[var1] xor field_71279!![field_71283]
            ++field_71283
        }
        arraycopy(field_71278, 0, field_71279!!, 0, 8)
        field_71282 = field_71281
        field_71281 += 8
        field_71283 = 0
        field_71286 = false
    }

    // $FF: renamed from: a (int) byte[]
    private fun method_67416(var1: Int): ByteArray {
        val var2 = ByteArray(var1)
        field_71288.nextBytes(var2)
        return var2
    }

    // $FF: renamed from: a (byte[]) byte[]
    private fun method_67417(var1: ByteArray): ByteArray {
        var var1: ByteArray? = var1
        var var2 = 16
        var var5: Long
        var var7: Long
        val var9: Long
        val var11: Long
        val var13: Long
        val var15: Long
        var7 = method_67414(var1, 0, 4)
        var5 = method_67414(var1, 4, 4)
        var9 = method_67414(field_71285, 0, 4)
        var11 = method_67414(field_71285, 4, 4)
        var13 = method_67414(field_71285, 8, 4)
        var15 = method_67414(field_71285, 12, 4)
        var var3 = 0L
        while (var2 > 0) {
            var3 = var3 + (-1640531527L and 4294967295L) and 4294967295L
            var7 = var7 + ((var5 shl 4) + var9 xor var5 + var3 xor (var5 ushr 5) + var11) and 4294967295L
            var5 = var5 + ((var7 shl 4) + var13 xor var7 + var3 xor (var7 ushr 5) + var15) and 4294967295L
            --var2
        }
        return buildPacket { 
            writeInt(var7.toInt())
            writeInt(var5.toInt())
        }.readBytes()
    }

    // $FF: renamed from: a (byte[], int) byte[]
    private fun method_67418(var1: ByteArray?, var2: Int): ByteArray? {
        var var1 = var1
        var var2 = var2
        val var3: Byte = 16
        var var6: Long
        var var8: Long
        val var10: Long
        val var12: Long
        val var14: Long
        val var16: Long
        var8 = method_67414(var1, var2, 4)
        var6 = method_67414(var1, var2 + 4, 4)
        var10 = method_67414(field_71285, 0, 4)
        var12 = method_67414(field_71285, 4, 4)
        var14 = method_67414(field_71285, 8, 4)
        var16 = method_67414(field_71285, 12, 4)
        var var4 = -478700656L and 4294967295L
        var2 = var3.toInt()
        while (var2 > 0) {
            var6 = var6 - ((var8 shl 4) + var14 xor var8 + var4 xor (var8 ushr 5) + var16) and 4294967295L
            var8 = var8 - ((var6 shl 4) + var10 xor var6 + var4 xor (var6 ushr 5) + var12) and 4294967295L
            var4 = var4 - (-1640531527L and 4294967295L) and 4294967295L
            --var2
        }
        return var8.toByteArray() + var6.toByteArray()
    }

    // $FF: renamed from: a (byte[], byte[], int) byte[]
    private fun method_67419(var1: ByteArray, var2: ByteArray, var3: Int): ByteArray? {
        var var1: ByteArray? = var1
        var var2: ByteArray? = var2
        var2 = method_67425(var1, 0, var1!!.size, var2)
        var1 = var2
        if (var2 == null) {
            var1 = method_67416(var3)
        }
        return var1
    }

    // $FF: renamed from: b () int
    private fun method_67420(): Int {
        return if (field_71289) field_71288.nextInt() else 16711935
    }

    // $FF: renamed from: b (byte[], int, int) boolean
    private fun method_67421(var1: ByteArray?, var2: Int, var3: Int): Boolean {
        field_71283 = 0
        while (field_71283 < 8) {
            if (field_71287 + field_71283 >= var3) {
                return true
            }
            val var5 = field_71279
            val var4 = field_71283
            var5!![var4] = var5[var4] xor var1!![field_71281 + var2 + field_71283]
            ++field_71283
        }
        field_71279 = method_67422(field_71279)
        return if (field_71279 == null) {
            false
        } else {
            field_71287 += 8
            field_71281 += 8
            field_71283 = 0
            true
        }
    }

    // $FF: renamed from: b (byte[]) byte[]
    private fun method_67422(var1: ByteArray?): ByteArray? {
        return method_67418(var1, 0)
    }

    // $FF: renamed from: b (byte[], int, int, byte[]) byte[]
    private fun method_67423(var1: ByteArray, var2: Int, var3: Int, var4: ByteArray): ByteArray {
        var var1 = var1
        var var2 = var2
        var var3 = var3
        var var4 = var4
        field_71278 = ByteArray(8)
        field_71279 = ByteArray(8)
        field_71283 = 1
        field_71284 = 0
        field_71282 = 0
        field_71281 = 0
        field_71285 = var4
        field_71286 = true
        field_71283 = (var3 + 10) % 8
        if (field_71283 != 0) {
            field_71283 = 8 - field_71283
        }
        field_71280 = ByteArray(field_71283 + var3 + 10)
        field_71278[0] = (method_67420() and 248 or field_71283).toByte()
        var var5: Int
        var5 = 1
        while (var5 <= field_71283) {
            field_71278[var5] = (method_67420() and 255).toByte()
            ++var5
        }
        ++field_71283
        var5 = 0
        while (var5 < 8) {
            field_71279!![var5] = 0
            ++var5
        }
        field_71284 = 1
        while (field_71284 <= 2) {
            if (field_71283 < 8) {
                var4 = field_71278
                var5 = field_71283++
                var4[var5] = (method_67420() and 255).toByte()
                ++field_71284
            }
            if (field_71283 == 8) {
                method_67415()
            }
        }
        while (var3 > 0) {
            if (field_71283 < 8) {
                var4 = field_71278
                val var6 = field_71283++
                var5 = var2 + 1
                var4[var6] = var1[var2]
                --var3
                var2 = var5
            }
            if (field_71283 == 8) {
                method_67415()
            }
        }
        field_71284 = 1
        while (field_71284 <= 7) {
            if (field_71283 < 8) {
                var1 = field_71278
                var2 = field_71283++
                var1[var2] = 0
                ++field_71284
            }
            if (field_71283 == 8) {
                method_67415()
            }
        }
        return field_71280
    }

    // $FF: renamed from: a (boolean) void
    fun method_67424(var1: Boolean) {
        field_71289 = var1
    }

    // $FF: renamed from: a (byte[], int, int, byte[]) byte[]
    internal fun method_67425(
        var1: ByteArray?,
        var2: Int,
        var3: Int,
        var4: ByteArray?
    ): ByteArray? {
        var var4 = var4
        field_71282 = 0
        field_71281 = 0
        field_71285 = var4
        var4 = ByteArray(var2 + 8)
        return if (var3 % 8 == 0 && var3 >= 16) {
            field_71279 = method_67418(var1, var2)
            field_71283 = (field_71279!![0] and 7).toInt()
            var var6 = var3 - field_71283 - 10
            if (var6 < 0) {
                null
            } else {
                var var5: Int
                var5 = var2
                while (var5 < var4.size) {
                    var4[var5] = 0
                    ++var5
                }
                field_71280 = ByteArray(var6)
                field_71282 = 0
                field_71281 = 8
                field_71287 = 8
                ++field_71283
                field_71284 = 1
                while (field_71284 <= 2) {
                    if (field_71283 < 8) {
                        ++field_71283
                        ++field_71284
                    }
                    if (field_71283 == 8) {
                        if (!method_67421(var1, var2, var3)) {
                            return null
                        }
                        var4 = var1
                    }
                }
                var5 = 0
                while (var6 != 0) {
                    if (field_71283 < 8) {
                        field_71280[var5] =
                            (var4!![field_71282 + var2 + field_71283] xor field_71279!![field_71283])
                        ++var5
                        ++field_71283
                        --var6
                    }
                    if (field_71283 == 8) {
                        field_71282 = field_71281 - 8
                        if (!method_67421(var1, var2, var3)) {
                            return null
                        }
                        var4 = var1
                    }
                }
                field_71284 = 1
                while (field_71284 < 8) {
                    if (field_71283 < 8) {
                        if (var4!![field_71282 + var2 + field_71283] xor field_71279!![field_71283] != 0.toByte()) {
                            return null
                        }
                        ++field_71283
                    }
                    if (field_71283 == 8) {
                        field_71282 = field_71281
                        if (!method_67421(var1, var2, var3)) {
                            return null
                        }
                        var4 = var1
                    }
                    ++field_71284
                }
                field_71280
            }
        } else {
            null
        }
    }

    // $FF: renamed from: a (byte[], byte[]) byte[]
    internal fun method_67426(var1: ByteArray, var2: ByteArray?): ByteArray? {
        return method_67425(var1, 0, var1.size, var2)
    }

    // $FF: renamed from: b (byte[], byte[]) byte[]
    internal fun method_67427(var1: ByteArray, var2: ByteArray): ByteArray {
        return method_67423(var1, 0, var1.size, var2)
    }

    companion object {
        // $FF: renamed from: a (byte[], int, int) long
        private fun method_67414(var0: ByteArray?, var1: Int, var2: Int): Long {
            var var1 = var1
            var var2 = var2
            var var3 = 0L
            if (var2 > 8) {
                var2 = var1 + 8
            } else {
                var2 += var1
            }
            while (var1 < var2) {
                var3 = var3 shl 8 or (var0!![var1] and 255.toByte()).toLong()
                ++var1
            }
            return 4294967295L and var3 or var3 ushr 32
        }
    }
}

internal expect fun arraycopy(src: ByteArray, srcPos: Int, dest: ByteArray, destPos: Int, length: Int)
@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.utils

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.dataInputStream
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.lang.reflect.Field
import java.util.*
import java.util.zip.CRC32
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.imageio.ImageIO
import kotlin.jvm.JvmSynthetic


/**
 * @author Him188moe
 * @author NaturalHG
 */

@JvmSynthetic
fun ByteArray.toHexString(): String = toHexString(" ")

fun ByteArray.toHexString(separator: String = " "): String = this.joinToString(separator) {
    var ret = it.toString(16).toUpperCase()
    if (ret.length == 1) {
        ret = "0$ret"
    }
    return@joinToString ret
}


fun ByteArray.toUHexString(separator: String = " "): String = this.toUByteArray().toUHexString(separator)


@JvmSynthetic
fun ByteArray.toUHexString(): String = this.toUByteArray().toUHexString()


@JvmSynthetic
fun UByteArray.toUHexString(separator: String = " "): String {
    return this.joinToString(separator) {
        var ret = it.toString(16).toUpperCase()
        if (ret.length == 1) {
            ret = "0$ret"
        }
        return@joinToString ret
    }
}


@JvmSynthetic
fun UByteArray.toUHexString(): String = this.toUHexString(" ")


fun Byte.toUHexString(): String = this.toUByte().toString(16)


fun String.hexToBytes(): ByteArray = TIMProtocol.hexToBytes(this)


fun String.hexToUBytes(): UByteArray = TIMProtocol.hexToUBytes(this)


fun String.hexToInt(): Int = hexToBytes().toUInt().toInt()


fun ByteArray.toUInt(): UInt =
        this[0].toUInt().and(255u).shl(24) + this[1].toUInt().and(255u).shl(16) + this[2].toUInt().and(255u).shl(8) + this[3].toUInt().and(255u).shl(0)

open class ByteArrayDataOutputStream : DataOutputStream(ByteArrayOutputStream()) {
    open fun toByteArray(): ByteArray = (out as ByteArrayOutputStream).toByteArray()

    open fun toUByteArray(): UByteArray = (out as ByteArrayOutputStream).toByteArray().toUByteArray()
}

@JvmSynthetic
fun lazyEncode(t: (ByteArrayDataOutputStream) -> Unit): ByteArray = ByteArrayDataOutputStream().also(t).toByteArray()

@JvmSynthetic
fun <T> lazyDecode(byteArray: ByteArray, t: (DataInputStream) -> T): T = byteArray.dataInputStream().let(t)

fun DataInputStream.skip(n: Number) {
    this.skip(n.toLong())
}

fun getRandomByteArray(length: Int): ByteArray {
    val bytes = LinkedList<Byte>()
    repeat(length) { bytes.add((Math.random() * 255).toByte()) }
    return bytes.toByteArray()
}

@JvmSynthetic
operator fun File.plus(child: String): File = File(this, child)

private const val GTK_BASE_VALUE: Int = 5381

internal fun getGTK(sKey: String): Int {
    var value = GTK_BASE_VALUE
    for (c in sKey.toCharArray()) {
        value += (value shl 5) + c.toInt()
    }

    value = value and Int.MAX_VALUE
    return value
}

internal fun getCrc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }


/**
 * 获取类的所有字段(类成员变量), 包括父类的和私有的. <br></br>
 * 相当于将这个类和它所有父类的 [Class.getDeclaredFields] 都合并成一个 [List] <br></br>
 * 不会排除重名的字段. <br></br>
 *
 * @return field list
 */
@Throws(SecurityException::class)
fun Any.getAllDeclaredFields(): List<Field> {
    var clazz: Class<*> = this.javaClass
    val list = LinkedList<Field>()
    loop@ do {

        if (!clazz.name.contains("net.mamoe")) {
            break@loop
        }

        list.addAll(clazz.declaredFields.filter { (it.name == "Companion" || it.name == "input").not() }.toList())

        if (clazz.superclass == null) {
            break
        }
        clazz = clazz.superclass

    } while (clazz != Object::javaClass)

    return list
}

private const val ZERO_BYTE: Byte = 0

fun ByteArray.removeZeroTail(): ByteArray {
    var i = this.size - 1
    while (this[i] == ZERO_BYTE) {
        --i
    }
    return this.copyOfRange(0, i + 1)
}

fun BufferedImage.toByteArray(formatName: String = "PNG"): ByteArray {
    return lazyEncode {
        ImageIO.write(this, formatName, it)
    }
}

object GZip {
    fun uncompress(bytes: ByteArray): ByteArray = lazyEncode {
        GZIPInputStream(bytes.inputStream()).transferTo(it)
    }

    fun compress(bytes: ByteArray): ByteArray = ByteArrayOutputStream().let {
        GZIPOutputStream(it).write(bytes)
        return it.toByteArray()
    }
}
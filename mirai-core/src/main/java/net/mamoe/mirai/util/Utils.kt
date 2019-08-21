package net.mamoe.mirai.util

import net.mamoe.mirai.network.Protocol
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.reflect.Field
import java.util.*
import java.util.zip.CRC32

/**
 * @author Him188moe
 */
object Utils {
    fun toHexString(byteArray: ByteArray, separator: String = " "): String = byteArray.joinToString(separator) {
        var ret = it.toString(16).toUpperCase();
        if (ret.length == 1) {
            ret = "0$ret";
        }
        return@joinToString ret;
    }

    @ExperimentalUnsignedTypes
    fun toHexString(byteArray: UByteArray, separator: String = " "): String = byteArray.joinToString(separator) {
        var ret = it.toString(16).toUpperCase();
        if (ret.length == 1) {
            ret = "0$ret";
        }
        return@joinToString ret;
    }
}

fun ByteArray.toHexString(): String = toHexString(" ")
fun ByteArray.toHexString(separator: String = " "): String = Utils.toHexString(this, separator)
@ExperimentalUnsignedTypes
fun ByteArray.toUHexString(separator: String = " "): String = this.toUByteArray().toHexString(separator)

@ExperimentalUnsignedTypes
fun ByteArray.toUHexString(): String = this.toUByteArray().toHexString()

@ExperimentalUnsignedTypes
fun UByteArray.toHexString(separator: String = " "): String = Utils.toHexString(this, separator)

@ExperimentalUnsignedTypes
fun UByteArray.toHexString(): String = toHexString(" ")

@ExperimentalUnsignedTypes
fun Byte.toHexString(): String = this.toUByte().toString(16)

@ExperimentalUnsignedTypes
fun String.hexToBytes(): ByteArray = Protocol.hexToBytes(this)

@ExperimentalUnsignedTypes
fun String.hexToUBytes(): UByteArray = Protocol.hexToUBytes(this)

@ExperimentalUnsignedTypes
fun String.hexToShort(): Short = hexToBytes().let { ((it[1].toInt() shl 8) + it[0]).toShort() }

@ExperimentalUnsignedTypes
fun String.hexToByte(): Byte = hexToBytes()[0]

open class ByteArrayDataOutputStream : DataOutputStream(ByteArrayOutputStream()) {
    open fun toByteArray(): ByteArray = (out as ByteArrayOutputStream).toByteArray()
    @ExperimentalUnsignedTypes
    open fun toUByteArray(): UByteArray = (out as ByteArrayOutputStream).toByteArray().toUByteArray();
}

fun lazyEncode(t: (ByteArrayDataOutputStream) -> Unit): ByteArray = ByteArrayDataOutputStream().let { t(it); return it.toByteArray() }

@ExperimentalUnsignedTypes
fun getRandomKey(length: Int): ByteArray {
    val bytes = LinkedList<Byte>();
    repeat(length) { bytes.add((Math.random() * 255).toByte()) }
    return bytes.toByteArray();
}

fun getCrc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }


/**
 * 获取类的所有字段(类成员变量), 包括父类的和私有的. <br></br>
 * 相当于将这个类和它所有父类的 [Class.getDeclaredFields] 都合并成一个 [List] <br></br>
 * 不会排除重名的字段. <br></br>
 *
 * @param clazz class
 *
 * @return field list
 */
@Throws(SecurityException::class)
fun Any.getAllDeclaredFields(): List<Field> {
    var clazz: Class<*> = this.javaClass;
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

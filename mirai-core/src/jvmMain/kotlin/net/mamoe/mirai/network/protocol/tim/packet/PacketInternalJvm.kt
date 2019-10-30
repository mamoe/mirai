@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.utils.io.toUHexString
import java.lang.reflect.Field
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.jvm.kotlinProperty

internal object PacketNameFormatter {
    @JvmStatic
    private var longestNameLength: Int = 43

    @JvmStatic
    fun adjustName(name: String): String {
        if (name.length > longestNameLength) {
            longestNameLength = name.length
            return name
        }

        return " ".repeat(longestNameLength - name.length) + name
    }
}

private object IgnoreIdListEquals : List<String> by listOf(
    "idHex",
    "id",
    "packetId",
    "sequenceIdInternal",
    "sequenceId",
    "fixedId",
    "idByteArray",
    "encoded",
    "packet",
    "EMPTY_ID_HEX",
    "input",
    "sequenceId",
    "output",
    "bot",
    "UninitializedByteReadPacket",
    "sessionKey"
)

private object IgnoreIdListInclude : List<String> by listOf(
    "Companion",
    "EMPTY_ID_HEX",
    "input",
    "output",
    "this\$",
    "\$\$delegatedProperties",
    "UninitializedByteReadPacket",
    "\$FU",
    "RefVolatile"
)

/**
 * 这个方法会翻倍内存占用, 考虑修改.
 */
@Suppress("UNCHECKED_CAST")
internal actual fun Packet.packetToString(): String = PacketNameFormatter.adjustName(this::class.simpleName + "(${this.idHexString})") + this::class.java.allDeclaredFields
    .filterNot { field ->
        IgnoreIdListEquals.any { field.name.replace("\$delegate", "") == it } || IgnoreIdListInclude.any { it in field.name }
    }
    .joinToString(", ", "{", "}") {
        it.isAccessible = true
        it.name.replace("\$delegate", "") + "=" + it.get(this).let { value ->
            when (value) {
                null -> null
                is ByteArray -> value.toUHexString()
                is UByteArray -> value.toUHexString()
                is ByteReadPacket -> "[ByteReadPacket(${value.remaining})]"
                //is ByteReadPacket -> value.copy().readBytes().toUHexString()
                is IoBuffer -> "[IoBuffer(${value.readRemaining})]"
                is Lazy<*> -> "[Lazy]"
                is ReadWriteProperty<*, *> -> (value as ReadWriteProperty<Packet, *>).getValue(this, it.kotlinProperty!!)
                else -> value.toString()
            }
        }
    }

private val Class<*>.allDeclaredFields: List<Field>
    get() {
        val list = mutableListOf<Field>()

        var clazz: Class<*> = this
        do {
            list.addAll(clazz.declaredFields)
        } while (clazz.let { clazz = it.superclass; clazz.kotlin != Any::class })

        return list
    }
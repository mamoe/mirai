@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.utils.toUHexString
import java.lang.reflect.Field

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

private object IgnoreIdList : List<String> by listOf(
        "idHex",
        "id",
        "packetId",
        "sequenceIdInternal",
        "sequenceId",
        "fixedId",
        "idByteArray",
        "encoded",
        "packet",
        "Companion",
        "EMPTY_ID_HEX",
        "input",
        "output",
        "UninitializedByteReadPacket",
        "sessionKey"
)

internal actual fun Packet.packetToString(): String = PacketNameFormatter.adjustName(this::class.simpleName + "(${this.idHexString})") + this::class.java.allDeclaredFields
        .filterNot { it.name in IgnoreIdList || "delegate" in it.name || "$" in it.name }
        .joinToString(", ", "{", "}") {
            it.isAccessible = true
            it.name + "=" + it.get(this).let { value ->
                when (value) {
                    null -> null
                    is ByteArray -> value.toUHexString()
                    is UByteArray -> value.toUHexString()
                    is ByteReadPacket -> "[ByteReadPacket(${value.remaining})]"
                    //is ByteReadPacket -> value.copy().readBytes().toUHexString()
                    is IoBuffer -> "[IoBuffer(${value.readRemaining})]"
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
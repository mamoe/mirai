@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.message

/**
 * @author Him188moe
 */
@Suppress("unused")
enum class MessageType(private val value: UByte) {
    PLAIN_TEXT(0x03u),
    AT(0x06u),
    FACE(0x02u),
    IMAGE(0x03u),//may be 0x06?
    ;


    val intValue: Int = this.value.toInt()
}
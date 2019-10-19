@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.message


@Suppress("unused")
enum class MessageType(val value: UByte) {
    PLAIN_TEXT(0x01u),
    AT(0x06u),
    FACE(0x02u),
    GROUP_IMAGE(0x03u),
    FRIEND_IMAGE(0x06u),
    ;


    val intValue: Int = this.value.toInt()
}
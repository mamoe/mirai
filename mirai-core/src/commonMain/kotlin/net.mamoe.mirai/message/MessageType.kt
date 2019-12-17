@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.message


@Suppress("unused")
enum class MessageType(val value: UByte) {
    PLAIN_TEXT(0x01u),
    AT(0x01u), // same as PLAIN
    FACE(0x02u),
    /**
     * [ImageId.value] 长度为 42 的图片
     */
    IMAGE_42(0x03u),
    /**
     * [ImageId.value] 长度为 37 的图片
     */
    IMAGE_37(0x06u),
    XML(0x19u)
    ;


    inline val intValue: Int get() = this.value.toInt()
}
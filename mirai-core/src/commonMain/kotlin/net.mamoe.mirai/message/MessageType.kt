/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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
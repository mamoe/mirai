/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

internal enum class ChatType(val internalID: Int) {

    FRIEND(2),//可以为任何数字

    CONTACT(1006),

    //推测为"群"
    TROOP(1),
    TROOP_HCTOPIC(1026),

    //坦白说
    CONFESS_A(1033),
    CONFESS_B(1034),

    CM_GAME_TEMP(1036),

    DISCUSSION(3000),

    DEVICE_MSG(9501),
}
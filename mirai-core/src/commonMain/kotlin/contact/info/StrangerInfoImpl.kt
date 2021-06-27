/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.StrangerInfo

@SerialName("StrangerInfo")
@Serializable
internal class StrangerInfoImpl(
    override val uin: Long,
    override val nick: String,
    override val fromGroup: Long = 0,
    override val remark: String = "",
) : StrangerInfo {
    companion object {
        fun StrangerInfo.impl() = if (this is StrangerInfoImpl) this else StrangerInfoImpl(uin, nick, fromGroup, remark)
    }
}
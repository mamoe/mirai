/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.events

import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.jvm.JvmOverloads

/**
 * 陌生人请求添加机器人账号为好友
 */
class ReceiveFriendAddRequestEvent(
    _qq: QQ,
    /**
     * 验证消息
     */
    val message: String
) : EventPacket {
    val qq: QQ by _qq.unsafeWeakRef()

    /**
     * 同意这个请求
     *
     * @param remark 备注名, 不设置则需为 `null`
     */
    @JvmOverloads
    suspend fun approve(remark: String? = null): Unit = qq.bot.approveFriendAddRequest(qq.id, remark)
}

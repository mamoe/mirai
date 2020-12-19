/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MemberDeprecatedApi

/**
 * 匿名
 *
 * 代表匿名群成员
 */
public interface AnonymousMember : Member {
    /** 该匿名群成员 ID */
    public val anonymousId: String

    @MemberDeprecatedApi(message = "无法发送信息至 AnonymousMember")
    @Deprecated(level = DeprecationLevel.ERROR, message = "无法发送信息至 AnonymousMember")
    public override suspend fun sendMessage(message: Message): Nothing

    @Deprecated(level = DeprecationLevel.ERROR, message = "无法发送信息至 AnonymousMember")
    @MemberDeprecatedApi(message = "无法发送信息至 AnonymousMember")
    public override suspend fun sendMessage(message: String): Nothing

}

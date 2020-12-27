/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource

/**
 * 匿名
 *
 * 代表匿名群成员
 */
@Suppress("DeprecatedCallableAddReplaceWith")
public interface AnonymousMember : Member {
    /** 该匿名群成员 ID */
    public val anonymousId: String

    @Deprecated(level = DeprecationLevel.ERROR, message = "无法发送信息至 AnonymousMember")
    public override suspend fun sendMessage(message: Message): Nothing =
        throw UnsupportedOperationException("Cannot send message to AnonymousMember")

    @Deprecated(level = DeprecationLevel.ERROR, message = "无法发送信息至 AnonymousMember")
    public override suspend fun sendMessage(message: String): Nothing =
        throw UnsupportedOperationException("Cannot send message to AnonymousMember")

    override fun nudge(): MemberNudge = throw UnsupportedOperationException("Cannot nudge AnonymousMember")
    override suspend fun uploadImage(resource: ExternalResource): Image =
        throw UnsupportedOperationException("Cannot upload image to AnonymousMember")
}

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DeprecatedCallableAddReplaceWith")

package net.mamoe.mirai.contact

import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 代表匿名群成员.
 *
 * 可通过 [anonymousId] 获取其识别属性. [AnonymousMember.id] 的值由服务器提供因此不可靠.
 *
 * 匿名群成员不支持发送私聊消息, 戳一戳, 上传图片.
 *
 * @see NormalMember
 */
@NotStableForInheritance
public interface AnonymousMember : Member {
    /** 该匿名群成员 ID */
    public val anonymousId: String

    @Deprecated(level = DeprecationLevel.ERROR, message = "无法发送信息至 AnonymousMember") // diagnostic deprecation
    public override suspend fun sendMessage(message: Message): Nothing =
        throw UnsupportedOperationException("Cannot send message to AnonymousMember")

    @Deprecated(level = DeprecationLevel.ERROR, message = "无法发送信息至 AnonymousMember") // diagnostic deprecation
    public override suspend fun sendMessage(message: String): Nothing =
        throw UnsupportedOperationException("Cannot send message to AnonymousMember")

    override fun nudge(): MemberNudge = throw UnsupportedOperationException("Cannot nudge AnonymousMember")
    override suspend fun uploadImage(resource: ExternalResource): Image =
        throw UnsupportedOperationException("Cannot upload image to AnonymousMember")
}

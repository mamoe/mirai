/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.internal.message.protocol.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.runCoroutineInPlace
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.buildTypeSafeMap


internal val UNSUPPORTED_VOICE_MESSAGE_PLAIN = PlainText("收到语音消息，你需要升级到最新版QQ才能接收，升级地址https://im.qq.com")

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun MessageChain.toRichTextElems(
    messageTarget: ContactOrBot?,
    withGeneralFlags: Boolean,
    isForward: Boolean = false,
): MutableList<ImMsgBody.Elem> {
    val originalMessage = this
    val pipeline = MessageProtocolFacade.encoderPipeline

    val attributes = buildTypeSafeMap {
        set(MessageEncoderContext.CONTACT, messageTarget)
        set(MessageEncoderContext.ORIGINAL_MESSAGE, originalMessage)
        set(MessageEncoderContext.ADD_GENERAL_FLAGS, withGeneralFlags)
        set(MessageEncoderContext.IS_FORWARD, isForward)
    }

    val builder = ArrayList<ImMsgBody.Elem>(originalMessage.size)

    runCoroutineInPlace {
        originalMessage.forEach { builder.addAll(pipeline.process(it, attributes)) }
    }

    return builder
}

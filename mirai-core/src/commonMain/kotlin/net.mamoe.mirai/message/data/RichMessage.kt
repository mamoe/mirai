/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.SinceMirai

/**
 * XML 消息等富文本消息
 *
 * @see XmlMessage
 * @see JsonMessage
 * @see LightApp
 */
@SinceMirai("0.27.0")
interface RichMessage : MessageContent {
    companion object Key : Message.Key<RichMessage>

    val content: String
}
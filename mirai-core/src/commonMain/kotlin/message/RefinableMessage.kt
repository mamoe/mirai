/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.*

/**
 * 在接收解析消息后会经过一层转换的消息.
 * @see MessageChain.refineLight
 */
internal interface RefinableMessage : SingleMessage {

    /**
     * Refine if possible (without suspension), returns self otherwise.
     * @since 2.6
     */ // see #1157
    fun tryRefine(
        bot: Bot,
        context: MessageChain,
    ): Message? = this

    /**
     * This message [RefinableMessage] will be replaced by return value of [refineLight]
     */
    suspend fun refine(
        bot: Bot,
        context: MessageChain,
    ): Message? = tryRefine(bot, context)
}

internal sealed class MessageRefiner {
    protected inline fun MessageChain.refineImpl(
        bot: Bot,
        refineAction: (message: RefinableMessage) -> Message?
    ): MessageChain {
        val convertLineSeparator = bot.configuration.convertLineSeparator

        if (none {
                it is RefinableMessage
                        || (it is PlainText && convertLineSeparator && it.content.contains('\r'))
            }
        ) return this


        val builder = MessageChainBuilder(this.size)
        for (singleMessage in this) {
            if (singleMessage is RefinableMessage) {
                val v = refineAction(singleMessage)
                if (v != null) builder.add(v)
            } else if (singleMessage is PlainText && convertLineSeparator) {
                val content = singleMessage.content
                if (content.contains('\r')) {
                    builder.add(
                        PlainText(
                            content
                                .replace("\r\n", "\n")
                                .replace('\r', '\n')
                        )
                    )
                } else {
                    builder.add(singleMessage)
                }
            } else {
                builder.add(singleMessage)
            }
        }
        return builder.build()
    }
}

internal object LightMessageRefiner : MessageRefiner() {
    fun MessageChain.refineLight(bot: Bot): MessageChain {
        return refineImpl(bot) { it.tryRefine(bot, this) }
    }
}

internal object DeepMessageRefiner : MessageRefiner() {
    suspend fun MessageChain.refineDeep(bot: Bot): MessageChain {
        return refineImpl(bot) { it.refine(bot, this) }
    }
}
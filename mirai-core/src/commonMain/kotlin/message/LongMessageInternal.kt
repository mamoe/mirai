/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast

// internal runtime value, not serializable
internal data class LongMessageInternal internal constructor(override val content: String, val resId: String) :
    AbstractServiceMessage(), RefinableMessage {
    override val serviceId: Int get() = 35

    override suspend fun refine(contact: Contact, context: MessageChain): Message {
        val bot = contact.bot.asQQAndroidBot()
        val long = Mirai.downloadLongMessage(bot, resId)

        return RichMessageOrigin(SimpleServiceMessage(serviceId, content), resId, RichMessageKind.LONG) + long
    }

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, LongMessageInternal>(ServiceMessage, { it.safeCast() })
}

// internal runtime value, not serializable
@Suppress("RegExpRedundantEscape", "UnnecessaryVariable")
internal data class ForwardMessageInternal(override val content: String, val resId: String) : AbstractServiceMessage(),
    RefinableMessage {
    override val serviceId: Int get() = 35

    override suspend fun refine(contact: Contact, context: MessageChain): Message {
        val bot = contact.bot.asQQAndroidBot()

        val msgXml = content.substringAfter("<msg", "")
        val xmlHead = msgXml.substringBefore("<item")
        val xmlFoot: String
        val xmlContent = msgXml.substringAfter("<item").let {
            xmlFoot = it.substringAfter("</item", "")
            it.substringBefore("</item")
        }
        val brief = xmlHead.findField("brief")

        val summary = SUMMARY_REGEX.find(xmlContent)?.let { it.groupValues[1] } ?: ""

        val titles = TITLE_REGEX.findAll(xmlContent)
            .map { it.groupValues[2].trim() }.toMutableList()

        val title = titles.removeFirstOrNull() ?: ""

        val preview = titles
        val source = xmlFoot.findField("name")

        return RichMessageOrigin(SimpleServiceMessage(serviceId, content), resId, RichMessageKind.FORWARD) + ForwardMessage(
            preview = preview,
            title = title,
            brief = brief,
            source = source,
            summary = summary.trim(),
            nodeList = Mirai.downloadForwardMessage(bot, resId)
        )
    }

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, ForwardMessageInternal>(ServiceMessage, { it.safeCast() }) {

        val SUMMARY_REGEX = """\<summary.*\>(.*?)\<\/summary\>""".toRegex()

        @Suppress("SpellCheckingInspection")
        val TITLE_REGEX = """\<title([A-Za-z\s#\"0-9\=]*)\>([\u0000-\uFFFF]*?)\<\/title\>""".toRegex()


        fun String.findField(type: String): String {
            return substringAfter("$type=\"", "")
                .substringBefore("\"", "")
        }
    }
}

internal interface RefinableMessage : SingleMessage {

    /**
     * This message [RefinableMessage] will be replaced by return value of [refine]
     */
    suspend fun refine(
        contact: Contact,
        context: MessageChain,
    ): Message?
}
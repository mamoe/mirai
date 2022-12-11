/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.getMiraiImpl
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.RefineContextKey
import net.mamoe.mirai.internal.message.visitor.ex
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.encodeHtmlEscape
import net.mamoe.mirai.utils.safeCast

// internal runtime value, not serializable
internal data class LongMessageInternal internal constructor(override val content: String, val resId: String) :
    AbstractServiceMessage(), RefinableMessage {
    override val serviceId: Int get() = 35

    override suspend fun refine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message {
        bot.asQQAndroidBot()
        val long = Mirai.downloadLongMessage(bot, resId)

        return MessageOrigin(SimpleServiceMessage(serviceId, content), resId, MessageOriginKind.LONG) + long
    }

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitLongMessageInternal(this, data) ?: super<AbstractServiceMessage>.accept(visitor, data)
    }

    companion object Key :
        AbstractPolymorphicMessageKey<ServiceMessage, LongMessageInternal>(ServiceMessage, { it.safeCast() })
}

// internal runtime value, not serializable
@Suppress("RegExpRedundantEscape", "UnnecessaryVariable")
internal data class ForwardMessageInternal(
    override val content: String,
    val resId: String?,
    /**
     * null means top-level.
     * not null means nested and need [ForwardMessageInternal.MsgTransmits] in [RefineContext]
     */
    val fileName: String?,

    /**
     * For light refine before constructing [MessageReceipt].
     * See [OutgoingMessageSourceInternal] for more details.
     */
    val origin: ForwardMessage? = null,
) : AbstractServiceMessage(),
    RefinableMessage {
    override val serviceId: Int get() = 35

    override fun tryRefine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message {
        return origin ?: this
    }

    override suspend fun refine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message {
        bot.asQQAndroidBot()

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

        val resId = resId?.takeIf { it.isNotEmpty() }

        if (fileName != null) kotlin.run nested@{ // nested
            val transmits = refineContext[MsgTransmits]?.get(fileName)
                ?: return@nested // Refine failed
            return MessageOrigin(
                SimpleServiceMessage(serviceId, content),
                resId,
                MessageOriginKind.FORWARD,
            ) + ForwardMessage(
                preview = preview,
                title = title,
                brief = brief,
                source = source,
                summary = summary.trim(),
                nodeList = getMiraiImpl().run { transmits.toForwardMessageNodes(bot, refineContext) },
            )
        }

        // No id and no fileName
        if (resId == null) {
            return SimpleServiceMessage(serviceId, content)
        }

        return MessageOrigin(
            SimpleServiceMessage(serviceId, content),
            resId,
            MessageOriginKind.FORWARD,
        ) + ForwardMessage(
            preview = preview,
            title = title,
            brief = brief,
            source = source,
            summary = summary.trim(),
            nodeList = Mirai.downloadForwardMessage(bot, resId),
        )
    }

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitForwardMessageInternal(this, data) ?: super<AbstractServiceMessage>.accept(
            visitor,
            data
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

        val MsgTransmits = RefineContextKey<Map<String, MsgTransmit.PbMultiMsgNew>>("MsgTransmit")
    }
}

private fun String.xmlEnc(): String = encodeHtmlEscape()

internal fun RichMessage.Key.forwardMessage(
    resId: String,
    fileName: String,
    forwardMessage: ForwardMessage,
): ForwardMessageInternal = with(forwardMessage) {
    val template = """
        <?xml version="1.0" encoding="utf-8"?>
        <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="${brief.take(30).xmlEnc()}"
             m_resid="$resId" m_fileName="$fileName"
             tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">
            <item layout="1" advertiser_id="0" aid="0">
                <title size="34" maxLines="2" lineSpace="12">${title.take(50).xmlEnc()}</title>
                ${
        when {
            preview.size > 4 -> {
                preview.take(3).joinToString("") {
                    """<title size="26" color="#777777" maxLines="2" lineSpace="12">${it.take(50).xmlEnc()}</title>"""
                } + """<title size="26" color="#777777" maxLines="2" lineSpace="12">...</title>"""
            }
            else -> {
                preview.joinToString("") {
                    """<title size="26" color="#777777" maxLines="2" lineSpace="12">${it.take(50).xmlEnc()}</title>"""
                }
            }
        }
    }
                <hr hidden="false" style="0"/>
                <summary size="26" color="#777777">${summary.take(50).xmlEnc()}</summary>
            </item>
            <source name="${source.take(50).xmlEnc()}" icon="" action="" appid="-1"/>
        </msg>
    """.trimIndent().replace("\n", " ").trim()
    return ForwardMessageInternal(template, resId, null, forwardMessage)
}
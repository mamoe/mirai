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
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.MiraiImpl
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.message.data.*
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
) : AbstractServiceMessage(),
    RefinableMessage {
    override val serviceId: Int get() = 35

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

        if (fileName != null) { // nested
            val transmits = refineContext.getNotNull(MsgTransmits)[fileName]
                ?: return SimpleServiceMessage(serviceId, content) // Refine failed
            return MessageOrigin(
                SimpleServiceMessage(serviceId, content),
                null, // Nested don't have resource id
                MessageOriginKind.FORWARD
            ) + ForwardMessage(
                preview = preview,
                title = title,
                brief = brief,
                source = source,
                summary = summary.trim(),
                nodeList = MiraiImpl.run { transmits.toForwardMessageNodes(bot, refineContext) }
            )
        }

        return MessageOrigin(
            SimpleServiceMessage(serviceId, content),
            resId,
            MessageOriginKind.FORWARD
        ) + ForwardMessage(
            preview = preview,
            title = title,
            brief = brief,
            source = source,
            summary = summary.trim(),
            nodeList = Mirai.downloadForwardMessage(bot, resId!!)
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


internal fun RichMessage.Key.longMessage(brief: String, resId: String, timeSeconds: Long): LongMessageInternal {
    val limited: String = if (brief.length > 30) {
        brief.take(30) + "…"
    } else {
        brief
    }

    val template = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <msg serviceID="35" templateID="1" action="viewMultiMsg"
                     brief="$limited"
                     m_resid="$resId"
                     m_fileName="$timeSeconds" sourceMsgId="0" url=""
                     flag="3" adverSign="0" multiMsgFlag="1">
                    <item layout="1">
                        <title>$limited</title>
                        <hr hidden="false" style="0"/>
                        <summary>点击查看完整消息</summary>
                    </item>
                    <source name="聊天记录" icon="" action="" appid="-1"/>
                </msg>
            """.trimIndent().trim()

    return LongMessageInternal(template, resId)
}


internal fun RichMessage.Key.forwardMessage(
    resId: String,
    timeSeconds: Long,
    forwardMessage: ForwardMessage,
): ForwardMessageInternal = with(forwardMessage) {
    val template = """
        <?xml version="1.0" encoding="utf-8"?>
        <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="${brief.take(30)}"
             m_resid="$resId" m_fileName="$timeSeconds"
             tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">
            <item layout="1" advertiser_id="0" aid="0">
                <title size="34" maxLines="2" lineSpace="12">${title.take(50)}</title>
                ${
        when {
            preview.size > 4 -> {
                preview.take(3).joinToString("") {
                    """<title size="26" color="#777777" maxLines="2" lineSpace="12">$it</title>"""
                } + """<title size="26" color="#777777" maxLines="2" lineSpace="12">...</title>"""
            }
            else -> {
                preview.joinToString("") {
                    """<title size="26" color="#777777" maxLines="2" lineSpace="12">$it</title>"""
                }
            }
        }
    }
                <hr hidden="false" style="0"/>
                <summary size="26" color="#777777">${summary.take(50)}</summary>
            </item>
            <source name="${source.take(50)}" icon="" action="" appid="-1"/>
        </msg>
    """.trimIndent().replace("\n", " ").trim()
    return ForwardMessageInternal(template, resId, null)
}
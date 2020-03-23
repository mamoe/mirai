/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file: JvmName("MessageUtils")
@file:JvmMultifileClass

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * XML 消息等富文本消息
 *
 * @see XmlMessage XML
 * @see JsonMessage JSON
 * @see LightApp 小程序 (JSON)
 */
// not using sealed class for customized implementations
@SinceMirai("0.27.0")
interface RichMessage : MessageContent {

    @SinceMirai("0.30.0")
    companion object Templates : Message.Key<RichMessage> {

        @MiraiExperimentalAPI
        @SinceMirai("0.30.0")
        fun share(url: String, title: String? = null, content: String? = null, coverUrl: String? = null): XmlMessage =
            buildXmlMessage {
                templateId = 12345
                serviceId = 1
                action = "web"
                brief = "[分享] " + (title.orEmpty())
                this.url = url
                item {
                    layout = 2
                    if (coverUrl != null) {
                        picture(coverUrl)
                    }
                    if (title != null) {
                        title(title)
                    }
                    if (content != null) {
                        summary(content)
                    }
                }
            }
    }

    val content: String

    override val length: Int get() = content.length
    override fun get(index: Int): Char = content[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = content.subSequence(startIndex, endIndex)
    override fun compareTo(other: String): Int = content.compareTo(other)
}

/**
 * Json 消息.
 *
 * @see LightApp 一些消息实际上是 [LightApp]
 */
@SinceMirai("0.27.0")
@OptIn(MiraiExperimentalAPI::class)
class JsonMessage(override val content: String) : RichMessage {
    companion object Key : Message.Key<JsonMessage>

    // serviceId = 1
    override fun toString(): String = content
}

/**
 * 小程序, 如音乐分享
 */
@OptIn(MiraiExperimentalAPI::class)
@SinceMirai("0.27.0")
class LightApp constructor(override val content: String) : RichMessage {

    companion object Key : Message.Key<LightApp>

    override fun toString(): String = content
}


/**
 * XML 消息, 如分享, 卡片等.
 *
 * @see buildXmlMessage
 */
@SinceMirai("0.27.0")
@OptIn(MiraiExperimentalAPI::class)
class XmlMessage constructor(override val content: String) : RichMessage {
    companion object Key : Message.Key<XmlMessage>

    // override val serviceId: Int get() = 60

    override fun toString(): String = content
}

/**
 * 构造一条 XML 消息
 */
@JvmSynthetic
@SinceMirai("0.27.0")
@MiraiExperimentalAPI
inline fun buildXmlMessage(block: @XMLDsl XmlMessageBuilder.() -> Unit): XmlMessage =
    XmlMessage(XmlMessageBuilder().apply(block).text)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class XMLDsl

@Suppress("MemberVisibilityCanBePrivate")
@XMLDsl
class XmlMessageBuilder(
    var templateId: Int = 1,
    var serviceId: Int = 1,
    var action: String = "plugin",
    /**
     * 一般为点击这条消息后跳转的链接
     */
    var actionData: String = "",
    /**
     * 摘要, 在官方客户端内消息列表中显示
     */
    var brief: String = "",
    var flag: Int = 3,
    var url: String = "", // TODO: 2019/12/3 unknown
    var sourceName: String = "",
    var sourceIconURL: String = ""
) {
    @PublishedApi
    internal val builder: StringBuilder = StringBuilder()

    val text: String
        get() = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>" +
                "<msg templateID='$templateId' serviceID='$serviceId' action='$action' actionData='$actionData' brief='$brief' flag='$flag' url='$url'>" +
                builder.toString() +
                "<source name='$sourceName' icon='$sourceIconURL'/>" +
                "</msg>"

    @JvmOverloads
    @XMLDsl
    inline fun item(bg: Int = 0, layout: Int = 4, block: @XMLDsl ItemBuilder.() -> Unit) {
        builder.append(ItemBuilder(bg, layout).apply(block).text)
    }

    fun source(name: String, iconURL: String = "") {
        sourceName = name
        sourceIconURL = iconURL
    }

    @SinceMirai("0.27.0")
    @XMLDsl
    class ItemBuilder @PublishedApi internal constructor(
        var bg: Int = 0,
        var layout: Int = 4
    ) {
        @PublishedApi
        internal val builder: StringBuilder = StringBuilder()
        val text: String get() = "<item bg='$bg' layout='$layout'>$builder</item>"

        fun summary(text: String, color: String = "#000000") {
            this.builder.append("<summary color='$color'>$text</summary>")
        }

        fun title(text: String, size: Int = 25, color: String = "#000000") {
            this.builder.append("<title size='$size' color='$color'>$text</title>")
        }

        fun picture(coverUrl: String) {
            this.builder.append("<picture cover='$coverUrl'/>")
        }
    }
}

@Deprecated(
    "for source compatibility",
    replaceWith = ReplaceWith("RichMessage.Templates", "net.mamoe.mirai.message.data.RichMessage"),
    level = DeprecationLevel.ERROR
)
@Suppress("unused")
// in bytecode it's public
internal typealias XmlMessageHelper = RichMessage.Templates
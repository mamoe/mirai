/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * XML 消息, 如分享, 卡片等.
 *
 * @see buildXMLMessage
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
inline fun buildXMLMessage(block: @XMLDsl XMLMessageBuilder.() -> Unit): XmlMessage =
    XmlMessage(XMLMessageBuilder().apply(block).text)

@SinceMirai("0.27.0")
@XMLDsl
class ItemBuilder(
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

@XMLDsl
class XMLMessageBuilder(
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

    @XMLDsl
    fun item(block: @XMLDsl ItemBuilder.() -> Unit) {
        builder.append(ItemBuilder().apply(block).text)
    }

    fun source(name: String, iconURL: String = "") {
        sourceName = name
        sourceIconURL = iconURL
    }
}

@MiraiExperimentalAPI
object XmlMessageHelper {
    fun share(u: String, title: String?, content: String?, image: String?) = buildXMLMessage {
        templateId = 12345
        serviceId = 1
        action = "web"
        brief = "[分享] " + (title ?: "")
        url = u
        item {
            layout = 2
            if (image != null) {
                picture(image)
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

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class XMLDsl

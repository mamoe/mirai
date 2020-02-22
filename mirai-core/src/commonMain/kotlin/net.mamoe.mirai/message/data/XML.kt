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
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * XML 消息, 如分享, 卡片等.
 *
 * @see buildXMLMessage
 */
inline class XMLMessage(val stringValue: String) : Message,
    SingleOnly {
    override fun followedBy(tail: Message): Nothing = error("XMLMessage Message cannot be followed")
    override fun toString(): String = stringValue
}

/**
 * 构造一条 XML 消息
 */
@XMLDsl
@MiraiExperimentalAPI("还未支持")
inline fun buildXMLMessage(block: @XMLDsl XMLMessageBuilder.() -> Unit): XMLMessage =
    XMLMessage(XMLMessageBuilder().apply(block).text)

@Suppress("NOTHING_TO_INLINE")
@XMLDsl
class ItemBuilder(
    var bg: Int = 0,
    var layout: Int = 4
) {
    @PublishedApi
    internal val builder: StringBuilder = StringBuilder()
    val text: String get() = "<item bg='$bg' layout='$layout'>$builder</item>"

    inline fun summary(text: String, color: String = "#FFFFFF") {
        this.builder.append("<summary color='$color'>$text</summary>")
    }

    inline fun title(text: String, size: Int = 18, color: String = "#FFFFFF") {
        this.builder.append("<title size='$size' color='$color'>$text</title>")
    }

    inline fun picture(coverUrl: String) {
        this.builder.append("<picture cover='$coverUrl'/>")
    }
}

@XMLDsl
@Suppress("NOTHING_TO_INLINE")
class XMLMessageBuilder(
    var templateId: Int = 1,
    var serviceId: Int = 1,
    var action: String = "plugin",
    /**
     * 一般为点击这条消息后跳转的链接
     */
    var actionData: String = "",
    /**
     * 摘要
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
    inline fun item(block: @XMLDsl ItemBuilder.() -> Unit) {
        builder.append(ItemBuilder().apply(block).text)
    }

    inline fun source(name: String, iconURL: String = "") {
        sourceName = name
        sourceIconURL = iconURL
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
internal annotation class XMLDsl
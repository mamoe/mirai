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
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
import kotlin.annotation.AnnotationTarget.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * XML 消息等富文本消息
 *
 * @see ServiceMessage 子类 [XmlMessage], [JsonMessage], [LongMessage], [ForwardMessage]
 * @see LightApp 小程序 (JSON)
 */
// not using sealed class for customized implementations
@SinceMirai("0.27.0")
interface RichMessage : MessageContent {

    override fun contentToString(): String = this.content

    @MiraiExperimentalAPI
    @SinceMirai("0.30.0")
    companion object Templates : Message.Key<RichMessage> {

        @MiraiExperimentalAPI
        @SinceMirai("0.30.0")
        fun share(
            url: String,
            title: String? = null,
            content: String? = null,
            coverUrl: String? = null
        ): ServiceMessage =
            buildXmlMessage(60) {
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

        @PlannedRemoval("1.0.0")
        @JvmName("share")
        @Deprecated(
            "for binary compatibility", level = DeprecationLevel.HIDDEN
        )
        @Suppress("DEPRECATION_ERROR")
        @MiraiExperimentalAPI
        fun shareDeprecated(
            url: String,
            title: String? = null,
            content: String? = null,
            coverUrl: String? = null
        ): XmlMessage = share(url, title, content, coverUrl) as XmlMessage

        override val typeName: String
            get() = "RichMessage"
    }

    val content: String
}

/**
 * 小程序, 如音乐分享.
 *
 * @param content 一般是 json
 */
@SinceMirai("0.27.0")
data class LightApp(override val content: String) : RichMessage {
    companion object Key : Message.Key<LightApp> {
        override val typeName: String get() = "LightApp"
    }

    override fun toString(): String = "[mirai:app:$content]"
}

/**
 * 服务消息, 如 [XmlMessage].
 */
@SinceMirai("0.37.3")
open class ServiceMessage(val serviceId: Int, final override val content: String) : RichMessage {
    companion object Key : Message.Key<ServiceMessage> {
        override val typeName: String get() = "ServiceMessage"
    }

    final override fun toString(): String = "[mirai:service:$serviceId,$content]"

    final override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        other as ServiceMessage
        return other.serviceId == this.serviceId && other.content == this.content
    }

    final override fun hashCode(): Int {
        var result = serviceId
        result = 31 * result + content.hashCode()
        return result
    }
}


/**
 * Json 消息.
 *
 * 由于 [serviceId] 不准确, 请使用 [ServiceMessage] 并手动指定 `serviceId`
 *
 * @see LightApp 一些 json 消息实际上是 [LightApp]
 */
@PlannedRemoval("1.0.0")
@Deprecated("use ServiceMessage with serviceId 1",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("ServiceMessage"))
@Suppress("DEPRECATION_ERROR")
@MiraiExperimentalAPI
class JsonMessage
@Deprecated("use ServiceMessage with serviceId 1",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("ServiceMessage(1, content)"))
constructor(content: String) : ServiceMessage(1, content) {
    @Suppress("DEPRECATION")
    companion object Key : Message.Key<JsonMessage> {
        override val typeName: String get() = "JsonMessage"
    }
}


/**
 * XML 消息, 如分享, 卡片等.
 *
 * 由于 [serviceId] 不准确, 请使用 [ServiceMessage] 并手动指定 `serviceId`
 *
 * @param serviceId 目前未知, 一般为 60
 *
 * @see buildXmlMessage 使用 DSL 构造一个 XML 消息
 */
@PlannedRemoval("1.0.0")
@Deprecated("use ServiceMessage with serviceId 1",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("ServiceMessage"))
@MiraiExperimentalAPI
@Suppress("DEPRECATION_ERROR")
@SinceMirai("0.27.0")
class XmlMessage @MiraiExperimentalAPI("Maybe replaced with an enum")
constructor(serviceId: Int = 60, content: String) : ServiceMessage(serviceId, content) {

    @MiraiExperimentalAPI
    @Deprecated("specify serviceId explicitly", replaceWith = ReplaceWith("XmlMessage(60, content)"))
    constructor(content: String) : this(60, content)

    companion object Key : Message.Key<XmlMessage> {
        override val typeName: String get() = "XmlMessage"
    }
}

/*
commonElem=CommonElem#750141174 {
        businessType=0x00000001(1)
        pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
        serviceType=0x00000002(2)
}
 */

/**
 * 构造一条 XML 消息
 */
@Suppress("DEPRECATION_ERROR")
@JvmSynthetic
@SinceMirai("0.27.0")
@MiraiExperimentalAPI
inline fun buildXmlMessage(serviceId: Int, block: @XmlMessageDsl XmlMessageBuilder.() -> Unit): ServiceMessage =
    XmlMessage(serviceId, XmlMessageBuilder().apply(block).text)

@Target(CLASS, FUNCTION, TYPE)
@DslMarker
annotation class XmlMessageDsl

@MiraiExperimentalAPI
@XmlMessageDsl
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
    @XmlMessageDsl
    inline fun item(bg: Int = 0, layout: Int = 4, block: @XmlMessageDsl ItemBuilder.() -> Unit) {
        builder.append(ItemBuilder(bg, layout).apply(block).text)
    }

    fun source(name: String, iconURL: String = "") {
        sourceName = name
        sourceIconURL = iconURL
    }

    @SinceMirai("0.27.0")
    @XmlMessageDsl
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


@JvmSynthetic
@SinceMirai("0.27.0")
@MiraiExperimentalAPI
@Deprecated("specify serviceId explicitly", ReplaceWith("buildXmlMessage(60, block)"))
inline fun buildXmlMessage(block: @XmlMessageDsl XmlMessageBuilder.() -> Unit): ServiceMessage =
    buildXmlMessage(60, block)


@SinceMirai("0.31.0")
@MiraiExperimentalAPI
internal class LongMessage internal constructor(content: String, val resId: String) : ServiceMessage(35, content) {
    companion object Key : Message.Key<LongMessage> {
        override val typeName: String get() = "LongMessage"
    }
}

@OptIn(MiraiExperimentalAPI::class)
@SinceMirai("0.39.0")
internal class ForwardMessageInternal(content: String) : ServiceMessage(35, content)
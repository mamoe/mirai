/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("MessageUtils")
@file:JvmMultifileClass
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.annotation.AnnotationTarget.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * XML, JSON 消息等富文本消息
 *
 * **注意**: 富文本消息的 [RichMessage.contentEquals] 和 [RichMessage.toString] 都不稳定. 将来可能在没有任何警告的情况下改变格式.
 *
 * @see ServiceMessage 服务消息 (XML, JSON)
 * @see LightApp 小程序 (JSON)
 */
// not using sealed class for customized implementations
interface RichMessage : MessageContent {

    /**
     * **注意**: 富文本消息的 [RichMessage.contentEquals] 和 [RichMessage.toString] 都不稳定. 将来可能在没有任何警告的情况下改变格式.
     */
    @MiraiExperimentalAPI
    override fun contentToString(): String = this.content

    /**
     * 消息内容. 可为 JSON 文本或 XML 文本
     */
    val content: String

    /**
     * 一些模板
     * @suppress 此 API 不稳定, 可能在任意时刻被删除
     */
    @MiraiExperimentalAPI
    companion object Templates : Message.Key<RichMessage> {

        /**
         * @suppress 此 API 不稳定, 可能在任意时刻被删除
         */
        @MiraiExperimentalAPI
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

        override val typeName: String
            get() = "RichMessage"
    }
}

/**
 * 小程序, 如音乐分享.
 *
 * 大部分 JSON 消息为此类型, 另外一部分为 [ServiceMessage]
 *
 * @param content 一般是 json
 *
 * @see ServiceMessage 服务消息
 */
data class LightApp(override val content: String) : RichMessage {
    companion object Key : Message.Key<LightApp> {
        override val typeName: String get() = "LightApp"
    }

    override fun toString(): String = "[mirai:app:$content]"
}

/**
 * 服务消息, 可以是 JSON 消息或 XML 消息.
 *
 * JSON 消息更多情况下通过 [LightApp] 发送.
 *
 * @param serviceId 目前未知, XML 一般为 60, JSON 一般为 1
 * @param content 消息内容. 可为 JSON 文本或 XML 文本
 *
 * @see LightApp 小程序类型消息
 */
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


/*
commonElem=CommonElem#750141174 {
        businessType=0x00000001(1)
        pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
        serviceType=0x00000002(2)
}
 */

/**
 * 构造一条 XML 消息
 * @suppress 此 API 不稳定
 */
@Suppress("DEPRECATION_ERROR")
@JvmSynthetic
@MiraiExperimentalAPI
inline fun buildXmlMessage(serviceId: Int, block: @XmlMessageDsl XmlMessageBuilder.() -> Unit): ServiceMessage =
    ServiceMessage(serviceId, XmlMessageBuilder().apply(block).text)

@MiraiExperimentalAPI
@Target(CLASS, FUNCTION, TYPE)
@DslMarker
annotation class XmlMessageDsl

/**
 * @suppress 此 API 不稳定
 */
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
@MiraiExperimentalAPI
internal class LongMessage internal constructor(content: String, val resId: String) : ServiceMessage(35, content) {
    companion object Key : Message.Key<LongMessage> {
        override val typeName: String get() = "LongMessage"
    }
}


internal class ForwardMessageInternal(content: String) : ServiceMessage(35, content)
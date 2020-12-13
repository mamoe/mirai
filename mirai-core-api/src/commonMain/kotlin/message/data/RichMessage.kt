/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("MessageUtils")
@file:JvmMultifileClass
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendAsMiraiCode
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast
import kotlin.annotation.AnnotationTarget.*

/**
 * XML, JSON 消息等富文本消息
 *
 * **注意**: 富文本消息的 [RichMessage.contentEquals] 和 [RichMessage.toString] 都不稳定. 将来可能在没有任何警告的情况下改变格式.
 *
 * @see ServiceMessage 服务消息 (XML, JSON)
 * @see LightApp 小程序 (JSON)
 */
// not using sealed class for customized implementations
// using polymorphic serializer from Message.Serializer
public interface RichMessage : MessageContent, ConstrainSingle {
    override val key: MessageKey<*> get() = Key

    /**
     * **注意**: 富文本消息的 [RichMessage.contentEquals] 和 [RichMessage.toString] 都不稳定. 将来可能在没有任何警告的情况下改变格式.
     */
    @MiraiExperimentalApi
    public override fun contentToString(): String = this.content

    /**
     * 消息内容. 可为 JSON 文本或 XML 文本
     */
    public val content: String

    /**
     * 一些模板
     * @suppress 此 API 不稳定, 可能在任意时刻被删除
     */
    @MiraiExperimentalApi
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, RichMessage>(MessageContent, { it.safeCast() }) {

        /**
         * @suppress 此 API 不稳定, 可能在任意时刻被删除
         */
        @MiraiExperimentalApi
        @JvmStatic
        public fun share(
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
@Serializable
public data class LightApp(override val content: String) : RichMessage, CodableMessage {
    public companion object Key : AbstractMessageKey<LightApp>({ it.safeCast() })

    public override fun toString(): String = "[mirai:app:$content]"

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.append("[mirai:app:").appendAsMiraiCode(content).append(']')
    }
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
@MiraiExperimentalApi
@Serializable
public class SimpleServiceMessage(
    public override val serviceId: Int,
    public override val content: String
) : ServiceMessage {
    public override fun toString(): String = "[mirai:service:$serviceId,$content]"

    public override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        other as ServiceMessage
        return other.serviceId == this.serviceId && other.content == this.content
    }

    public override fun hashCode(): Int {
        var result = serviceId
        result = 31 * result + content.hashCode()
        return result
    }

}


/**
 * 服务消息, 可以是 JSON 消息或 XML 消息.
 *
 * JSON 消息更多情况下通过 [LightApp] 发送.
 *
 * @see LightApp 小程序类型消息
 * @see SimpleServiceMessage
 */
public interface ServiceMessage : RichMessage, CodableMessage {
    public companion object Key :
        AbstractPolymorphicMessageKey<RichMessage, ServiceMessage>(RichMessage, { it.safeCast() })

    /**
     * 目前未知, XML 一般为 60, JSON 一般为 1
     */
    public val serviceId: Int

    override fun appendMiraiCode(builder: StringBuilder) {
        builder.append("[mirai:service:").append(serviceId).append(',').appendAsMiraiCode(content).append(']')
    }
}

@MiraiExperimentalApi
@Serializable
public abstract class AbstractServiceMessage : ServiceMessage {
    public override fun toString(): String = "[mirai:service:$serviceId,$content]"
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
@MiraiExperimentalApi
public inline fun buildXmlMessage(serviceId: Int, block: @XmlMessageDsl XmlMessageBuilder.() -> Unit): ServiceMessage =
    SimpleServiceMessage(serviceId, XmlMessageBuilder().apply(block).text)

@MiraiExperimentalApi
@Target(CLASS, FUNCTION, TYPE)
@DslMarker
public annotation class XmlMessageDsl

/**
 * @suppress 此 API 不稳定
 */
@MiraiExperimentalApi
@XmlMessageDsl
public class XmlMessageBuilder(
    public var templateId: Int = 1,
    public var serviceId: Int = 1,
    public var action: String = "plugin",
    /**
     * 一般为点击这条消息后跳转的链接
     */
    public var actionData: String = "",
    /**
     * 摘要, 在官方客户端内消息列表中显示
     */
    public var brief: String = "",
    public var flag: Int = 3,
    public var url: String = "", // TODO: 2019/12/3 unknown
    public var sourceName: String = "",
    public var sourceIconURL: String = ""
) {
    @PublishedApi
    internal val builder: StringBuilder = StringBuilder()

    public val text: String
        get() = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>" +
                "<msg templateID='$templateId' serviceID='$serviceId' action='$action' actionData='$actionData' brief='$brief' flag='$flag' url='$url'>" +
                builder.toString() +
                "<source name='$sourceName' icon='$sourceIconURL'/>" +
                "</msg>"

    @JvmOverloads
    @XmlMessageDsl
    public inline fun item(bg: Int = 0, layout: Int = 4, block: @XmlMessageDsl ItemBuilder.() -> Unit) {
        builder.append(ItemBuilder(bg, layout).apply(block).text)
    }

    public fun source(name: String, iconURL: String = "") {
        sourceName = name
        sourceIconURL = iconURL
    }

    @XmlMessageDsl
    public class ItemBuilder @PublishedApi internal constructor(
        public var bg: Int = 0,
        public var layout: Int = 4
    ) {
        @PublishedApi
        internal val builder: StringBuilder = StringBuilder()
        public val text: String get() = "<item bg='$bg' layout='$layout'>$builder</item>"

        public fun summary(text: String, color: String = "#000000") {
            this.builder.append("<summary color='$color'>$text</summary>")
        }

        public fun title(text: String, size: Int = 25, color: String = "#000000") {
            this.builder.append("<title size='$size' color='$color'>$text</title>")
        }

        public fun picture(coverUrl: String) {
            this.builder.append("<picture cover='$coverUrl'/>")
        }
    }
}

@Serializable
@MiraiExperimentalApi
internal data class LongMessage internal constructor(override val content: String, val resId: String) :
    AbstractServiceMessage() {
    override val serviceId: Int get() = 35

    companion object Key : AbstractPolymorphicMessageKey<ServiceMessage, LongMessage>(ServiceMessage, { it.safeCast() })
}

@Serializable
internal data class ForwardMessageInternal(override val content: String) : AbstractServiceMessage() {
    override val serviceId: Int get() = 35
}
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.isSameClass
import net.mamoe.mirai.utils.safeCast

/**
 * 标识来源 [RichMessage], 存在于接收的 [MessageChain] 中. 在发送消息时会被忽略.
 *
 * 一些 [RichMessage] 会被 mirai 解析成特定的更易使用的类型, 如:
 * - 长消息会被协议内部转化为 [ServiceMessage] `serviceId=35` 通过独立通道上传和下载并获得一个 [resourceId]. mirai 会自动下载长消息并把他们解析为 [MessageChain].
 * - 合并转发也使用长消息通道传输, 拥有 [resourceId], mirai 解析为 [ForwardMessage]
 * - [MusicShare] 也有特殊通道上传, 但会作为 [LightApp] 接收.
 *
 * 这些经过转换的类型的来源 [RichMessage] 会被包装为 [MessageOrigin] 并加入消息链中.
 *
 * 如一条被 mirai 解析的长消息的消息链组成为, 第一个元素为 [MessageSource], 第二个元素为 [MessageOrigin], 随后为长消息内容.
 *
 * 又如一条被 mirai 解析的 [MusicShare] 的消息链组成为, 第一个元素为 [MessageSource], 第二个元素为 [MessageOrigin], 第三个元素为 [MusicShare].
 *
 * @suppress **注意**: 这是实验性 API: 可能会在未来任意时刻变更.
 *
 * @since 2.6
 */
@Serializable
@SerialName(MessageOrigin.SERIAL_NAME)
@MiraiExperimentalApi
public class MessageOrigin(
    // [2.3, 2.6-M1) 类名为 RichMessageOrigin
    /**
     * 原 [SingleMessage].
     */
    public val origin: @Polymorphic SingleMessage,
    /**
     * 如果来自长消息或转发消息, 则会有 [resourceId], 否则为 `null`.
     *
     * - 下载长消息 [IMirai.downloadLongMessage]
     * - 下载合并转发消息 [IMirai.downloadForwardMessage]
     */
    public val resourceId: String?,
    /**
     * 来源类型
     */
    public val kind: MessageOriginKind,
) : MessageMetadata, ConstrainSingle {
    override val key: Key get() = Key

    override fun toString(): String {
        val resourceId = resourceId
        return if (resourceId == null) "[mirai:origin:$kind]"
        else "[mirai:origin:$kind,$resourceId]"
    }

    override fun contentToString(): String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageOrigin || !isSameClass(this, other)) return false

        if (origin != other.origin) return false
        if (resourceId != other.resourceId) return false
        if (kind != other.kind) return false

        return true
    }

    override fun hashCode(): Int {
        var result = origin.hashCode()
        result = 31 * result + (resourceId?.hashCode() ?: 0)
        result = 31 * result + kind.hashCode()
        return result
    }

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R = visitor.visitMessageOrigin(this, data)

    public companion object Key : AbstractMessageKey<MessageOrigin>({ it.safeCast() }) {
        public const val SERIAL_NAME: String = "MessageOrigin"
    }
}

/**
 * [MessageOrigin] 来源
 * @see MessageOrigin.kind
 * @since 2.6
 */
@Serializable
public enum class MessageOriginKind { // [2.3, 2.6-M1) 类名为 RichMessageKind
    /**
     * 长消息
     */
    LONG,

    /**
     * 合并转发
     * @see ForwardMessage
     */
    FORWARD,

    /**
     * 音乐分享
     * @see MusicShare
     */
    MUSIC_SHARE,
}
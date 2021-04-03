/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("unused")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.utils.safeCast


/**
 * 兼容 2.6 以下的 [MessageOrigin]. 请使用 [MessageOrigin]
 * @since 2.3
 * @suppress Deprecated since 2.6
 */
@Suppress("DEPRECATION_ERROR")
@Serializable
@SerialName(RichMessageOrigin.SERIAL_NAME)
@Deprecated(
    "Use MessageOrigin instead.",
    replaceWith = ReplaceWith(
        "MessageOrigin",
        "net.mamoe.mirai.message.data.MessageOrigin",
    ),
    level = DeprecationLevel.ERROR
)
public class RichMessageOrigin
@Deprecated(
    "Use MessageOrigin instead.",
    replaceWith = ReplaceWith(
        "MessageOrigin(origin, resourceId, kind)",
        "net.mamoe.mirai.message.data.MessageOrigin",
    ),
    level = DeprecationLevel.ERROR
)
constructor(
    /**
     * 原 [RichMessage].
     */
    public val origin: @Polymorphic RichMessage,
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
    @Suppress("DEPRECATION_ERROR")
    public val kind: RichMessageKind,
) : MessageMetadata, ConstrainSingle {
    @Suppress("DEPRECATION_ERROR")
    override val key: Key get() = Key

    override fun toString(): String {
        val resourceId = resourceId
        return if (resourceId == null) "[mirai:origin:$kind]"
        else "[mirai:origin:$kind,$resourceId]"
    }

    override fun contentToString(): String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        @Suppress("DEPRECATION_ERROR")
        other as RichMessageOrigin

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


    @Deprecated(
        "Use MessageOrigin instead.",
        replaceWith = ReplaceWith(
            "MessageOrigin",
            "net.mamoe.mirai.message.data.MessageOrigin",
        ),
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    public companion object Key : AbstractMessageKey<RichMessageOrigin>({ it.safeCast() }) {
        public const val SERIAL_NAME: String = "RichMessageOrigin"
    }
}

/**
 * 消息来源
 * @since 2.3
 * @suppress Deprecated since 2.6
 */
@Deprecated(
    "Use MessageOriginKind",
    ReplaceWith("MessageOriginKind", "net.mamoe.mirai.message.data.MessageOriginKind"),
    level = DeprecationLevel.ERROR
)
public enum class RichMessageKind {
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
     * @since 2.4
     */
    MUSIC_SHARE,
}
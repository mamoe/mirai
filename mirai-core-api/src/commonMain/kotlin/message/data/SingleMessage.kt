/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE",
    "NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE",
    "INAPPLICABLE_JVM_NAME"
)
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import net.mamoe.mirai.utils.safeCast

/**
 * 单个消息元素. 与之相对的是 [MessageChain], 是多个 [SingleMessage] 的集合.
 */
// @Serializable(SingleMessage.Serializer::class)
public interface SingleMessage : Message {

    /**
     * @suppress deprecated since 2.4.0
     */
    @Deprecated(
        "Please create PolymorphicSerializer(SingleMessage::class) on your own.",
        ReplaceWith(
            "PolymorphicSerializer(SingleMessage::class)",
            "kotlinx.serialization.PolymorphicSerializer",
            "net.mamoe.mirai.message.data.SingleMessage",
        ),
        level = DeprecationLevel.WARNING
    )
    public object Serializer : KSerializer<SingleMessage> by PolymorphicSerializer(SingleMessage::class)
}


/**
 * 消息元数据, 即不含内容的元素.
 *
 * 这种类型的 [Message] 只表示一条消息的属性. 其子类如 [MessageSource], [QuoteReply], [MessageOrigin] 和 [CustomMessageMetadata]
 *
 * 所有子类的 [contentToString] 都应该返回空字符串.
 *
 * 要获取详细信息, 查看 [MessageChain].
 *
 * @see MessageSource 消息源
 * @see QuoteReply 引用回复
 * @see CustomMessageMetadata 自定义元数据
 * @see ShowImageFlag 秀图标识
 *
 * @see ConstrainSingle 约束一个 [MessageChain] 中只存在这一种类型的元素
 */
public interface MessageMetadata : SingleMessage {
    /**
     * 返回空字符串
     */
    override fun contentToString(): String = ""
}

/**
 * 带内容的消息.
 *
 * @see PlainText 纯文本
 * @see At At 一个群成员.
 * @see AtAll At 全体成员
 * @see HummerMessage 一些特殊消息: [戳一戳][PokeMessage], [闪照][FlashImage]
 * @see Image 图片
 * @see RichMessage 富文本
 * @see ServiceMessage 服务消息, 如 JSON/XML
 * @see Face 原生表情
 * @see ForwardMessage 合并转发
 * @see Voice 语音
 * @see MarketFace 商城表情
 * @see MusicShare 音乐分享
 */
public interface MessageContent : SingleMessage {
    public companion object Key : AbstractMessageKey<MessageContent>({ it.safeCast() })
}



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
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "INVISIBLE_MEMBER")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
import net.mamoe.mirai.utils.asSequence
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 由一条消息中的 [QuoteReply] 得到的 [MessageSource].
 * 此消息源可能来自一条与机器人无关的消息. 因此无法提供对象化的 `sender` 或 `target` 获取.
 */
@SinceMirai("0.33.0")
abstract class OfflineMessageSource : MessageSource() {
    companion object Key : Message.Key<OfflineMessageSource> {
        override val typeName: String
            get() = "OfflineMessageSource"
    }

    enum class Kind {
        GROUP,
        FRIEND,

        @SinceMirai("0.36.0")
        TEMP
    }

    /**
     * 消息种类
     */
    abstract val kind: Kind

    // final override fun toString(): String = "OfflineMessageSource(sender=$senderId, target=$targetId)"
}


/**
 * 复制这个消息源, 并修改
 */
@JvmName("copySource")
inline fun MessageSource.copyAmend(
    block: MessageSourceBuilder.() -> Unit
): OfflineMessageSource {
    return constructMessageSource()
}

@MiraiExperimentalAPI
@SinceMirai("0.39.0")
@OptIn(MiraiInternalAPI::class)
fun constructMessageSource(
    kind: OfflineMessageSource.Kind,
    fromUin: Long, targetUin: Long,
    id: Int, time: Int, internalId: Int,
    originalMessage: MessageChain
): OfflineMessageSource {
    val bot = BotImpl.instances.asSequence().mapNotNull { it.get() }.firstOrNull()
        ?: error("no Bot instance available")

    return bot.constructMessageSource(kind, fromUin, targetUin, id, time, internalId, originalMessage)
}

@JvmSynthetic
@MiraiExperimentalAPI
inline fun buildMessageSource(block: MessageSourceBuilder.() -> Unit): MessageSource {
    val builder = MessageSourceBuilder().apply(block)
    return constructMessageSource(
        builder.kind ?: error("found "),
        block
    )
}

@DslMarker
annotation class SourceBuilderDsl

class MessageSourceBuilder(
    source: OfflineMessageSource
) : MessageChainBuilder() {
    var kind: OfflineMessageSource.Kind = source.kind
    var fromUin: Long = source.fromId
    var targetUin: Long = source.targetId
    var id: Int = source.id
    var time: Int = source.time
    var internalId: Int = source.internalId
    var originalMessage: MessageChain = source.originalMessage

    fun from(sender: Contact): MessageSourceBuilder {
        fromUin = if (sender is Group) {
            Group.calculateGroupUinByGroupCode(sender.id)
        } else sender.id
        return this
    }

    fun target(target: Contact): MessageSourceBuilder {
        targetUin = if (target is Group) {
            Group.calculateGroupUinByGroupCode(target.id)
        } else target.id
        return this
    }

    fun
}
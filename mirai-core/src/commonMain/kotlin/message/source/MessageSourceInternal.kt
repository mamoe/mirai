/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message.source

import kotlinx.serialization.Transient
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.message.LightMessageRefiner.dropMiraiInternalFlags
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.visitor.ex
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.cast


/**
 * All [MessageSource] should implement this interface.
 */
internal interface MessageSourceInternal : MessageMetadata {
    @Transient
    val sequenceIds: IntArray // ids

    @Transient
    val internalIds: IntArray // randomId

    @Deprecated("don't use this internally. Use sequenceId or random instead.", level = DeprecationLevel.ERROR)
    @Transient
    val ids: IntArray

    @Transient
    val isRecalledOrPlanned: Boolean
    fun setRecalled(): Boolean // CAS

    fun toJceData(): ImMsgBody.SourceMsg

    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.ex()?.visitMessageSourceInternal(this.cast(), data) ?: super.accept(visitor, data)
    }
}

/**
 * All [OnlineMessageSource.Outgoing] should implement this interface.
 */
internal interface OutgoingMessageSourceInternal : MessageSourceInternal {

    // #1371:
    // 问题是 `build` 得到的 `ForwardMessage` 会在 `transformSpecialMessages`
    // 时上传并变成 `ForwardMessageInternal` 再传递给 factory 发送, 并以这个 internal 结果构造了 receipt.

    // 于是构造 receipt 后会进行 light refine 并更新这个属性.

    /**
     * This 'overrides' [MessageSource.originalMessage].
     */
    var originalMessage: MessageChain

    /**
     * This for patch outgoing message source to real time (from server)
     */
    var time: Int
}

/**
 * All [OnlineMessageSource.Incoming] should implement this interface.
 *
 */
internal interface IncomingMessageSourceInternal : MessageSourceInternal {
    // #1532, #1289
    // 问题描述: 解析 Incoming 时存在中间元素 (如 ForwardMessageInternal) 等,
    // MessageChain.source.originMessage 中可能因为各种原因而存在这些中间元素

    // 于是在广播 MessageEvent 前将 originalMessage 改成 refined 后的 MessageChain

    var originalMessageLazy: Lazy<MessageChain>
}

@Suppress("DEPRECATION_ERROR")
internal fun <C : Contact> OnlineMessageSource.Outgoing.createMessageReceipt(
    target: C,
    doLightRefine: Boolean,
): MessageReceipt<C> {
    if (doLightRefine) {
        check(this is OutgoingMessageSourceInternal) { "Internal error: source !is OutgoingMessageSourceInternal" }
        this.originalMessage = this.originalMessage
            .dropMiraiInternalFlags()
            .refineLight(bot)
    }
    return MessageReceipt(this, target)
}

@Suppress("RedundantSuspendModifier", "unused")
internal suspend fun MessageSource.ensureSequenceIdAvailable() {
    if (this is OnlineMessageSourceToGroupImpl) {
        ensureSequenceIdAvailable()
    }
}

@Suppress("RedundantSuspendModifier", "unused")
internal suspend inline fun Message.ensureSequenceIdAvailable() {
    (this as? MessageChain)?.sourceOrNull?.ensureSequenceIdAvailable()
}

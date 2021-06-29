/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.serialization.Transient
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.contact.SendMessageHandler
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import java.util.concurrent.atomic.AtomicBoolean


/**
 * All [MessageSource] should implement this interface.
 */
internal interface MessageSourceInternal {
    @Transient
    val sequenceIds: IntArray // ids

    @Transient
    val internalIds: IntArray // randomId

    @Deprecated("don't use this internally. Use sequenceId or random instead.", level = DeprecationLevel.ERROR)
    @Transient
    val ids: IntArray

    @Transient
    val isRecalledOrPlanned: AtomicBoolean

    fun toJceData(): ImMsgBody.SourceMsg
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
     *
     * @see SendMessageHandler.sendMessagePacket
     */
    var originalMessage: MessageChain
}

@Suppress("DEPRECATION_ERROR")
internal fun <C : Contact> OnlineMessageSource.Outgoing.createMessageReceipt(
    target: C,
    doLightRefine: Boolean,
): MessageReceipt<C> {
    if (doLightRefine) {
        check(this is OutgoingMessageSourceInternal) { "Internal error: source !is OutgoingMessageSourceInternal" }
        this.originalMessage = this.originalMessage.refineLight(bot)
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

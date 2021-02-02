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
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.sourceOrNull
import java.util.concurrent.atomic.AtomicBoolean


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

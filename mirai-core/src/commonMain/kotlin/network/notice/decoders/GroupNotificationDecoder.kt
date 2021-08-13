/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.decoders

import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.protocol.data.proto.TroopTips0x857
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.internal.utils.io.serialization.loadAs

internal class GroupNotificationDecoder : MixedNoticeProcessor() {
    override suspend fun PipelineContext.processImpl(data: MsgType0x2DC) {
        when (data.kind) {
            0x10 -> {
                val proto = data.buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), offset = 1)
                processAlso(DecodedNotifyMsgBody(data.kind, data.group, proto))
            }
        }
    }
}

internal data class DecodedNotifyMsgBody(
    override val kind: Int,
    override val group: GroupImpl,
    override val buf: TroopTips0x857.NotifyMsgBody,
) : BaseMsgType0x2DC<TroopTips0x857.NotifyMsgBody>, ProtocolStruct
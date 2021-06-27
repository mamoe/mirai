/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.decoders

import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210

internal class MsgType0x210Decoder : SimpleNoticeProcessor<MsgType0x210>(type()) {
    override suspend fun PipelineContext.processImpl(data: MsgType0x210) {
        when (data.uSubMsgType) {
            0x8AL -> {
            }
            else -> {
            }
        }
    }
}
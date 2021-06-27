/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.utils.read

internal class BinaryMessageProcessor : SimpleNoticeProcessor<PbMsgInfo>(type()), NewContactSupport {
    override suspend fun PipelineContext.processImpl(data: PbMsgInfo) {
        data.msgData.read<Unit> {
            when (data.msgType) {
                44 -> {
                    TODO("removed")
                }
                34 -> {
                    TODO("removed")
                }
                else -> {
                    when {
                        data.msgType == 529 && data.msgSubtype == 9 -> {
                            TODO("removed")
                        }
                    }
                    throw contextualBugReportException(
                        "解析 OnlinePush.PbPushTransMsg, msgType=${data.msgType}",
                        data._miraiContentToString(),
                        null,
                        "并描述此时机器人是否被踢出, 或是否有成员列表变更等动作."
                    )
                }
            }
        }
    }
}
/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp
import net.mamoe.mirai.utils.warning
import net.mamoe.mirai.utils.withSwitch

internal class TraceLoggingNoticeProcessor(
    logger: MiraiLogger
) : SimpleNoticeProcessor<ProtocolStruct>(type()) {
    private val logger: MiraiLogger = logger.withSwitch(systemProp("mirai.network.notice.trace.logging", false))

    override suspend fun NoticePipelineContext.processImpl(data: ProtocolStruct) {
        logger.warning { "${data::class.simpleName}: isConsumed=$isConsumed" }
    }

//    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x210) {
//        logger.warning { "MsgType0x210: isConsumed=$isConsumed" }
//    }
//
}
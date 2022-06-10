/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.message.protocol

import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.desensitizer.Desensitizer.Companion.generateAndDesensitize
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug

internal class MessageDecodingRecorder(
    private val logger: MiraiLogger = MiraiLogger.Factory.create(MessageDecodingRecorder::class)
) : MessageDecoder {
    override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
        logger.debug {
            "\n" + ValueDescAnalyzer.generateAndDesensitize(data)
        }
    }
}
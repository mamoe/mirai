/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.notice

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.desensitizer.Desensitizer.Companion.generateAndDesensitize
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info


/**
 * ### How to use recorder?
 *
 * 0. Configure desensitization. See mirai-core/src/commonTest/recording/configs/desensitization.yml
 * 1. Inject the recorder as follows:
 *
 * ```
 * bot.components[NoticeProcessorPipeline].registerProcessor(recorder)
 * ```
 *
 * 2. Do something
 * 3. Recorded values are shown in logs. Check 'decoded' to ensure that all sensitive values are replaced.
 */
internal class RecordingNoticeProcessor : SimpleNoticeProcessor<ProtocolStruct>(type()) {
    private val id = atomic(0)
    private val lock = Mutex()

    override suspend fun NoticePipelineContext.processImpl(data: ProtocolStruct) {
        lock.withLock {
            id.getAndDecrement()
            logger.info { "Recorded #${id.value} ${data::class.simpleName}" }
            logger.info { "Desensitized: \n\n\u001B[0m" + ValueDescAnalyzer.generateAndDesensitize(data) + "\n\n" }
        }
    }
}

private val logger: MiraiLogger by lazy { MiraiLogger.Factory.create(RecordingNoticeProcessor::class) }
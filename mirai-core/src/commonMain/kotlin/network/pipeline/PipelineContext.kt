/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.pipeline

import net.mamoe.mirai.utils.ExceptionCollector
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TypeSafeMap

internal interface PipelineContext {
    /**
     * Stores the result after finishing each Phase. `null` for not finished.
     */
    var executionResult: Result<Any?>

    val logger: MiraiLogger
    val attributes: TypeSafeMap
    val exceptionCollector: ExceptionCollector
}

internal abstract class AbstractPipelineContext(
    override val attributes: TypeSafeMap,
) : PipelineContext {
    override val logger: MiraiLogger by lazy { MiraiLogger.Factory.create(this::class) }
    override val exceptionCollector: ExceptionCollector = ExceptionCollector()
    override var executionResult: Result<Any?> =
        Result.failure(object : IllegalStateException("executionResult is not yet initialized") {
            override fun fillInStackTrace(): Throwable = this
        })
}

internal inline val <T : PipelineContext> T.context get() = this

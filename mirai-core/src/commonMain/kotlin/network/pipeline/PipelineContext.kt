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
import net.mamoe.mirai.utils.MutableTypeSafeMap
import net.mamoe.mirai.utils.TypeKey

internal interface PipelineContext {
    val logger: MiraiLogger
    val attributes: MutableTypeSafeMap
    val exceptionCollector: ExceptionCollector

    companion object {
        /**
         * For final phases
         */
        @JvmField
        val KEY_EXECUTION_RESULT = TypeKey<Result<Any?>>("executionResult")
    }
}

internal inline val <T : PipelineContext> T.context get() = this

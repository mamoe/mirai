/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.pipeline

import net.mamoe.mirai.internal.network.pipeline.Node.*
import net.mamoe.mirai.utils.recoverCatchingSuppressed

/**
 * @see Finish
 * @see Finally
 * @see SavePoint
 * @see JumpToSavepointOnFailure
 * @see Phase
 */
internal sealed interface Node<in C : PipelineContext, in In, out Out> {
    val name: String

    class Finish<FinalOut>(override val name: String = "Finish") : Node<PipelineContext, FinalOut, FinalOut>
    abstract class Finally<C : PipelineContext>(override val name: String) : Node<C, Any?, Nothing> {
        abstract suspend fun C.doFinally()
    }

    class SavePoint<C : PipelineContext, T>(val id: Any) : Node<C, T, T> {
        override val name: String = "Savepoint $id"
    }

    class JumpToSavepointOnFailure<C : PipelineContext, AIn, AOut>(
        val delegate: Phase<C, AIn, AOut>,
        val targetSavepointId: Any,
    ) : Node<C, AIn, AOut> {
        override val name: String get() = delegate.name
    }
}

/**
 * Runnable [Node]
 */
internal interface Phase<in C : PipelineContext, in In, out Out> : Node<C, In, Out> {
    suspend fun C.doPhase(input: In): Out
}

internal abstract class AbstractPhase<in C : PipelineContext, in In, out Out>(
    override val name: String
) : Phase<C, In, Out>

internal suspend inline fun <C : PipelineContext, In, Out> Phase<C, In, Out>.doPhase(
    context: C,
    input: In
): Out {
    return context.run { doPhase(input) }
}

internal class RecoverablePhase<C : PipelineContext, AIn, AOut>(
    val delegate: Phase<C, AIn, AOut>,
    val onFailure: Array<Phase<C, AIn, AOut>>,
) : AbstractPhase<C, AIn, AOut>(delegate.name) {
    override suspend fun C.doPhase(input: AIn): AOut {
        val context = this

        return onFailure.fold(kotlin.runCatching {
            delegate.doPhase(context, input)
        }) { acc, phase ->
            acc.recoverCatchingSuppressed { phase.doPhase(context, input) }
        }.getOrThrow()
    }
}

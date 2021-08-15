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
internal sealed class Node<in C : PipelineContext, in In, out Out>(
    val name: String
) {
    class Finish<FinalOut>(name: String = "Finish") : Node<PipelineContext, FinalOut, FinalOut>(name)
    abstract class Finally<C : PipelineContext>(name: String) : Node<C, Any?, Nothing>(name) {
        abstract suspend fun C.doFinally()
    }

    internal class SavePoint<C : PipelineContext, T>(val id: Any) : Node<C, T, T>("Savepoint $id")

    internal class JumpToSavepointOnFailure<C : PipelineContext, AIn, AOut>(
        val delegate: Phase<C, AIn, AOut>,
        val targetSavepointId: Any,
    ) : Node<C, AIn, AOut>(delegate.name)
}

/**
 * Runnable [Node]
 */
internal abstract class Phase<in C : PipelineContext, in In, out Out>(
    name: String
) : Node<C, In, Out>(name) {
    abstract suspend fun C.doPhase(input: In): Out
}

internal suspend inline fun <C : PipelineContext, In, Out> Phase<C, In, Out>.doPhase(
    context: C,
    input: In
): Out {
    return context.run { doPhase(input) }
}

internal class RecoverablePhase<C : PipelineContext, AIn, AOut>(
    val delegate: Phase<C, AIn, AOut>,
    val onFailure: Array<Phase<C, AIn, AOut>>,
) : Phase<C, AIn, AOut>(delegate.name) {
    override suspend fun C.doPhase(input: AIn): AOut {
        val context = this

        return onFailure.fold(kotlin.runCatching {
            delegate.doPhase(context, input)
        }) { acc, phase ->
            acc.recoverCatchingSuppressed { phase.doPhase(context, input) }
        }.getOrThrow()
    }
}

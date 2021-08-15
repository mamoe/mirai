/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.pipeline

internal class PhaseConfiguration<C : PipelineContext, AIn, AOut>(
    private val phase: Phase<C, AIn, AOut>
) {
    private val recovers: MutableList<Phase<C, AIn, AOut>> = mutableListOf()

    @PhaseConfigurationDsl
    fun onFailure(phase: Phase<C, AIn, AOut>): Nothing? {
        recovers.add(phase)
        return null
    }

    @PhaseConfigurationDsl
    fun onFailureJumpTo(id: Any): Node.JumpToSavepointOnFailure<C, AIn, AOut> { // savepoint id
        return Node.JumpToSavepointOnFailure(phase, id)
    }

    fun toPhase(): Phase<C, AIn, AOut> = RecoverablePhase(phase, recovers.toTypedArray())
}

@DslMarker
internal annotation class PhaseConfigurationDsl
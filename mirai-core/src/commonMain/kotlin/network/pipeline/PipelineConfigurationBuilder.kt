/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.pipeline

import net.mamoe.mirai.internal.network.message.buildPhaseConfiguration

@DslMarker
internal annotation class CriticalPointMarker

/**
 * @see buildPhaseConfiguration
 */
internal class PipelineConfigurationBuilder<C : PipelineContext, InitialIn, FinalOut>(
    val configuration: PipelineConfiguration<C, InitialIn, FinalOut> = PipelineConfiguration()
) {

    @Suppress("PropertyName")
    @CriticalPointMarker
    val Finish = Node.Finish<FinalOut>()


    @Suppress("FunctionName")
    @CriticalPointMarker
    fun <T> Savepoint(id: Any): Node.SavePoint<C, T> {
        require(configuration.nodes.none { it is Node.SavePoint<*, *> && it.id == id }) {
            "There is already a savepoint with id '$id'."
        }
        Node.SavePoint<C, T>(id).let {
            configuration.addNode(it)
            return it
        }
    }

    infix fun <AIn, AOut, BOut, Next : Node<C, AOut, BOut>> Node<C, AIn, AOut>.then(next: Next): Next {
        configuration.addNode(this)
        return next
    }

    infix fun <FinalOut> Node.Finish<FinalOut>.finally(finally: Node.Finally<C>): Node.Finish<FinalOut> {
        configuration.addNode(finally)
        return this
    }

    @BuilderInference
    inline operator fun <AIn, AOut> Phase<C, AIn, AOut>.invoke(@BuilderInference action: PhaseConfiguration<C, AIn, AOut>.() -> Node<C, AIn, AOut>?): Node<C, AIn, AOut> {
        PhaseConfiguration(this).run {
            action()?.let { return it }
            return toPhase()
        }
    }

    /**
     * Fast path for [PhaseConfiguration.onFailureJumpTo]
     */
    @PhaseConfigurationDsl
    infix fun <AIn, AOut> Phase<C, AIn, AOut>.onFailureJumpTo(id: Any): Node.JumpToSavepointOnFailure<C, AIn, AOut> { // savepoint id
        return Node.JumpToSavepointOnFailure(this, id)
    }


    fun build() = configuration
}

internal inline fun <C : PipelineContext, InitialIn, FinalOut> buildPhaseConfiguration(
    block: PipelineConfigurationBuilder<C, InitialIn, FinalOut>.() -> Node.Finish<FinalOut>,
): PipelineConfiguration<C, InitialIn, FinalOut> =
    PipelineConfigurationBuilder<C, InitialIn, FinalOut>().apply { block() }.build()

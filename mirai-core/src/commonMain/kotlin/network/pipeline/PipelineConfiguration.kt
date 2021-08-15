/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.pipeline

import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.forEachWithIndexer
import net.mamoe.mirai.utils.uncheckedCast

internal class PipelineConfiguration<C : PipelineContext, InitialIn, FinalOut> {
    private val _nodes: ArrayList<Node<C, *, *>> = ArrayList() // must be ordered
    val nodes: List<Node<C, *, *>> get() = _nodes

    fun addNode(node: Node<C, *, *>) {
        _nodes.add(node)
    }

    suspend fun execute(context: C, initialIn: InitialIn): FinalOut {
        var value: Any? = initialIn

        /**
         * Run [Node.Finally]s and throw [e] with [PipelineContext.exceptionCollector].
         */
        suspend fun fail(e: Throwable): Nothing {
            _nodes.forEach { node ->
                if (node is Node.Finally) {
                    node.run { context.doFinally() }
                }
            }
            context.exceptionCollector.collectThrow(e)
        }

        _nodes.forEachWithIndexer { node ->
            context.attributes[PipelineContext.KEY_EXECUTION_RESULT] = Result.success(value)
            when (node) {
                is Phase<*, *, *> -> {
                    try {
                        value = node.cast<Phase<C, Any?, Any?>>().doPhase(context, value)
                    } catch (e: Throwable) {
                        fail(e)
                    }
                }
                is Node.Finish -> return value.uncheckedCast()
                is Node.SavePoint -> {
                    // nothing to do
                }
                is Node.Finally -> node.run { context.doFinally() }
                is Node.JumpToSavepointOnFailure -> {
                    try {
                        node.delegate.cast<Phase<C, Any?, Any?>>().doPhase(context, value)
                    } catch (e: Throwable) {
                        context.exceptionCollector.collect(e)
                        setNextIndex(_nodes.indexOfFirst { it is Node.SavePoint && it.id == node.targetSavepointId })
                    }
                }
            }
        }
        error("There is no finishing phase.")
    }
}
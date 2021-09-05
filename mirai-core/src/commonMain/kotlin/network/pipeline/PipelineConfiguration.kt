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
    val mutableNodes: MutableList<Node<C, *, *>> get() = _nodes

    fun addNode(node: Node<C, *, *>) {
        _nodes.add(node)
    }

    fun addNodeBeforeFinish(node: Node<C, *, *>) {
        val index = _nodes.indexOfFirst { it is Node.Finish } - 1
        _nodes.add(index.coerceAtLeast(0), node)
    }

    suspend fun execute(
        context: C,
        initialIn: InitialIn,
        afterEach: (node: Node<C, *, *>, result: Result<Any?>) -> Unit = { _, _ -> }
    ): FinalOut {
        var value: Any? = initialIn

        /**
         * Run [Node.Finally]s
         */
        suspend fun doAllFinally() {
            _nodes.forEach { node ->
                if (node is Node.Finally) {
                    try {
                        node.run { context.doFinally() }
                    } catch (e: Throwable) {
                        context.exceptionCollector.collectThrow(ExceptionInFinallyPhaseException(node, e))
                    }
                }
            }
        }

        /**
         * Run [Node.Finally]s and throw [e] with [PipelineContext.exceptionCollector].
         */
        suspend fun failAndExit(e: Throwable): Nothing {
            doAllFinally()
            context.exceptionCollector.collectThrow(e)
        }

        _nodes.forEachWithIndexer { node ->
            context.executionResult = Result.success(value)
            when (node) {
                is Phase<*, *, *> -> {
                    val result = kotlin.runCatching {
                        node.cast<Phase<C, Any?, Any?>>().doPhase(context, value)
                    }
                    afterEach(node, result)
                    result.fold(
                        onSuccess = { value = it },
                        onFailure = { failAndExit(it) }
                    )
                }
                is Node.Finish -> {
                    doAllFinally()
                    afterEach(node, Result.success(value))
                    return value.uncheckedCast()
                }
                is Node.SavePoint -> {
                    afterEach(node, Result.success(value))
                    // nothing to do
                }
                is Node.Finally -> {
                    // ignored
                }
                is Node.JumpToSavepointOnFailure -> {
                    val result = kotlin.runCatching {
                        node.delegate.cast<Phase<C, Any?, Any?>>().doPhase(context, value)
                    }
                    afterEach(node.delegate, result)
                    result.fold(
                        onSuccess = { value = it },
                        onFailure = { e ->
                            context.exceptionCollector.collect(e)
                            setNextIndex(_nodes.indexOfFirst { it is Node.SavePoint && it.id == node.targetSavepointId })
                        }
                    )
                }
            }
        }
        error("There is no finishing phase.")
    }
}

internal fun PipelineConfiguration<*, *, *>.validate() {
    if (this.nodes.none() { it is Node.Finally<*> }) error("There is no Finally node in the configuration $this")
}

internal class ExceptionInFinallyPhaseException(
    val phase: Node.Finally<*>,
    override val cause: Throwable?
) : RuntimeException("Exception in finally phase: $phase")
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.pipeline

import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.components.NoticeProcessor
import net.mamoe.mirai.utils.*
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

internal interface Processor<C : ProcessorPipelineContext<D, *>, D> : PipelineConsumptionMarker {
    suspend fun process(context: C, data: D)
}

internal interface ProcessorPipeline<P : Processor<out ProcessorPipelineContext<D, *>, D>, D, R> {
    val processors: Collection<P>

    fun interface DisposableRegistry : Closeable {
        fun dispose()

        override fun close() {
            dispose()
        }
    }

    fun registerProcessor(processor: P): DisposableRegistry

    suspend fun process(
        data: D,
        attributes: TypeSafeMap = TypeSafeMap.EMPTY
    ): Collection<R>

}

@JvmInline
internal value class MutableProcessResult<R>(
    val data: MutableCollection<R>
)


internal interface PipelineConsumptionMarker

internal interface ProcessorPipelineContext<D, R> {

    /**
     * Child processes ([processAlso]) will inherit [attributes] from its parent, while any other properties from the context will not.
     */
    val attributes: TypeSafeMap

    val collected: MutableProcessResult<R>

    // DSL to simplify some expressions
    operator fun MutableProcessResult<R>.plusAssign(result: R?) {
        if (result != null) collect(result)
    }


    /**
     * Collect a result.
     */
    fun collect(result: R)

    /**
     * Collect results.
     */
    fun collect(results: Iterable<R>)


    val isConsumed: Boolean

    /**
     * Marks the input as consumed so that there will not be warnings like 'Unknown type xxx'. This will not stop the pipeline.
     *
     * If this is executed, make sure you provided all information important for debugging.
     *
     * You need to invoke [markAsConsumed] if your implementation includes some `else` branch which covers all situations,
     * and throws a [contextualBugReportException] or logs something.
     */
    @ConsumptionMarker
    fun PipelineConsumptionMarker.markAsConsumed(marker: Any = this)

    /**
     * Marks the input as not consumed, if it was marked by this [NoticeProcessor].
     */
    @ConsumptionMarker
    fun PipelineConsumptionMarker.markNotConsumed(marker: Any = this)

    @DslMarker
    annotation class ConsumptionMarker // to give an explicit color.

    /**
     * Fire the [data] into the processor pipeline, and collect the results to current [collected].
     *
     * @param attributes extra attributes
     * @return result collected from processors. This would also have been collected to this context (where you call [processAlso]).
     */
    suspend fun processAlso(data: D, attributes: TypeSafeMap = TypeSafeMap.EMPTY): Collection<R>
}

internal abstract class AbstractProcessorPipelineContext<D, R>(
    override val attributes: TypeSafeMap,
    private val traceLogging: MiraiLogger,
) : ProcessorPipelineContext<D, R> {
    private val consumers: Stack<Any> = Stack()

    override val isConsumed: Boolean get() = consumers.isNotEmpty()
    override fun PipelineConsumptionMarker.markAsConsumed(marker: Any) {
        traceLogging.info { "markAsConsumed: marker=$marker" }
        consumers.push(marker)
    }

    override fun PipelineConsumptionMarker.markNotConsumed(marker: Any) {
        if (consumers.peek() === marker) {
            consumers.pop()
            traceLogging.info { "markNotConsumed: Y, marker=$marker" }
        } else {
            traceLogging.info { "markNotConsumed: N, marker=$marker" }
        }
    }

    override val collected: MutableProcessResult<R> = MutableProcessResult(ConcurrentLinkedQueue())

    override fun collect(result: R) {
        collected.data.add(result)
        traceLogging.info { "collect: $result" }
    }

    override fun collect(results: Iterable<R>) {
        this.collected.data.addAll(results)
        traceLogging.info { "collect: $results" }
    }
}

internal class PipelineConfiguration(
    var stopWhenConsumed: Boolean
)

internal abstract class AbstractProcessorPipeline<P : Processor<C, D>, C : ProcessorPipelineContext<D, R>, D, R>
protected constructor(
    val configuration: PipelineConfiguration,
    val traceLogging: MiraiLogger,
) : ProcessorPipeline<P, D, R> {
    constructor(configuration: PipelineConfiguration) : this(configuration, SilentLogger)

    /**
     * Must be ordered
     */
    override val processors = ConcurrentLinkedQueue<P>()

    override fun registerProcessor(processor: P): ProcessorPipeline.DisposableRegistry {
        processors.add(processor)
        return ProcessorPipeline.DisposableRegistry {
            processors.remove(processor)
        }
    }

    protected abstract fun createContext(attributes: TypeSafeMap): C

    abstract inner class BaseContextImpl(
        attributes: TypeSafeMap,
    ) : AbstractProcessorPipelineContext<D, R>(attributes, traceLogging) {
        override suspend fun processAlso(data: D, attributes: TypeSafeMap): Collection<R> {
            traceLogging.info { "processAlso: data=$data" }
            return process(data, this.attributes + attributes).also {
                this.collected.data += it
                traceLogging.info { "processAlso: result=$it" }
            }
        }
    }

    protected open fun handleExceptionInProcess(
        data: D,
        context: C,
        attributes: TypeSafeMap,
        processor: P,
        e: Throwable
    ): Unit = throw e

    override suspend fun process(data: D, attributes: TypeSafeMap): Collection<R> {
        traceLogging.info { "process: data=$data" }
        val context = createContext(attributes)

        val diff = if (traceLogging.isEnabled) CollectionDiff<R>() else null
        diff?.save(context.collected.data)

        for (processor in processors) {

            val result = kotlin.runCatching {
                processor.process(context, data)
            }.onFailure { e ->
                handleExceptionInProcess(data, context, attributes, processor, e)
            }

            diff?.run {
                val diffPackets = subtractAndSave(context.collected.data)

                traceLogging.info {
                    "Finished ${
                        processor.toString().replace("net.mamoe.mirai.internal.network.notice.", "")
                    }, success=${result.isSuccess}, consumed=${context.isConsumed}, diff=$diffPackets"
                }
            }

            if (context.isConsumed && configuration.stopWhenConsumed) {
                traceLogging.info { "stopWhenConsumed=true, stopped." }

                break
            }
        }
        return context.collected.data
    }
}

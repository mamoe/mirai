/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.ParseErrorPacket
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.notice.decoders.DecodedNotifyMsgBody
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushStatus
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.OnlinePushPbPushTransMsg
import net.mamoe.mirai.internal.network.toPacket
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.TypeKey
import net.mamoe.mirai.utils.TypeSafeMap
import net.mamoe.mirai.utils.toDebugString
import net.mamoe.mirai.utils.uncheckedCast
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

internal typealias ProcessResult = Collection<Packet>

/**
 * Centralized processor pipeline for [MessageSvcPbGetMsg] and [OnlinePushPbPushTransMsg]
 */
internal interface NoticeProcessorPipeline {
    fun registerProcessor(processor: NoticeProcessor)

    /**
     * Process [data] into [Packet]s. Exceptions are wrapped into [ParseErrorPacket]
     */
    suspend fun process(bot: QQAndroidBot, data: ProtocolStruct, attributes: TypeSafeMap = TypeSafeMap()): ProcessResult

    companion object : ComponentKey<NoticeProcessorPipeline> {
        val ComponentStorage.noticeProcessorPipeline get() = get(NoticeProcessorPipeline)

        @JvmStatic
        suspend inline fun QQAndroidBot.processPacketThroughPipeline(
            data: ProtocolStruct,
            attributes: TypeSafeMap = TypeSafeMap(),
        ): Packet {
            return components.noticeProcessorPipeline.process(this, data, attributes).toPacket()
        }
    }
}

@JvmInline
internal value class MutableProcessResult(
    val data: MutableCollection<Packet>
)

internal interface PipelineContext {
    val bot: QQAndroidBot

    val attributes: TypeSafeMap


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
    fun NoticeProcessor.markAsConsumed()

    /**
     * Marks the input as not consumed, if it was marked by this [NoticeProcessor].
     */
    @ConsumptionMarker
    fun NoticeProcessor.markNotConsumed()

    @DslMarker
    annotation class ConsumptionMarker // to give an explicit color.


    val collected: MutableProcessResult

    // DSL to simplify some expressions
    operator fun MutableProcessResult.plusAssign(packet: Packet?) {
        if (packet != null) collect(packet)
    }


    /**
     * Collect a result.
     */
    fun collect(packet: Packet)

    /**
     * Collect results.
     */
    fun collect(packets: Iterable<Packet>)

    /**
     * Fire the [data] into the processor pipeline, and collect the results to current [collected].
     *
     * @return result collected from processors. This would also have been collected to this context (where you call [processAlso]).
     */
    suspend fun processAlso(data: ProtocolStruct): ProcessResult

    companion object {
        val KEY_FROM_SYNC = TypeKey<Boolean>("fromSync")
        val PipelineContext.fromSync get() = attributes[KEY_FROM_SYNC]
    }
}

internal inline val PipelineContext.context get() = this

internal open class NoticeProcessorPipelineImpl private constructor() : NoticeProcessorPipeline {
    /**
     * Must be ordered
     */
    private val processors = ConcurrentLinkedQueue<NoticeProcessor>()

    override fun registerProcessor(processor: NoticeProcessor) {
        processors.add(processor)
    }


    inner class ContextImpl(
        override val bot: QQAndroidBot, override val attributes: TypeSafeMap,
    ) : PipelineContext {
        private val consumers: Stack<NoticeProcessor> = Stack()

        override val isConsumed: Boolean get() = consumers.isNotEmpty()
        override fun NoticeProcessor.markAsConsumed() {
            consumers.push(this)
        }

        override fun NoticeProcessor.markNotConsumed() {
            if (consumers.peek() === this) {
                consumers.pop()
            }
        }

        override val collected = MutableProcessResult(ConcurrentLinkedQueue())

        override fun collect(packet: Packet) {
            collected.data.add(packet)
        }

        override fun collect(packets: Iterable<Packet>) {
            this.collected.data.addAll(packets)
        }

        override suspend fun processAlso(data: ProtocolStruct): ProcessResult {
            return process(bot, data, attributes)
        }
    }


    override suspend fun process(bot: QQAndroidBot, data: ProtocolStruct, attributes: TypeSafeMap): ProcessResult {
        val context = ContextImpl(bot, attributes)
        for (processor in processors) {
            kotlin.runCatching {
                processor.process(context, data)
            }.onFailure { e ->
                context.collect(
                    ParseErrorPacket(
                        data,
                        IllegalStateException(
                            "Exception in $processor while processing packet ${packetToString(data)}.",
                            e,
                        ),
                    ),
                )
            }
        }
        return context.collected.data
    }

    protected open fun packetToString(data: Any?): String =
        data.toDebugString("mirai.network.debug.notice.pipeline.log.full")


    companion object {
        fun create(vararg processors: NoticeProcessor): NoticeProcessorPipelineImpl =
            NoticeProcessorPipelineImpl().apply {
                for (processor in processors) {
                    registerProcessor(processor)
                }
            }
    }
}

///////////////////////////////////////////////////////////////////////////
// NoticeProcessor
///////////////////////////////////////////////////////////////////////////

/**
 * A processor handling some specific type of message.
 */
internal interface NoticeProcessor {
    suspend fun process(context: PipelineContext, data: Any?)
}

internal abstract class AnyNoticeProcessor : SimpleNoticeProcessor<ProtocolStruct>(type())

internal abstract class SimpleNoticeProcessor<in T : ProtocolStruct>(
    private val type: KClass<T>,
) : NoticeProcessor {

    final override suspend fun process(context: PipelineContext, data: Any?) {
        if (type.isInstance(data)) {
            context.processImpl(data.uncheckedCast())
        }
    }

    protected abstract suspend fun PipelineContext.processImpl(data: T)

    companion object {
        @JvmStatic
        protected inline fun <reified T : Any> type(): KClass<T> = T::class
    }
}

internal abstract class MsgCommonMsgProcessor : SimpleNoticeProcessor<MsgComm.Msg>(type()) {
    abstract override suspend fun PipelineContext.processImpl(data: MsgComm.Msg)
}

internal abstract class MixedNoticeProcessor : AnyNoticeProcessor() {
    final override suspend fun PipelineContext.processImpl(data: ProtocolStruct) {
        when (data) {
            is PbMsgInfo -> processImpl(data)
            is MsgOnlinePush.PbPushMsg -> processImpl(data)
            is MsgComm.Msg -> processImpl(data)
            is MsgType0x210 -> processImpl(data)
            is MsgType0x2DC -> processImpl(data)
            is Structmsg.StructMsg -> processImpl(data)
            is RequestPushStatus -> processImpl(data)
            is DecodedNotifyMsgBody -> processImpl(data)
        }
    }

    protected open suspend fun PipelineContext.processImpl(data: MsgType0x210) {} // 528
    protected open suspend fun PipelineContext.processImpl(data: MsgType0x2DC) {} // 732
    protected open suspend fun PipelineContext.processImpl(data: PbMsgInfo) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgOnlinePush.PbPushMsg) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgComm.Msg) {}
    protected open suspend fun PipelineContext.processImpl(data: Structmsg.StructMsg) {}
    protected open suspend fun PipelineContext.processImpl(data: RequestPushStatus) {}

    protected open suspend fun PipelineContext.processImpl(data: DecodedNotifyMsgBody) {}
}
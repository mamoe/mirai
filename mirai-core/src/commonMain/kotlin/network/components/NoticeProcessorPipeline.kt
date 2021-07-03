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
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
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


    val collected: Collection<Packet>

    // DSL to simplify some expressions
    operator fun Collection<Packet>.plusAssign(packet: Packet) {
        require(this === collected) { "`plusAssign` can only be applied to `collected`" }
        collect(packet)
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
     * Fire the [data] into the processor pipeline.
     *
     * @return result collected from processors. This would also have been collected to this context (where you call [fire]).
     */
    suspend fun fire(data: ProtocolStruct): ProcessResult

    companion object {
        val KEY_FROM_SYNC = TypeKey<Boolean>("fromSync")
        val PipelineContext.fromSync get() = attributes[KEY_FROM_SYNC]
    }
}

internal inline val PipelineContext.context get() = this

internal open class NoticeProcessorPipelineImpl : NoticeProcessorPipeline {
    private val processors = ArrayList<NoticeProcessor>()
    private val processorsLock = ReentrantReadWriteLock()

    override fun registerProcessor(processor: NoticeProcessor) {
        processorsLock.write {
            processors.add(processor)
        }
    }


    inner class ContextImpl(
        override val bot: QQAndroidBot, override val attributes: TypeSafeMap,
    ) : PipelineContext {

        private val consumers: Stack<NoticeProcessor> = Stack()

        override val isConsumed: Boolean = consumers.isNotEmpty()
        override fun NoticeProcessor.markAsConsumed() {
            consumers.push(this)
        }

        override fun NoticeProcessor.markNotConsumed() {
            if (consumers.peek() === this) {
                consumers.pop()
            }
        }

        override val collected = ConcurrentLinkedQueue<Packet>()

        override fun collect(packet: Packet) {
            collected.add(packet)
        }

        override fun collect(packets: Iterable<Packet>) {
            this.collected.addAll(packets)
        }

        override suspend fun fire(data: ProtocolStruct): ProcessResult {
            return process(bot, data, attributes)
        }
    }


    override suspend fun process(bot: QQAndroidBot, data: ProtocolStruct, attributes: TypeSafeMap): ProcessResult {
        processorsLock.read {
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
            return context.collected
        }
    }

    protected open fun packetToString(data: Any?): String =
        data.toDebugString("mirai.network.debug.notice.pipeline.log.full")

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

internal abstract class AnyNoticeProcessor : SimpleNoticeProcessor<Any>(type())

internal abstract class SimpleNoticeProcessor<T : Any>(
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
    final override suspend fun PipelineContext.processImpl(data: Any) {
        when (data) {
            is PbMsgInfo -> processImpl(data)
            is MsgOnlinePush.PbPushMsg -> processImpl(data)
            is MsgComm.Msg -> processImpl(data)
            is MsgType0x210 -> processImpl(data)
            is MsgType0x2DC -> processImpl(data)
            is Structmsg.StructMsg -> processImpl(data)
            is RequestPushStatus -> processImpl(data)
        }
    }

    protected open suspend fun PipelineContext.processImpl(data: MsgType0x210) {} // 528
    protected open suspend fun PipelineContext.processImpl(data: MsgType0x2DC) {} // 732
    protected open suspend fun PipelineContext.processImpl(data: PbMsgInfo) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgOnlinePush.PbPushMsg) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgComm.Msg) {}
    protected open suspend fun PipelineContext.processImpl(data: Structmsg.StructMsg) {}
    protected open suspend fun PipelineContext.processImpl(data: RequestPushStatus) {}
}
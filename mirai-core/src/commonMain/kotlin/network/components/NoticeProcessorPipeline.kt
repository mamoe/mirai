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
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.OnlinePushPbPushTransMsg
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TypeSafeMap
import net.mamoe.mirai.utils.uncheckedCast
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

internal interface PipelineContext {
    val bot: QQAndroidBot
    val attributes: TypeSafeMap


    val isConsumed: Boolean

    /**
     * Mark the input as consumed so that there will not be warnings like 'Unknown type xxx'
     *
     * If this is executed, make sure you provided all information important for debugging.
     *
     * You need to invoke [markAsConsumed] if your implementation includes some `else` branch which covers all situations,
     * and throws a [contextualBugReportException] or logs something.
     */
    fun markAsConsumed()


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
    suspend fun fire(data: Any?): Collection<Packet>
}

internal inline val PipelineContext.context get() = this

/**
 * Centralized processor pipeline for [MessageSvcPbGetMsg] and [OnlinePushPbPushTransMsg]
 */
internal interface NoticeProcessorPipeline {
    fun registerProcessor(processor: NoticeProcessor)

    suspend fun process(bot: QQAndroidBot, data: Any?, attributes: TypeSafeMap = TypeSafeMap()): Collection<Packet>

    companion object : ComponentKey<NoticeProcessorPipeline> {
        val ComponentStorage.noticeProcessorPipeline get() = get(NoticeProcessorPipeline)
    }
}

internal class NoticeProcessorPipelineImpl(
    private val logger: MiraiLogger,
) : NoticeProcessorPipeline {
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

        override var isConsumed: Boolean = false
        override fun markAsConsumed() {
            isConsumed = true
        }

        override val collected = ConcurrentLinkedQueue<Packet>()

        override fun collect(packet: Packet) {
            collected.add(packet)
        }

        override fun collect(packets: Iterable<Packet>) {
            this.collected.addAll(packets)
        }

        override suspend fun fire(data: Any?): Collection<Packet> {
            return process(bot, data, attributes)
        }
    }


    override suspend fun process(bot: QQAndroidBot, data: Any?, attributes: TypeSafeMap): Collection<Packet> {
        processorsLock.read {
            val context = ContextImpl(bot, attributes)
            for (processor in processors) {
                processor.process(context, data)
            }
            return context.collected
        }
    }

}

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
            context.process0(data.uncheckedCast())
        }
    }

    protected abstract suspend fun PipelineContext.process0(data: T)

    companion object {
        @JvmStatic
        protected inline fun <reified T : Any> type(): KClass<T> = T::class
    }
}

internal abstract class MsgCommonMsgProcessor : SimpleNoticeProcessor<MsgComm.Msg>(type()) {
    abstract override suspend fun PipelineContext.process0(data: MsgComm.Msg)
}

internal abstract class MixedNoticeProcessor : AnyNoticeProcessor() {
    final override suspend fun PipelineContext.process0(data: Any) {
        when (data) {
            is MsgInfo -> processImpl(data)
            is PbMsgInfo -> processImpl(data)
            is MsgOnlinePush.PbPushMsg -> processImpl(data)
            is MsgComm.Msg -> processImpl(data)
            is MsgType0x210 -> processImpl(data)
            is MsgType0x2DC -> processImpl(data)
            is Structmsg.StructMsg -> processImpl(data)
        }
    }

    protected open suspend fun PipelineContext.processImpl(data: MsgInfo) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgType0x210) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgType0x2DC) {}
    protected open suspend fun PipelineContext.processImpl(data: PbMsgInfo) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgOnlinePush.PbPushMsg) {}
    protected open suspend fun PipelineContext.processImpl(data: MsgComm.Msg) {}
    protected open suspend fun PipelineContext.processImpl(data: Structmsg.StructMsg) {}
}
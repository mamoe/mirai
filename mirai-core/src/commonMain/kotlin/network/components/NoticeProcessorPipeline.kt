/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.ParseErrorPacket
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.notice.BotAware
import net.mamoe.mirai.internal.network.notice.NewContactSupport
import net.mamoe.mirai.internal.network.notice.decoders.DecodedNotifyMsgBody
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushStatus
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.OnlinePushPbPushTransMsg
import net.mamoe.mirai.internal.network.toPacket
import net.mamoe.mirai.internal.pipeline.*
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * Centralized processor pipeline for [MessageSvcPbGetMsg] and [OnlinePushPbPushTransMsg]
 */
internal interface NoticeProcessorPipeline :
    ProcessorPipeline<NoticeProcessor, NoticePipelineContext, ProtocolStruct, Packet> {
    companion object : ComponentKey<NoticeProcessorPipeline> {
        val ComponentStorage.noticeProcessorPipeline get() = get(NoticeProcessorPipeline)

        @JvmStatic
        suspend inline fun QQAndroidBot.processPacketThroughPipeline(
            data: ProtocolStruct,
            attributes: TypeSafeMap = TypeSafeMap.EMPTY,
        ): Packet {
            return components.noticeProcessorPipeline.process(data, attributes).collected.toPacket()
        }
    }
}

internal interface NoticePipelineContext : BotAware, NewContactSupport,
    ProcessorPipelineContext<ProtocolStruct, Packet> {
    override val bot: QQAndroidBot

    companion object {
        val KEY_FROM_SYNC = TypeKey<Boolean>("fromSync")
        val KEY_MSG_INFO = TypeKey<MsgInfo>("msgInfo")

        val NoticePipelineContext.fromSync get() = attributes[KEY_FROM_SYNC]
        val NoticePipelineContext.fromSyncSafely get() = attributes[KEY_FROM_SYNC, false]

        /**
         * 来自 [MsgInfo] 的数据, 即 [MsgType0x210], [MsgType0x2DC] 的处理过程之中可以使用
         */
        val NoticePipelineContext.msgInfo get() = attributes[KEY_MSG_INFO]
    }
}

internal inline val NoticePipelineContext.context get() = this

private val defaultTraceLogging: MiraiLogger by lazy {
    MiraiLogger.Factory.create(NoticeProcessorPipelineImpl::class, "NoticeProcessorPipeline")
        .withSwitch(systemProp("mirai.network.notice.pipeline.log.full", false))
}

internal open class NoticeProcessorPipelineImpl protected constructor(
    private val bot: QQAndroidBot,
    traceLogging: MiraiLogger = defaultTraceLogging,
) : NoticeProcessorPipeline,
    AbstractProcessorPipeline<NoticeProcessor, NoticePipelineContext, ProtocolStruct, Packet>(
        PipelineConfiguration(stopWhenConsumed = false), traceLogging
    ) {

    open inner class ContextImpl(
        attributes: TypeSafeMap,
    ) : BaseContextImpl(attributes), NoticePipelineContext {
        override val bot: QQAndroidBot
            get() = this@NoticeProcessorPipelineImpl.bot
    }

    override fun handleExceptionInProcess(
        data: ProtocolStruct,
        context: NoticePipelineContext,
        attributes: TypeSafeMap,
        processor: NoticeProcessor,
        e: Throwable
    ) {
        context.collect(
            ParseErrorPacket(
                data,
                IllegalStateException(
                    "Exception in $processor while processing packet ${packetToString(data)}.",
                    e,
                ),
            )
        )
    }

    override fun createContext(data: ProtocolStruct, attributes: TypeSafeMap): NoticePipelineContext =
        ContextImpl(attributes)

    protected open fun packetToString(data: Any?): String =
        data.toDebugString("mirai.network.notice.pipeline.log.full")


    companion object {
        fun create(bot: QQAndroidBot, vararg processors: NoticeProcessor): NoticeProcessorPipelineImpl =
            NoticeProcessorPipelineImpl(bot, defaultTraceLogging).apply {
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
internal interface NoticeProcessor : Processor<NoticePipelineContext, ProtocolStruct>

internal abstract class AnyNoticeProcessor : SimpleNoticeProcessor<ProtocolStruct>(type())

internal abstract class SimpleNoticeProcessor<in T : ProtocolStruct>(
    private val type: KClass<T>,
) : NoticeProcessor {

    final override suspend fun process(context: NoticePipelineContext, data: ProtocolStruct) {
        if (type.isInstance(data)) {
            context.processImpl(data.uncheckedCast())
        }
    }

    protected abstract suspend fun NoticePipelineContext.processImpl(data: T)

    companion object {
        @JvmStatic
        protected inline fun <reified T : Any> type(): KClass<T> = T::class
    }
}

internal abstract class MsgCommonMsgProcessor : SimpleNoticeProcessor<MsgComm.Msg>(type()) {
    abstract override suspend fun NoticePipelineContext.processImpl(data: MsgComm.Msg)
}

internal abstract class MixedNoticeProcessor : AnyNoticeProcessor() {
    final override suspend fun NoticePipelineContext.processImpl(data: ProtocolStruct) {
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

    protected open suspend fun NoticePipelineContext.processImpl(data: MsgType0x210) {} // 528
    protected open suspend fun NoticePipelineContext.processImpl(data: MsgType0x2DC) {} // 732
    protected open suspend fun NoticePipelineContext.processImpl(data: PbMsgInfo) {}
    protected open suspend fun NoticePipelineContext.processImpl(data: MsgOnlinePush.PbPushMsg) {}
    protected open suspend fun NoticePipelineContext.processImpl(data: MsgComm.Msg) {}
    protected open suspend fun NoticePipelineContext.processImpl(data: Structmsg.StructMsg) {}
    protected open suspend fun NoticePipelineContext.processImpl(data: RequestPushStatus) {}

    protected open suspend fun NoticePipelineContext.processImpl(data: DecodedNotifyMsgBody) {}
}
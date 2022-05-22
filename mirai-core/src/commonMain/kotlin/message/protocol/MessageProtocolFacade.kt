/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.EmptyRefineContext
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.protocol.decode.*
import net.mamoe.mirai.internal.message.protocol.encode.*
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.runCoroutineInPlace
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.utils.buildTypeSafeMap
import java.util.*
import kotlin.reflect.KClass

internal interface MessageProtocolFacade {
    val encoderPipeline: MessageEncoderPipeline
    val decoderPipeline: MessageDecoderPipeline
    val loaded: List<MessageProtocol>

    fun encode(
        chain: MessageChain,
        messageTarget: ContactOrBot?, // for At.display, QuoteReply, Image, and more.
        withGeneralFlags: Boolean, // important for RichMessages, may also be helpful for others
        isForward: Boolean = false, // is inside forward, for At.display
    ): List<ImMsgBody.Elem>

    fun decode(
        elements: List<ImMsgBody.Elem>,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        bot: Bot,
        builder: MessageChainBuilder,
    )

    fun decode(
        elements: List<ImMsgBody.Elem>,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        bot: Bot,
    ): MessageChain = buildMessageChain { decode(elements, groupIdOrZero, messageSourceKind, bot, this) }

    companion object INSTANCE : MessageProtocolFacade by MessageProtocolFacadeImpl()
}

internal fun MessageProtocolFacade.decodeAndRefineLight(
    elements: List<ImMsgBody.Elem>,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind,
    bot: Bot,
    refineContext: RefineContext = EmptyRefineContext
): MessageChain = decode(elements, groupIdOrZero, messageSourceKind, bot).refineLight(bot, refineContext)

internal suspend fun MessageProtocolFacade.decodeAndRefineDeep(
    elements: List<ImMsgBody.Elem>,
    groupIdOrZero: Long,
    messageSourceKind: MessageSourceKind,
    bot: Bot,
    refineContext: RefineContext = EmptyRefineContext
): MessageChain = decode(elements, groupIdOrZero, messageSourceKind, bot).refineDeep(bot, refineContext)


internal class MessageProtocolFacadeImpl(
    protocols: Iterable<MessageProtocol> = ServiceLoader.load(MessageProtocol::class.java)
) : MessageProtocolFacade {
    override val encoderPipeline: MessageEncoderPipeline = MessageEncoderPipelineImpl()
    override val decoderPipeline: MessageDecoderPipeline = MessageDecoderPipelineImpl()

    override val loaded: List<MessageProtocol> = kotlin.run {
        val instances: PriorityQueue<MessageProtocol> = protocols
            .toCollection(PriorityQueue(MessageProtocol.PriorityComparator.reversed()))
        for (instance in instances) {
            instance.collectProcessors(object : ProcessorCollector() {
                override fun <T : SingleMessage> add(encoder: MessageEncoder<T>, elementType: KClass<T>) {
                    this@MessageProtocolFacadeImpl.encoderPipeline.registerProcessor(
                        MessageEncoderProcessor(
                            encoder,
                            elementType
                        )
                    )
                }

                override fun add(decoder: MessageDecoder) {
                    this@MessageProtocolFacadeImpl.decoderPipeline.registerProcessor(MessageDecoderProcessor(decoder))
                }

            })
        }
        instances.toList()
    }

    override fun encode(
        chain: MessageChain,
        messageTarget: ContactOrBot?,
        withGeneralFlags: Boolean,
        isForward: Boolean
    ): List<ImMsgBody.Elem> {
        val pipeline = encoderPipeline

        val attributes = buildTypeSafeMap {
            set(MessageEncoderContext.CONTACT, messageTarget)
            set(MessageEncoderContext.ORIGINAL_MESSAGE, chain)
            set(MessageEncoderContext.ADD_GENERAL_FLAGS, withGeneralFlags)
            set(MessageEncoderContext.IS_FORWARD, isForward)
        }

        val builder = ArrayList<ImMsgBody.Elem>(chain.size)

        chain.accept(object : RecursiveMessageVisitor<Unit>() {
            override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                runCoroutineInPlace {
                    builder.addAll(pipeline.process(message, attributes))
                }
            }
        })

        return builder
    }

    override fun decode(
        elements: List<ImMsgBody.Elem>,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        bot: Bot,
        builder: MessageChainBuilder
    ) {
        val pipeline = decoderPipeline

        val attributes = buildTypeSafeMap {
            set(MessageDecoderContext.BOT, bot)
            set(MessageDecoderContext.MESSAGE_SOURCE_KIND, messageSourceKind)
            set(MessageDecoderContext.GROUP_ID, groupIdOrZero)
        }

        runCoroutineInPlace {
            elements.forEach { builder.addAll(pipeline.process(it, attributes)) }
        }
    }
}

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.SendMessageStep
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.EmptyRefineContext
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.message.protocol.decode.*
import net.mamoe.mirai.internal.message.protocol.encode.*
import net.mamoe.mirai.internal.message.protocol.outgoing.*
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.COMPONENTS
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.MESSAGE_TO_RETRY
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.ORIGINAL_MESSAGE
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.ORIGINAL_MESSAGE_AS_CHAIN
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.STEP
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.buildComponentStorage
import net.mamoe.mirai.internal.network.component.withFallback
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.pipeline.ProcessResult
import net.mamoe.mirai.internal.utils.runCoroutineInPlace
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.utils.*
import kotlin.reflect.KClass

internal interface MessageProtocolFacade {
    val remark: String get() = "MessageProtocolFacade"

    val encoderPipeline: MessageEncoderPipeline
    val decoderPipeline: MessageDecoderPipeline
    val preprocessorPipeline: OutgoingMessagePipeline
    val outgoingPipeline: OutgoingMessagePipeline
    val serializers: Collection<MessageSerializer<*>>

    val loaded: List<MessageProtocol>

    /**
     * Encode high-level [MessageChain] to give list of low-level and protocol-specific [ImMsgBody.Elem]s.
     */
    fun encode(
        chain: MessageChain,
        messageTarget: ContactOrBot?, // for At.display, QuoteReply, Image, and more.
        withGeneralFlags: Boolean, // important for RichMessages, may also be helpful for others
        isForward: Boolean = false, // is inside forward, for At.display
    ): List<ImMsgBody.Elem>

    /**
     * Decode list of low-level and protocol-specific [ImMsgBody.Elem]s to give a high-level [MessageChain].
     *
     * [SingleMessage]s are appended to the [builder].
     */
    fun decode(
        elements: List<ImMsgBody.Elem>,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        bot: Bot,
        builder: MessageChainBuilder,
        containingMsg: MsgComm.Msg? = null,
    )


    /**
     * Pre-process a message
     * @see OutgoingMessagePreprocessor
     */
    suspend fun <C : AbstractContact> preprocess(
        target: C,
        message: Message,
        components: ComponentStorage,
    ): MessageChain

    /**
     * Send a message
     * @see OutgoingMessageProcessor
     */
    suspend fun <C : AbstractContact> sendOutgoing(
        target: C,
        message: Message,
        components: ComponentStorage,
    ): MessageReceipt<C>

    /**
     * Preprocess and send a message
     * @see OutgoingMessagePreprocessor
     * @see OutgoingMessageProcessor
     */
    suspend fun <C : AbstractContact> preprocessAndSendOutgoing(
        target: C,
        message: Message,
        components: ComponentStorage,
    ): MessageReceipt<C>


    /**
     * Preprocess and send a message
     * @see OutgoingMessagePreprocessor
     * @see OutgoingMessageProcessor
     */
    @TestOnly
    suspend fun <C : AbstractContact> preprocessAndSendOutgoingImpl(
        target: C,
        message: Message,
        components: ComponentStorage,
    ): ProcessResult<OutgoingMessagePipelineContext, MessageReceipt<*>>

    /**
     * Decode list of low-level and protocol-specific [ImMsgBody.Elem]s to give a high-level [MessageChain].
     */
    fun decode(
        elements: List<ImMsgBody.Elem>,
        groupIdOrZero: Long,
        messageSourceKind: MessageSourceKind,
        bot: Bot,
    ): MessageChain = buildMessageChain {
        decode(elements, groupIdOrZero, messageSourceKind, bot, this, null)
    }


    fun createSerializersModule(): SerializersModule = SerializersModule {
        serializers.forEach { ms ->
            @Suppress("UNCHECKED_CAST")
            ms as MessageSerializer<SingleMessage>
            for (superclass in ms.superclasses) {
                polymorphic(superclass) {
                    subclass(ms.forClass, ms.serializer)
                }
            }
            if (ms.registerAlsoContextual) {
                contextual(ms.forClass, ms.serializer)
            }
//            contextual(ms.forClass, ms.serializer)
        }
    }

    fun copy(): MessageProtocolFacade

    /**
     * The default global instance.
     */
    companion object INSTANCE : MessageProtocolFacade by MessageProtocolFacadeImpl(),
        ComponentKey<MessageProtocolFacade> {
        init {
            MessageSerializers.registerSerializers(createSerializersModule())
        }
    }
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


private const val errorTips =
    "This should not happen if you are using mirai under default JVM classloader or using Mirai Console." +
            "If so, please file an issue. " +
            "If you are trying to load mirai manually from other classloader, " +
            "e.g. in another plugin system like Minecraft, it's your responsibility to ensure the Java SPI works."

internal class MessageProtocolFacadeImpl(
    private val protocols: Iterable<MessageProtocol> = loadServices(MessageProtocol::class).asIterable(),
    override val remark: String = "MessageProtocolFacade"
) : MessageProtocolFacade {
    override val encoderPipeline: MessageEncoderPipeline = MessageEncoderPipelineImpl()
    override val decoderPipeline: MessageDecoderPipeline = MessageDecoderPipelineImpl()
    override val preprocessorPipeline: OutgoingMessagePipeline = OutgoingMessagePipelineImpl()
    override val outgoingPipeline: OutgoingMessagePipeline = OutgoingMessagePipelineImpl()
    override val serializers: MutableCollection<MessageSerializer<*>> = ArrayList(10)

    override val loaded: List<MessageProtocol> = kotlin.run {
        val instances = protocols
            .sortedWith(MessageProtocol.PriorityComparator.reversed())
        if (instances.isEmpty()) {
            error(
                "Failed to load services for MessageProtocol from your classpath. " +
                        "Check you ClassLoader environment and ensure services for '${MessageProtocol::class.qualifiedName}' can be loaded. $errorTips"
            )
        }
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

                override fun add(preprocessor: OutgoingMessagePreprocessor) {
                    preprocessorPipeline.registerProcessor(OutgoingMessageProcessorAdapter(preprocessor))
                }

                override fun add(transformer: OutgoingMessageTransformer) {
                    outgoingPipeline.registerProcessor(OutgoingMessageProcessorAdapter(transformer))
                }

                override fun add(sender: OutgoingMessageSender) {
                    outgoingPipeline.registerProcessor(OutgoingMessageProcessorAdapter(sender))
                }

                override fun add(postprocessor: OutgoingMessagePostprocessor) {
                    outgoingPipeline.registerProcessor(OutgoingMessageProcessorAdapter(postprocessor))
                }

                override fun <T : Any> add(serializer: MessageSerializer<T>) {
                    serializers.add(serializer)
                }
            })
        }
        instances.toList()
    }

    private fun checkOutgoingPipeline() {
        if (outgoingPipeline.processors.isEmpty()) {
            error(
                "`outgoingPipeline` is empty. It means you have corrupted classpath or bad service configuration. $errorTips"
            )
        }
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
                    builder.addAll(pipeline.process(message, attributes).collected)
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
        builder: MessageChainBuilder,
        containingMsg: MsgComm.Msg?
    ) {
        val pipeline = decoderPipeline

        val attributes = buildTypeSafeMap {
            set(MessageDecoderContext.BOT, bot)
            set(MessageDecoderContext.MESSAGE_SOURCE_KIND, messageSourceKind)
            set(MessageDecoderContext.GROUP_ID, groupIdOrZero)
            set(MessageDecoderContext.CONTAINING_MSG, containingMsg)
        }

        runCoroutineInPlace {
            elements.forEach { builder.addAll(pipeline.process(it, attributes).collected) }
        }
    }

    private val thisComponentStorage by lazy {
        buildComponentStorage {
            set(
                MessageProtocolFacade,
                this@MessageProtocolFacadeImpl
            )
        }
    }

    override suspend fun <C : AbstractContact> preprocess(
        target: C,
        message: Message,
        components: ComponentStorage
    ): MessageChain {
        val attributes = createAttributesForOutgoingMessage(target, message, components)

        return preprocessorPipeline.process(message.toMessageChain(), attributes).context.currentMessageChain
    }

    override suspend fun <C : AbstractContact> sendOutgoing(
        target: C, message: Message,
        components: ComponentStorage
    ): MessageReceipt<C> {
        checkOutgoingPipeline()

        val attributes = createAttributesForOutgoingMessage(target, message, components)
        val (_, result) = outgoingPipeline.process(message.toMessageChain(), attributes)

        return getSingleReceipt(result, message)
    }

    override suspend fun <C : AbstractContact> preprocessAndSendOutgoing(
        target: C,
        message: Message,
        components: ComponentStorage
    ): MessageReceipt<C> {
        @OptIn(TestOnly::class)
        return getSingleReceipt(preprocessAndSendOutgoingImpl(target, message, components).collected, message)
    }

    @TestOnly
    override suspend fun <C : AbstractContact> preprocessAndSendOutgoingImpl(
        target: C,
        message: Message,
        components: ComponentStorage
    ): ProcessResult<OutgoingMessagePipelineContext, MessageReceipt<*>> {
        checkOutgoingPipeline()
        val attributes = createAttributesForOutgoingMessage(target, message, components)

        val data = message.toMessageChain()
        val (context, _) = preprocessorPipeline.process(data, attributes)
        val preprocessed = context.currentMessageChain

        return outgoingPipeline.process(
            data,
            outgoingPipeline.createContext(preprocessed, context.attributes.plus(MESSAGE_TO_RETRY to preprocessed)),
            attributes
        )
    }

    override fun copy(): MessageProtocolFacade {
        return MessageProtocolFacadeImpl(protocols)
    }

    private fun <C : AbstractContact> getSingleReceipt(
        result: Collection<MessageReceipt<*>>,
        message: Message
    ): MessageReceipt<C> {
        when (result.size) {
            0 -> throw contextualBugReportException(
                "Internal error: no MessageReceipt was returned from OutgoingMessagePipeline for message",
                forDebug = message.structureToString()
            )

            1 -> return result.single().castUp()
            else -> throw contextualBugReportException(
                "Internal error: multiple MessageReceipts were returned from OutgoingMessagePipeline: $result",
                forDebug = message.structureToString()
            )
        }
    }

    private fun <C : AbstractContact> createAttributesForOutgoingMessage(
        target: C,
        message: Message,
        context: ComponentStorage
    ): MutableTypeSafeMap {
        val chain = message.toMessageChain()
        val attributes = buildTypeSafeMap {
            set(CONTACT, target.impl())
            set(ORIGINAL_MESSAGE, message)
            set(ORIGINAL_MESSAGE_AS_CHAIN, chain)
            set(STEP, SendMessageStep.FIRST)
            set(COMPONENTS, thisComponentStorage.withFallback(context))
            set(MESSAGE_TO_RETRY, chain)
        }
        return attributes
    }
}

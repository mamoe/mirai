/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.*
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.message.data.inferMessageSourceKind
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacadeImpl
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderPipelineImpl
import net.mamoe.mirai.internal.message.protocol.decodeAndRefineLight
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderPipelineImpl
import net.mamoe.mirai.internal.message.protocol.outgoing.HighwayUploader
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToFriendImpl
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.internal.testFramework.dynamicTest
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.utils.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.test.*

@OptIn(TestOnly::class)
internal abstract class AbstractMessageProtocolTest : AbstractMockNetworkHandlerTest(), GroupExtensions {
    init {
        setSystemProp("mirai.message.protocol.log.full", "true")
        setSystemProp("mirai.message.outgoing.pipeline.log.full", "true")
    }

    override fun createAccount(): BotAccount = BotAccount(1230001L, "pwd")

    protected abstract val protocols: Array<out MessageProtocol>
    protected var defaultTarget: ContactOrBot by lateinitMutableProperty {
        bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    private var decoderLoggerEnabled = false
    private var encoderLoggerEnabled = false

    @BeforeTest
    fun beforeEach() {
        decoderLoggerEnabled = MessageDecoderPipelineImpl.defaultTraceLogging.isEnabled
        MessageDecoderPipelineImpl.defaultTraceLogging.enable()
        encoderLoggerEnabled = MessageEncoderPipelineImpl.defaultTraceLogging.isEnabled
        MessageEncoderPipelineImpl.defaultTraceLogging.enable()
    }

    @AfterTest
    fun afterEach() {
        if (!decoderLoggerEnabled) {
            MessageDecoderPipelineImpl.defaultTraceLogging.disable()
        }
        if (!encoderLoggerEnabled) {
            MessageEncoderPipelineImpl.defaultTraceLogging.disable()
        }
    }

    protected fun facadeOf(vararg protocols: MessageProtocol): MessageProtocolFacade {
        return MessageProtocolFacadeImpl(
            protocols.toList(),
            remark = "MessageProtocolFacade with ${protocols.joinToString { it::class.simpleName!! }}"
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // coding
    ///////////////////////////////////////////////////////////////////////////

    protected fun doEncoderChecks(
        expectedStruct: List<ImMsgBody.Elem>,
        protocols: Array<out MessageProtocol>,
        encode: MessageProtocolFacade.() -> List<ImMsgBody.Elem>
    ) {
        asserter.assertEquals(
            expectedStruct,
            facadeOf(*protocols).encode(),
            message = "Failed to check single Protocol"
        )
        asserter.assertEquals(
            expectedStruct,
            MessageProtocolFacade.INSTANCE.encode(),
            message = "Failed to check with all protocols"
        )
    }

    var asserter: EqualityAsserter = EqualityAsserter.OrdinaryThenStructural

    fun useOrdinaryEquality() {
        asserter = EqualityAsserter.Ordinary
    }

    fun useStructuralEquality() {
        asserter = EqualityAsserter.Structural
    }

    protected fun doDecoderChecks(
        expectedChain: MessageChain,
        protocols: Array<out MessageProtocol> = this.protocols,
        decode: MessageProtocolFacade.() -> MessageChain
    ) {
        asserter.assertEquals(
            expectedChain.toList(),
            facadeOf(*protocols).decode().toList(),
            message = "Failed to check single Protocol"
        )
        asserter.assertEquals(
            expectedChain.toList(),
            MessageProtocolFacade.INSTANCE.decode().toList(),
            message = "Failed to check with all protocols"
        )
    }

    protected fun doEncoderChecks(
        vararg expectedStruct: ImMsgBody.Elem,
        protocols: Array<out MessageProtocol> = this.protocols,
        encode: MessageProtocolFacade.() -> List<ImMsgBody.Elem>
    ): Unit = doEncoderChecks(expectedStruct.toList(), protocols, encode)


    inner class CodingChecksBuilder {
        var elems: MutableList<ImMsgBody.Elem> = mutableListOf()
        var messages: MessageChainBuilder = MessageChainBuilder()

        var groupIdOrZero: Long = 0
        var target: ContactOrBot = defaultTarget
        var messageSourceKind: MessageSourceKind by lateinitMutableProperty { target.inferMessageSourceKind() }
        var withGeneralFlags = true
        var isForward = false

        fun elem(vararg elem: ImMsgBody.Elem) {
            elems.addAll(elem)
        }

        fun message(vararg message: SingleMessage) {
            messages.addAll(message)
        }

        fun target(target: ContactOrBot) {
            this.target = target

            messageSourceKind = target.inferMessageSourceKind()

            if (target is Group) {
                groupIdOrZero = target.id
            }
        }

        fun forward() {
            this.isForward = true
        }

        fun build() = ChecksConfiguration(
            elems.toList(),
            messages.build(),
            groupIdOrZero,
            messageSourceKind,
            target,
            withGeneralFlags,
            isForward
        )
    }

    class ChecksConfiguration(
        val elems: List<ImMsgBody.Elem>,
        val messageChain: MessageChain,
        val groupIdOrZero: Long,
        val messageSourceKind: MessageSourceKind,
        val target: ContactOrBot?,
        val withGeneralFlags: Boolean,
        val isForward: Boolean,
    )

    @Suppress("DeferredIsResult")
    protected fun buildCodingChecks(
        builderAction: CodingChecksBuilder.() -> Unit,
    ): Deferred<ChecksConfiguration> { // IDE will warn you if you forget to call .do
        contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
        return CompletableDeferred(CodingChecksBuilder().apply(builderAction).build())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected open fun Deferred<ChecksConfiguration>.doEncoderChecks() {
        val config = this.getCompleted()
        doEncoderChecks(config.elems, protocols) {
            encode(
                config.messageChain,
                config.target,
                withGeneralFlags = config.withGeneralFlags,
                isForward = config.isForward
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected open fun Deferred<ChecksConfiguration>.doDecoderChecks() {
        val config = this.getCompleted()
        doDecoderChecks(config.messageChain, protocols) {
            decodeAndRefineLight(
                config.elems,
                config.groupIdOrZero,
                config.messageSourceKind,
                bot
            )
        }
    }

    protected open fun Deferred<ChecksConfiguration>.doBothChecks() {
        doEncoderChecks()
        doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // sending
    ///////////////////////////////////////////////////////////////////////////

    open inner class TestMessageProtocolStrategy : MessageProtocolStrategy<AbstractContact> {
        override suspend fun sendPacket(bot: AbstractBot, packet: OutgoingPacket): Packet {
            assertEquals(0x123, packet.sequenceId)
            return MessageSvcPbSendMsg.Response.SUCCESS(123)
        }

        override suspend fun createPacketsForGeneralMessage(
            client: QQAndroidClient,
            contact: AbstractContact,
            message: MessageChain,
            originalMessage: MessageChain,
            fragmented: Boolean,
            sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
        ): List<OutgoingPacket> {
            sourceCallback(CompletableDeferred(constructSourceForSpecialMessage(originalMessage, 1000)))
            return listOf(OutgoingPacket("Test", "test", 0x123, ByteReadPacket.Empty))
        }

        override suspend fun constructSourceForSpecialMessage(
            originalMessage: MessageChain,
            fromAppId: Int
        ): OnlineMessageSource.Outgoing {
            return when (val defaultTarget = defaultTarget) {
                is Group -> OnlineMessageSourceToGroupImpl(
                    coroutineScope = defaultTarget,
                    internalIds = intArrayOf(1),
                    time = 1,
                    originalMessage = originalMessage,
                    sender = bot,
                    target = defaultTarget
                )

                is Friend -> OnlineMessageSourceToFriendImpl(
                    sequenceIds = intArrayOf(1),
                    internalIds = intArrayOf(1),
                    time = 1,
                    originalMessage = originalMessage,
                    sender = bot,
                    target = defaultTarget
                )

                else -> error("Unexpected target: $defaultTarget")
            }
        }

    }

    init {
        components[MessageProtocolStrategy] = TestMessageProtocolStrategy()
        components[HighwayUploader] = object : HighwayUploader {
            override suspend fun uploadMessages(
                contact: AbstractContact,
                components: ComponentStorage,
                nodes: Collection<ForwardMessage.INode>,
                isLong: Boolean,
                senderName: String
            ): String {
                return "(size=${nodes.size})${
                    nodes.joinToString().replace(bot.id.toString(), "123123").md5().toUHexString("")
                }"
            }
        }
        components[ClockHolder] = object : ClockHolder() {
            override val local: Clock = object : Clock {
                override fun currentTimeMillis(): Long = 160023456
            }
        }
    }

    fun runWithFacade(action: suspend MessageProtocolFacade.() -> Unit) {
        runBlockingUnit {
            facadeOf(*protocols).run { action() }
            MessageProtocolFacade.INSTANCE.copy().run { action() }
        }
    }

    companion object {
        fun assertMessageEquals(expected: Message, actual: Message) {
            val expectedChain = expected.toMessageChain()
            val actualChain = actual.toMessageChain()

            val message = """
                Expected: $1
                                
                Actual: $2
            """.trimIndent().replace("$1", expectedChain.render()).replace("$2", actualChain.render())
            assertEquals(expectedChain.size, actualChain.size, message)
            asserter.assertEquals(message, expectedChain, actualChain)
        }

        fun MessageProtocolFacade.assertMessageEquals(expected: Message, actual: Message) {
            val expectedChain = expected.toMessageChain()
            val actualChain = actual.toMessageChain()

            val message = """
                Facade: ${this.remark}
                Expected: $1
                                
                Actual: $2
            """.trimIndent().replace("$1", expectedChain.render()).replace("$2", actualChain.render())
            assertEquals(expectedChain.size, actualChain.size, message)
            asserter.assertEquals(message, expectedChain, actualChain)
        }

        inline fun Asserter.assertEquals(crossinline message: () -> String, expected: Any?, actual: Any?) {
            assertTrue({ message() + ". Expected <$expected>, actual <$actual>." }, actual == expected)
        }


        fun MessageChain.render(): String = buildString {
            appendLine("size = $size")
            for (singleMessage in distinct()) {
                val count = this@render.count { it == singleMessage }
                appendLine("$count x [${singleMessage::class.simpleName}] $singleMessage")
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////

    open val format: Json
        // `serializersModule` is volatile, always return new Json instances.
        get() = Json {
            prettyPrint = true
            serializersModule = MessageSerializers.serializersModule
        }

    ///////////////////////////////////////////////////////////////////////////
    // serialization - polymorphism
    ///////////////////////////////////////////////////////////////////////////

    interface PolymorphicWrapper {
        val message: SingleMessage
    }

    /**
     * @param expectedSerialName also known as *poly discriminator*, give `null` to check for no discriminator's presence
     */
    protected open fun <M : SingleMessage, P : PolymorphicWrapper> testPolymorphicIn(
        polySerializer: KSerializer<P>,
        polyConstructor: (message: M) -> P,
        data: M,
        expectedSerialName: String?,
        expectedInstance: M = data,
    ) {
        val string = format.encodeToString(
            polySerializer,
            polyConstructor(data)
        )
        println(string)
        var element = format.parseToJsonElement(string)
        element as JsonObject
        element = element["message"] as JsonObject
        if (expectedSerialName != null) {
            assertEquals(expectedSerialName, element["type"]?.cast<JsonPrimitive>()?.content)
        } else {
            assertEquals(null, element["type"])
        }
        assertEquals(
            expectedInstance,
            format.decodeFromString(polySerializer, string).message
        )
    }

    @Serializable
    data class PolymorphicWrapperSingleMessage(
        override val message: @Polymorphic SingleMessage
    ) : PolymorphicWrapper

    protected open fun <M : SingleMessage> testPolymorphicInSingleMessage(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInSingleMessage") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperSingleMessage.serializer(),
            polyConstructor = ::PolymorphicWrapperSingleMessage,
            data = data,
            expectedSerialName = expectedSerialName,
            expectedInstance = expectedInstance
        )

    })

    @Serializable
    data class PolymorphicWrapperMessageContent(
        override val message: @Polymorphic MessageContent
    ) : PolymorphicWrapper

    protected open fun <M : MessageContent> testPolymorphicInMessageContent(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInMessageContent") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperMessageContent.serializer(),
            polyConstructor = ::PolymorphicWrapperMessageContent,
            data = data,
            expectedSerialName = expectedSerialName,
            expectedInstance = expectedInstance
        )
    })

    @Serializable
    data class PolymorphicWrapperMessageMetadata(
        override val message: @Polymorphic MessageMetadata
    ) : PolymorphicWrapper

    protected open fun <M : MessageMetadata> testPolymorphicInMessageMetadata(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInMessageMetadata") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperMessageMetadata.serializer(),
            polyConstructor = ::PolymorphicWrapperMessageMetadata,
            data = data,
            expectedSerialName = expectedSerialName,
            expectedInstance = expectedInstance
        )
    })

    ///////////////////////////////////////////////////////////////////////////
    // serialization - in MessageChain
    ///////////////////////////////////////////////////////////////////////////

    protected open fun <M : SingleMessage> testInsideMessageChain(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testInsideMessageChain") {
        val chain = messageChainOf(data)

        val string = chain.serializeToJsonString(format)
        println(string)
        val element = format.parseToJsonElement(string).jsonArray.single().jsonObject
        assertEquals(expectedSerialName, element["type"]?.cast<JsonPrimitive>()?.content)

        assertEquals(
            expectedInstance,
            MessageChain.deserializeFromJsonString(string).single()
        )
    })

    ///////////////////////////////////////////////////////////////////////////
    // serialization - contextual
    ///////////////////////////////////////////////////////////////////////////

    @Serializable
    data class ContextualWrapper(
        val message: @Contextual SingleMessage,
    )

    @Serializable
    data class GenericTypedWrapper<T : SingleMessage>(
        val message: T,
    )

    protected open fun <M : T, T : SingleMessage> testContextual(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
        targetType: KClass<out T> = data::class,
    ) = listOf(
        testContextualWithWrapper<M, T>(data, expectedSerialName, expectedInstance),
        testContextualWithStaticType(targetType, data, expectedInstance),
        testContextualGeneric(targetType, data, expectedInstance),
        testContextualWithoutWrapper<M, T>(data, expectedInstance)
    ).flatten()

    private fun <M : T, T : SingleMessage> testContextualWithoutWrapper(
        data: M,
        expectedInstance: M
    ) = listOf(dynamicTest("testContextualWithoutWrapper") {
        @Suppress("UNCHECKED_CAST")
        val serializer = ContextualSerializer(data::class) as KSerializer<M>
        val string = format.encodeToString(serializer, data)
        println(string)
        val element = format.parseToJsonElement(string).jsonObject
        assertEquals(null, element["type"])

        assertEquals(
            expectedInstance,
            format.decodeFromString(serializer, string)
        )
    })

    private fun <M : T, T : SingleMessage> testContextualGeneric(
        targetType: KClass<out T>,
        data: M,
        expectedInstance: M
    ) = listOf(dynamicTest("testContextualGeneric") {
        val messageSerializer: ContextualSerializer<SingleMessage> = ContextualSerializer(targetType).cast()

        /**
         * ```
         * data class StaticTypedWrapper(
         *     val message: T
         * )
         * ```
         * without concern of generic types.
         */
        /**
         * ```
         * data class StaticTypedWrapper(
         *     val message: T
         * )
         * ```
         * without concern of generic types.
         */
        val serializer = GenericTypedWrapper.serializer(messageSerializer)

        val string = format.encodeToString(serializer, GenericTypedWrapper(data))
        println(string)
        val element = format.parseToJsonElement(string).jsonObject["message"]!!.jsonObject

        assertEquals(null, element["type"]?.jsonPrimitive?.content)

        assertEquals(
            expectedInstance,
            format.decodeFromString(serializer, string).message
        )
    })

    private fun <M : T, T : SingleMessage> testContextualWithStaticType(
        targetType: KClass<out T>,
        data: M,
        expectedInstance: M,
    ) = listOf(dynamicTest("testContextualWithStaticType") {
        val messageSerializer: ContextualSerializer<SingleMessage> = ContextualSerializer(targetType).cast()

        /**
         * ```
         * data class StaticTypedWrapper(
         *     val message: T
         * )
         * ```
         * without concern of generic types.
         */
        /**
         * ```
         * data class StaticTypedWrapper(
         *     val message: T
         * )
         * ```
         * without concern of generic types.
         */
        val serializer = object : KSerializer<SingleMessage> {
            override val descriptor: SerialDescriptor = buildClassSerialDescriptor("StaticTypedWrapper") {
                element("message", messageSerializer.descriptor, listOf(Contextual()))
            }

            override fun deserialize(decoder: Decoder): SingleMessage {
                return decoder.decodeStructure(descriptor) {
                    if (this.decodeSequentially()) {
                        this.decodeSerializableElement(
                            descriptor.getElementDescriptor(0),
                            0,
                            messageSerializer
                        )
                    } else {
                        val index = this.decodeElementIndex(descriptor)
                        check(index == 0)
                        this.decodeSerializableElement(descriptor, index, messageSerializer)
                    }
                }
            }

            override fun serialize(encoder: Encoder, value: SingleMessage) {
                encoder.encodeStructure(descriptor) {
                    encodeSerializableElement(descriptor, 0, messageSerializer, value)
                }
            }
        }

        val string = format.encodeToString(serializer, data)
        println(string)
        val element = format.parseToJsonElement(string).jsonObject["message"]!!.jsonObject

        assertEquals(null, element["type"]?.jsonPrimitive?.content)

        assertEquals(
            expectedInstance,
            format.decodeFromString(serializer, string)
        )
    })

    private fun <M : T, T : SingleMessage> testContextualWithWrapper(
        data: M,
        expectedSerialName: String,
        expectedInstance: M,
        afterSerialization: (element: JsonObject) -> Unit = {}
    ) = listOf(dynamicTest("testContextualWithWrapper") {
        val string = format.encodeToString(ContextualWrapper.serializer(), ContextualWrapper(data))
        println(string)
        val element = format.parseToJsonElement(string).jsonObject["message"]!!.jsonObject
        afterSerialization(element)

        assertEquals(expectedSerialName, element["type"]?.jsonPrimitive?.content)

        assertEquals(
            expectedInstance,
            format.decodeFromString(ContextualWrapper.serializer(), string).message
        )
    })
}
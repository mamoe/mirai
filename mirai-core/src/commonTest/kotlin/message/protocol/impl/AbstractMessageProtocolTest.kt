/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.message.data.inferMessageSourceKind
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacadeImpl
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderPipelineImpl
import net.mamoe.mirai.internal.message.protocol.decodeAndRefineLight
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderPipelineImpl
import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.SingleMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal abstract class AbstractMessageProtocolTest : AbstractMockNetworkHandlerTest(), GroupExtensions {

    protected abstract val protocols: Array<out MessageProtocol>
    protected var defaultTarget: ContactOrBot? = null

    private var decoderLoggerEnabled = false
    private var encoderLoggerEnabled = false

    @BeforeEach
    fun beforeEach() {
        decoderLoggerEnabled = MessageDecoderPipelineImpl.defaultTraceLogging.isEnabled
        MessageDecoderPipelineImpl.defaultTraceLogging.enable()
        encoderLoggerEnabled = MessageEncoderPipelineImpl.defaultTraceLogging.isEnabled
        MessageEncoderPipelineImpl.defaultTraceLogging.enable()
    }

    @AfterEach
    fun afterEach() {
        if (!decoderLoggerEnabled) {
            MessageDecoderPipelineImpl.defaultTraceLogging.disable()
        }
        if (!encoderLoggerEnabled) {
            MessageEncoderPipelineImpl.defaultTraceLogging.disable()
        }
    }

    protected fun facadeOf(vararg protocols: MessageProtocol): MessageProtocolFacade {
        return MessageProtocolFacadeImpl(protocols.toList())
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
        var messageSourceKind: MessageSourceKind = MessageSourceKind.GROUP
        var target: ContactOrBot? = defaultTarget
        var withGeneralFlags = true
        var isForward = false

        fun elem(vararg elem: ImMsgBody.Elem) {
            elems.addAll(elem)
        }

        fun message(vararg message: SingleMessage) {
            messages.addAll(message)
        }

        fun target(target: ContactOrBot?) {
            this.target = target

            if (target != null) {
                messageSourceKind = target.inferMessageSourceKind()
            }

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
            decodeAndRefineLight(config.elems, config.groupIdOrZero, config.messageSourceKind, bot)
        }
    }

    protected open fun Deferred<ChecksConfiguration>.doBothChecks() {
        doEncoderChecks()
        doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // sending
    ///////////////////////////////////////////////////////////////////////////


}
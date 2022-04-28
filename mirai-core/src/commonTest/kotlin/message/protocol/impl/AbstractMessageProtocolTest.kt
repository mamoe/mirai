/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.protocol.*
import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.structureToString
import net.mamoe.mirai.message.data.MessageChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.asserter

internal abstract class AbstractMessageProtocolTest : AbstractMockNetworkHandlerTest() {

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

    protected fun doEncoderChecks(
        expectedStruct: List<ImMsgBody.Elem>,
        protocol: MessageProtocol,
        encode: MessageProtocolFacade.() -> List<ImMsgBody.Elem>
    ) {
        assertEquals(
            expectedStruct,
            facadeOf(protocol).encode(),
            message = "Failed to check single Protocol"
        )
        assertEquals(
            expectedStruct,
            MessageProtocolFacade.INSTANCE.encode(),
            message = "Failed to check with all protocols"
        )
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    private fun <@kotlin.internal.OnlyInputTypes T> assertEquals(
        expected: List<T>,
        actual: List<T>,
        message: String? = null
    ) {
        if (expected.size == 1 && actual.size == 1) {
            asserter.assertEquals(message, expected.single().structureToString(), actual.single().structureToString())
        } else {
            asserter.assertEquals(
                message,
                expected.joinToString { it.structureToString() },
                actual.joinToString { it.structureToString() })
        }
    }

    protected fun doDecoderChecks(
        expectedChain: MessageChain,
        protocol: MessageProtocol,
        decode: MessageProtocolFacade.() -> MessageChain
    ) {
        assertEquals(
            expectedChain.toList(),
            facadeOf(protocol).decode().toList(),
            message = "Failed to check single Protocol"
        )
        assertEquals(
            expectedChain.toList(),
            MessageProtocolFacade.INSTANCE.decode().toList(),
            message = "Failed to check with all protocols"
        )
    }

    protected fun doEncoderChecks(
        expectedStruct: ImMsgBody.Elem,
        protocol: MessageProtocol,
        encode: MessageProtocolFacade.() -> List<ImMsgBody.Elem>
    ): Unit = doEncoderChecks(mutableListOf(expectedStruct), protocol, encode)
}
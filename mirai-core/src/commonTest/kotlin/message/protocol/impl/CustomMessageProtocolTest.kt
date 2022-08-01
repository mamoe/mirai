/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.CustomMessage
import net.mamoe.mirai.message.data.CustomMessageMetadata
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class CustomMessageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(CustomMessageProtocol(), TextProtocol())

    @BeforeTest
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @BeforeTest
    fun init() {
        MyCustomMessage(1) // register
    }

    @Serializable
    class MyCustomMessage(
        val int: Int,
    ) : CustomMessageMetadata(), ProtoBuf {
        override fun getFactory(): Factory = Factory

        object Factory : CustomMessage.Factory<MyCustomMessage>("MyCustomMessage") {
            override fun dump(message: MyCustomMessage): ByteArray {
                return message.toByteArray(serializer())
            }

            override fun load(input: ByteArray): MyCustomMessage {
                return input.loadAs(serializer())
            }
        }
    }

    @Test
    fun `test encode`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customElem = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomElem(
                        data = "00 00 00 17 08 01 12 0F 4D 79 43 75 73 74 6F 6D 4D 65 73 73 61 67 65 1A 02 08 01".hexToBytes(),
                        enumType = 103904510,
                    ),
                )
            )
            message(MyCustomMessage(1))
        }.doEncoderChecks()
    }

    @Test
    fun `test decode`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customElem = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomElem(
                        data = "00 00 00 17 08 01 12 0F 4D 79 43 75 73 74 6F 6D 4D 65 73 73 61 67 65 1A 02 08 01".hexToBytes(),
                        enumType = 103904510,
                    ),
                )
            )
            message(MyCustomMessage(1))
        }.doEncoderChecks()
    }

// not supported, see https://github.com/mamoe/mirai/issues/2144
//    @TestFactory
//    fun `test serialization`(): DynamicTestsResult {
//        val data = MyCustomMessage(1)
//        val serialName = "CustomMessage"
//        return runDynamicTests(
//            testPolymorphicInMessageMetadata(data, serialName),
//            testPolymorphicInSingleMessage(data, serialName),
//            testInsideMessageChain(data, serialName),
//            testContextual(data),
//        )
//    }
}
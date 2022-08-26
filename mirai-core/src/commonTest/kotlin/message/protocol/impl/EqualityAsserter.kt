/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.structureToStringIfAvailable
import kotlin.test.assertNotNull
import kotlin.test.asserter

internal interface EqualityAsserter {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    fun <@kotlin.internal.OnlyInputTypes T> assertEquals(
        expected: List<T>,
        actual: List<T>,
        message: String? = null
    )

    object Ordinary : EqualityAsserter {
        override fun <T> assertEquals(expected: List<T>, actual: List<T>, message: String?) {
            if (expected.size == actual.size) {
                if (expected.size == 1 && expected.singleOrNull() == actual.singleOrNull()) {
                    return asserter.assertEquals(message, expected.single(), actual.single())
                }

                if (expected.zip(actual).all { (e, a) -> e == a }) return

                asserter.assertEquals(message, expected, actual)
            } else {
                asserter.assertEquals(message, expected, actual)
            }
        }
    }

    object Structural : EqualityAsserter {
        override fun <T> assertEquals(expected: List<T>, actual: List<T>, message: String?) {
            if (expected.size == 1 && actual.size == 1) {
                val e = expected.single()
                val a = actual.single()
                if (a == null || e == null) {
                    asserter.assertEquals(
                        "[Null] $message",
                        structureToStringOrOrdinaryString(e),
                        structureToStringOrOrdinaryString(a)
                    )
                    assertNotNull(a, message)
                    assertNotNull(e, message)
                }
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                if (!e!!::class.isInstance(a) && !a!!::class.isInstance(e)) {
                    asserter.assertEquals(
                        "[Incompatible type] $message",
                        structureToStringOrOrdinaryString(e),
                        structureToStringOrOrdinaryString(a)
                    )
                    return
                }
                asserter.assertEquals(
                    message,
                    structureToStringOrOrdinaryString(e),
                    structureToStringOrOrdinaryString(a)
                )
            } else {
                asserter.assertEquals(
                    message,
                    expected.joinToString { structureToStringOrOrdinaryString(it) },
                    actual.joinToString { structureToStringOrOrdinaryString(it) })
            }
        }

        private val json = Json {
            isLenient = true
            prettyPrint = true
            serializersModule = MessageSerializers.serializersModule
        }

        private fun <T> structureToStringOrOrdinaryString(value: T): String {
            if (value == null) return "null"
            val valueNotNull: T & Any = value
            @Suppress("UNCHECKED_CAST")
            return valueNotNull.structureToStringIfAvailable()
            // fallback for native
                ?: kotlin.run {
                    if (valueNotNull is SingleMessage) {
                        messageChainOf(valueNotNull).serializeToJsonString(json) // use the stable serialization approach
                    } else json.encodeToString(
                        when (valueNotNull) {
                            is ImMsgBody.Elem -> ImMsgBody.Elem.serializer()
                            else -> error("Unsupported type: $valueNotNull")
                        } as KSerializer<Any>, valueNotNull
                    )
                }
        }
    }

    object OrdinaryThenStructural : EqualityAsserter {
        override fun <T> assertEquals(expected: List<T>, actual: List<T>, message: String?) {
            try {
                Ordinary.assertEquals(expected, actual, message)
                return
            } catch (e: AssertionError) {
                Structural.assertEquals(expected, actual, message)
                return
            }
        }

    }
}
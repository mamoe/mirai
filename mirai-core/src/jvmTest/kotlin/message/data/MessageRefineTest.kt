/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.AbstractTestWithMiraiImpl
import net.mamoe.mirai.internal.MiraiImpl
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.OfflineMessageSourceImplData
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.utils.PlatformLogger
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals


open class TM(private val name: String = Random.nextInt().toString()) : SingleMessage {
    override fun toString(): String = name
    override fun contentToString(): String = name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TM

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()
}

private val bot = MockBot()

private suspend fun testRefineAll(
    before: Message,
    after: MessageChain,
) {
    testRefineLight(before, after)
    testRefineDeep(before, after)
}

private suspend fun testRefineDeep(
    before: Message,
    after: MessageChain
) = assertEquals(after.toMessageChain(), before.toMessageChain().refineDeep(bot))

private fun testRefineLight(
    before: Message,
    after: MessageChain
) = assertEquals(after.toMessageChain(), before.toMessageChain().refineLight(bot))


@Suppress("TestFunctionName")
private fun RefinableMessage(
    refine: (bot: Bot, context: MessageChain) -> Message?
): RefinableMessage {
    return object : RefinableMessage, TM() {
        override fun tryRefine(bot: Bot, context: MessageChain): Message? {
            return refine(bot, context)
        }
    }
}

@Suppress("TestFunctionName")
private fun RefinableMessage0(
    refine: () -> Message?
): RefinableMessage {
    return object : RefinableMessage, TM() {
        override fun tryRefine(bot: Bot, context: MessageChain): Message? {
            return refine()
        }
    }
}

private object MiraiImplForRefineTest : MiraiImpl() {
    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
        return super.downloadForwardMessage(bot, resourceId)
    }
}

internal class MessageRefineTest : AbstractTestWithMiraiImpl() {

    @Test
    fun `can remove self`() = runBlockingUnit {
        testRefineAll(
            RefinableMessage0 { null },
            messageChainOf()
        )
    }

    @Test
    fun `can replace`() = runBlockingUnit {
        testRefineAll(
            RefinableMessage0 { TM("1") },
            messageChainOf(TM("1"))
        )
    }

    @Test
    fun `ignore non-refinable`() = runBlockingUnit {
        testRefineAll(
            TM("1"),
            messageChainOf(TM("1"))
        )
    }

    @Test
    fun `can replace flatten`() = runBlockingUnit {
        testRefineAll(
            buildMessageChain {
                +RefinableMessage0 { TM("1") + TM("2") }
                +TM("3")
                +RefinableMessage0 { TM("4") + TM("5") }
            },
            messageChainOf(TM("1"), TM("2"), TM("3"), TM("4"), TM("5"))
        )
    }

    private val testCases = object {
        /**
         * 单个 quote 包含 at 和 plain
         */
        val simpleQuote =
            decodeProto("087aea027708a2fc1010d285d8cc0418f9e7b4830620012a0d0a0b0a09e999a4e99d9e363438420a18aedd90f380808080014a480a2d08d285d8cc0410d285d8cc04185228a2fc1030f9e7b4830638aedd90f380808080014a0608d285d8cc04e001011a170a15120d0a0b0a09e999a4e99d9e36343812044a0208591a0a180a0740e9bb84e889b21a0d00010000000300499602d20000050a030a01201a0a180a0740e9bb84e889b21a0d00010000000300499602d20000070a050a032073624baa02489a0145080120cb507800c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021002900480c0829004b80400c00400ca0400f804dc8002880500044a0240011082010d0a076161617465737418012803")

        /**
         * 一个引用另一个 quote
         */
        val nestedQuote2 =
            decodeProto("0631ea022e08a4fc1010d285d8cc041885e8b4830620012a0e0a0c0a0a40e9bb84e889b2207362420a1896fee2d386808080011b0a190a0840616161746573741a0d00010000000800499602d20000080a060a04207878784baa02489a0145080120cb507800c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021003900480c0829004b80400c00400ca0400f804dc8002880500044a0240011082010d0a076161617465737418012803")

        /**
         * quote -> quote -> quote[at + plain]
         */
        val nestedQuote3 =
            decodeProto("062aea022708a6fc1010d285d8cc0418b0e8b4830620012a070a050a03787878420a18b584a7ca80808080011b0a190a0840616161746573741a0d00010000000800499602d200000a0a080a062061616161614baa02489a0145080120cb507800c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021003900480c0829004b80400c00400ca0400f804dc8002880500044a0240011082010d0a076161617465737418012803")

        /**
         * [Dice.value] 4
         */
        val dice4 =
            decodeProto("056432620a0e5be99a8fe69cbae9aab0e5ad905d1006180122104823d3adb15df08014ce5d6796b76ee128c85930033a103430396532613639623136393138663950c80158c8016211727363547970653f313b76616c75653d336a0a0a0608c80110c8014001120a100a0e5be99a8fe69cbae9aab0e5ad905d4baa02489a014508017800900101c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021003900480c0829004b80400c00400ca0400f804dc8002880500044a0240011082010d0a076161617465737418012803")

        /**
         * quote -> dice4
         */
        val quoteDice4 =
            decodeProto("06e601ea02e20108adfc1010d285d8cc04188feab4830620012a120a100a0e5be99a8fe69cbae9aab0e5ad905d420a1894bbc6f481808080014aad010a2d08d285d8cc0410d285d8cc04185228adfc10308feab483063894bbc6f481808080014a0608d285d8cc04e001011a7c0a7a125e325c0a0e5be99a8fe69cbae9aab0e5ad905d1006180122104823d3adb15df08014ce5d6796b76ee128c85930033a1034303965326136396231363931386639480050c80158c8016211727363547970653f313b76616c75653d336a02400112120a100a0e5be99a8fe69cbae9aab0e5ad905d12044a0208001b0a190a0840616161746573741a0d00010000000800499602d20000080a060a04206162634baa02489a0145080120cb507800c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021003900480c0829004b80400c00400ca0400f804dc8002880500044a0240011082010d0a076161617465737418012803")

        /**
         * forward[quote + dice + forward]
         */
        val complexForward =
            "044b0a3a08d285d8cc041852288f831130dfffb6830638a7c4dfde87808080014a0e08d285d8cc042206e7b289e889b2a2010b10b0f083f7fbffffffff011a0d0a0b12050a030a016112024a00dc010a3a08d285d8cc0418522891831130e4ffb68306388fc594dd85808080014a0e08d285d8cc042206e7b289e889b2a2010b10b1f083f7fbffffffff011a9d010a9a011270ea026d088f831110d285d8cc0418dfffb6830620012a050a030a0161420a18a7c4dfde87808080014a400a2d08d285d8cc0410d285d8cc041852288f831130dfffb6830638a7c4dfde87808080014a0608d285d8cc04e001011a0f0a0d12050a030a016112044a02080050d285d8cc04121a0a180a0740e7b289e889b21a0d00010000000300499602d2000012060a040a02207212024a00520a3a08d285d8cc0418522892831130f2ffb6830638a8e4d1eb80808080014a0e08d285d8cc042206e7b289e889b2a2010b10b2f083f7fbffffffff011a140a12120c0a0a0a085be9aab0e5ad905d12024a00700a3a08d285d8cc04185228978311308d80b7830638a0e8bfa582808080014a0e08d285d8cc042206e7b289e889b2a2010b10b3f083f7fbffffffff011a320a30122a0a280a265be59088e5b9b6e8bdace58f915de8afb7e58d87e7baa7e696b0e78988e69cace69fa5e79c8b12024a00"
                .let { s ->
                    ProtoBuf.decodeFromHexString<List<MsgComm.Msg>>(s).flatMap { it.msgBody.richText.elems }
                }

        private fun decodeProto(p: String) = ProtoBuf.decodeFromHexString<List<ImMsgBody.Elem>>(p)
    }

//    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
//        return super.downloadForwardMessage(bot, resourceId)
//    }

    /**
     * We cannot test LongMessage and MusicShare in unit tests (for now), but these tests will be sufficient
     */
    @Test
    fun `recursive refinement`() = runBlockingUnit {
        val map = listOf(
            RefineTest(testCases.simpleQuote) {
                expected {
                    +QuoteReply(sourceStub(buildMessageChain {
                        +"除非648"
                    }))
                    +At(1234567890) // sent by official client, redundant At?
                    +" "
                    +At(1234567890)
                    +" sb"
                }
                light()
                deep()
            },
            RefineTest(testCases.nestedQuote2) {
                expected {
                    +QuoteReply(sourceStub(buildMessageChain {
                        +"@黄色 sb" // this is sent by official client.
                    }))
                    +At(1234567890) // mentions self
                    +" xxx"
                }
                light()
                deep()
            },
            RefineTest(testCases.nestedQuote3) {
                expected {
                    +QuoteReply(sourceStub(buildMessageChain {
                        +"xxx" // official client does not handle nested quotes.
                    }))
                    +At(1234567890) // mentions self
                    +" aaaaa"
                }
                light()
                deep()
            },
            RefineTest(testCases.dice4) {
                expected {
                    +Dice(4)
                }
                light()
                deep()
            },
            RefineTest(testCases.quoteDice4) {
                expected {
                    +QuoteReply(sourceStub(PlainText("[随机骰子]")))
                    +At(1234567890)
                    +" abc"
                }
                light()
                deep()
            },
            RefineTest(testCases.complexForward) {
                expected {
                    +"a"
                    +QuoteReply(sourceStub(PlainText("a")))
                    +At(1234567890)
                    +" r"
                    +"[骰子]" // client does not support
                    +"[合并转发]请升级新版本查看" // client support but mirai does not.
                }
                deep() // deep only
            }
        )

        for (test in map) {
            if (test.testLight) {
                testRecursiveRefine(test.list, test.expected, true)
            }
            if (test.testDeep) {
                testRecursiveRefine(test.list, test.expected, false)
            }
        }
    }
}


private fun sourceStub(
    originalMessage: Message
): OfflineMessageSourceImplData {
    return OfflineMessageSourceImplData(
        MessageSourceKind.GROUP, intArrayOf(), bot.id, 0, 0, 0, originalMessage.toMessageChain(), intArrayOf()
    )
}

private suspend fun testRecursiveRefine(list: List<ImMsgBody.Elem>, expected: MessageChain, isLight: Boolean) {
    val actual = buildMessageChain {
        ReceiveMessageTransformer.joinToMessageChain(list, 0, MessageSourceKind.GROUP, bot, this)
    }.let { c ->
        if (isLight) {
            c.refineLight(bot)
        } else {
            c.refineDeep(bot)
        }
    }
    val color = object : PlatformLogger("") {
        val yellow get() = Color.LIGHT_YELLOW.toString()
        val green get() = Color.LIGHT_GREEN.toString()
        val reset get() = Color.RESET.toString()
    }

    fun compare(expected: MessageChain, actual: MessageChain): Boolean {
        if (expected.size != actual.size) return false
        for ((e, a) in expected.zip(actual)) {
            when (e) {
                is QuoteReply -> {
                    if (a !is QuoteReply) return false
                    if (!compare(e.source.originalMessage, a.source.originalMessage)) return false
                }
                is MessageSource -> {
                    if (a !is MessageSource) return false
                    if (!compare(e.originalMessage, a.originalMessage)) return false
                }
                else -> {
                    if (e != a) return false
                }
            }
        }
        return true
    }

    if (!compare(expected, actual))
        throw AssertionError(
            "\n" + """
                Expected  str:${color.green}${expected}${color.reset}
                Actual    str:${color.green}${actual}${color.reset}
                
                Expected json:${color.yellow}${expected.serializeToJsonString()}${color.reset}
                Actual   json:${color.yellow}${actual.serializeToJsonString()}${color.reset}
            """.trimIndent() + "\n"
        )
}

private class RefineTest(
    val list: List<ImMsgBody.Elem>,
) {
    lateinit var expected: MessageChain
    fun expected(chain: MessageChainBuilder.() -> Unit) {
        expected = buildMessageChain(chain)
    }

    var testLight: Boolean = false
    var testDeep: Boolean = false
    fun deep() {
        testDeep = true
    }

    fun light() {
        testLight = true
    }
}

@Suppress("TestFunctionName")
private fun RefineTest(list: List<ImMsgBody.Elem>, action: RefineTest.() -> Unit): RefineTest {
    return RefineTest(list).apply(action)
}
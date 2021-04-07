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
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.OfflineMessageSourceImplData
import net.mamoe.mirai.internal.message.ReceiveMessageTransformer
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
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

internal class MessageRefineTest {

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
         * forward[quote + dice + quote]
         */
        val complexForward =
            decodeProto(
                "048a046287040a820401789cad93cb6ed34014865fe5685878557c4b531bd9ae90a04aa40489245cdaaaaaa6f6d819341e07cf384dbaabc402c1a2b060531521a4b20071e98a0552799b38e95b30762a280b5875369ed1b1bff9cfff1f7beb9394c198e48266dcd7cc9b86068487594479e26b0f061b2b8e0642621e619671e26b532234580fbc542420483ea62169dff191bd8a409274c4b0accf26021cca8a89c694ec770b2669572408f6724a621f6d2f0e5fcc4e3f2ebe9ecdcedfec204877732268e4a3bc671c98518beb497cfbf1fdf1ddad0e379f440fe38215a3b8f7a8334c5b836cc8e57e6b337bdacdb6ac565b2f4c33343607aed39b742a544c19b98753e2a3a6db70aca6b36adbae6b396b6eb3e1289dfd22558a1188acc843a264b5d5c5068222673e4210339cd4751c2963fa34e17535bdec61a32e1b28f0a8ea18189e66855c765cbd2fa9b265972e8978f90c3c49252320e881126537140c4f3a9413e1230b0153bbfe0887aa665a2898ff3c55eecc8f9f5df5c8d36bc4df24ab8920cc5896fbe8c65abdfe4fc658c523e42d005cad6b666e5f1c1f95273f2e3e9dcdbebcdab96ec17be16fe23087218d22a272893113442529a78cd48eeb81278a34c5f9f4df5706e5bb0ff3939776f9f6fde2fcf3ece875f9fd7979f8cdd32fbf0c3cbd8a5691ea01015ecfd2d53410d0b09aed3f53ae76a35195f68a598bd0d50f12fc0252992eac102348aa02459a014208017800c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021003900480c0829004b80400c00400ca0400f804dc8002880500024a001082010d0a076161617465737418012803"
            )

        /**
         * forward[plain ]
         */
        val simpleForward =
            decodeProto(
                "04f10362ee030ae90301789c7d924f6fd33018c6bfca2b73c869b469bab4437126c136286b1123dda04268721327f570fe2876ba26b7491c101c10072e088490c6010901276ee3dbf4cfc7c071271807f0c5b65efbe7e779fc3adbb398c394e682a50936cceb4d0368e2a7014b226c1c0ef736ba0608499280f034a1d828a93060db75621181a0f994f9b4b78391b58940d238e344eabd8980f8b266a229a3a783824b3610118271ce6888d1e3d5d98bf9f9e7d5d7eff38b374f10c4c739152cc0e876afbcb575e40fc2f42422bb0777463be383cc7a74327b7014dd1c0d9ff687d5b823f633b1597a0fef979568b7abe290ec5783bba3497fd7ab5121e3f41e892946f656bbdbb2bb1dd3342ddbb6ecae5debf48a582b146991fb54c9eaa9879b088a9c638420e424528e94834005e3b128d1d5f8d2c39e2e3791eb30e5183829d342ae1dd7e72553b11cb33591ac67d7914c720a82554a94d5563032ebb3840a8c5a08b85a7919f155cd6c2177f9f35ca5b37cfbec6a464e4323fe26b56c047ecad31ca36b1d3dfe4f26447d8f903700c8d8ff4d9ce43061414095cd9070415530b2e4541b68b88e28e298e4e5bf9f74171f3e2ddfbd3417ef3fae2ebecc5fbd5efc78be38fbe6342e6fba4ea34e4a9174de90e8afb96a0e01f3eb56f9d3346a956575781ba616d150fde6fe02ea85f2d7102348aa02459a014208017800c80100f00100f80100900200c80200980300a00300b00300c00300d00300e803008a04021003900480c0829004b80400c00400ca0400f804dc8002880500024a001082010d0a076161617465737418012803"
            )

        private fun decodeProto(p: String) = ProtoBuf.decodeFromHexString<List<ImMsgBody.Elem>>(p)
    }

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
                    +QuoteReply(sourceStub(PlainText("[随机骰子]")))
                    +At(1234567890)
                    +" abc"
                }
                light()
                deep()
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
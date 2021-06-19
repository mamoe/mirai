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
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.utils.PlatformLogger
import net.mamoe.mirai.utils.autoHexToBytes
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
        override fun tryRefine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message? {
            return refine(bot, context)
        }
    }
}

@Suppress("TestFunctionName")
private fun RefinableMessage0(
    refine: () -> Message?
): RefinableMessage {
    return object : RefinableMessage, TM() {
        override fun tryRefine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message? {
            return refine()
        }
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

        val nestedForward =
            "0a790a3008d285d8cc041852200028f9263084f99c83064a1608d285d8cc04220ee69eabe790b3c2b7e99ba8e88eb9a2010210011a450a430a0618d5f0fbdf0412390a370a315be59088e5b9b6e8bdace58f915de8afb7e4bdbfe794a8e6898be69cba5151e69c80e696b0e78988e69cace69fa5e79c8b1a005a000a620a3f08d285d8cc041852200028fb2630f5f99c83064a2508d285d8cc04221de7bea4e59e83e59cbeefbc8ce697b6e4b88de697b6e69da5e8a2ab6763a2010210011a1f0a1d0a0618df98a9a10d12090a070a01351a005a0012084a060890e60260000a97010a3f08d285d8cc0418522000288a2730ef999d83064a2508d285d8cc04221de7bea4e59e83e59cbeefbc8ce697b6e4b88de697b6e69da5e8a2ab6763a2010210011a540a520a0618a19bc4fe09122fea022c08fb2610d285d8cc0418f5f99c830620012a050a030a01353001420a188fefa5ae8a8080800150d285d8cc04120d0a0b0a0561736566661a005a0012084a060890e602600012fe060a084d756c74694d736712f1060af0040a3008d285d8cc041852200028f9263084f99c83064a1608d285d8cc04220ee69eabe790b3c2b7e99ba8e88eb9a2010210011abb040ab8040a0618d5f0fbdf0412ad0462aa040aa30401789cbd535d6f1241147df7574cc6676497a5c036bb3408bbb608550a02d234cdb2ccc2da1db6ccccb2edbe359af8fdf5604c6cad8969134d8d9a98544d2c0ffe1617f0c9bfe080561b5f7c30f14e723373ef9c73ef99dc51e636b003fa8850dbedaa503c234080baa6dbb2bb6d157acc8aa4e05cfa14e0a660da064d62234b85cbe3ad5be1decbf1ebb7e1d1a31508f0aa653b68d1c0488545cf617691b6571389999ca8c96224a38952249ee43b5917e488248852327936abcb991c0486c9a6a5fb36f28fa110b0b287551883c0720cde88040145a46f9b6821c74f33c715cb76c02b8a02ef1aa48162338481636cba1ee351c823cc660e02a6ebb84485a785a971ae294c8a430e1a0df6b898d1936b272501253a45fe85219698300c770f460fde7d7eff75fbc5f8dec75950a15cc23f3164caf99a96af674dcbcad44c400d622d87db83d1cdeb2bffb933fe3ae1eed57067f0edd39de1e3c32f1fee723f7cba3f7e7ed0366741f55c3568cc2fe1a5b546bd12d3a592b0982b89eb976b58971af54ea715bbe417af14e58b55d6aaf17ca3baae558235bfd835850b755d0a7a3d9f90423320fd3c08923579a3d9d419958c8542b613b082ee5332df4f56b4780a8bbee9457b74be10b43cdfa71ea3e7b576aee4aabf0501a543d24a943ba0500f63836cfe52971226eb8ff779b63fdab93d3e7a15de7f383cbc31dc7ac329a23f919c233a19a8b4425d8f980874a7e37d724c202ff62339b9ccff47fa3b18253b9f10231a000a620a3f08d285d8cc041852200028fb2630f5f99c83064a2508d285d8cc04221de7bea4e59e83e59cbeefbc8ce697b6e4b88de697b6e69da5e8a2ab6763a2010210011a1f0a1d0a0618df98a9a10d12090a070a01351a005a0012084a060890e60260000a97010a3f08d285d8cc0418522000288a2730ef999d83064a2508d285d8cc04221de7bea4e59e83e59cbeefbc8ce697b6e4b88de697b6e69da5e8a2ab6763a2010210011a540a520a0618a19bc4fe09122fea022c08fb2610d285d8cc0418f5f99c830620012a050a030a01353001420a188fefa5ae8a8080800150d285d8cc04120d0a0b0a0561736566661a005a0012084a060890e602600012f3040a2d4d756c74694d73675f36363544314539312d414531332d343739312d394630392d33303133373742434639414412c1040a4e0a3108d285d8cc041852200028ffb20130fef89c83064a1608d285d8cc04220ee69eabe790b3c2b7e99ba8e88eb9a2010210011a190a170a061881f9d1ae02120d0a0b0a0554734d73671a005a000ab9010a3108d285d8cc041852200028ffb20130fff89c83064a1608d285d8cc04220ee69eabe790b3c2b7e99ba8e88eb9a2010210011a83010a80010a0618e3fde8a20b121b0a190a1341534a57454a58436366664157630a736172661a005a00125942571220463042383130354243463033374133323733413644343133453941333043393338b0bd858e0940b787ace70b485050005a0060006a10f0b8105bcf037a3273a6d413e9a30c93a00100b001ac02b8018302c801b85c0a4e0a3108d285d8cc041852200028ffb2013080f99c83064a1608d285d8cc04220ee69eabe790b3c2b7e99ba8e88eb9a2010210011a190a170a0618d1afffda02120d0a0b0a0554734d73671a005a000ae2010a4008d285d8cc041852200028ffb2013081f99c83064a2508d285d8cc04221de7bea4e59e83e59cbeefbc8ce697b6e4b88de697b6e69da5e8a2ab6763a2010210011a9d010a9a010a0618bfb1ebfb0f128f010a8c010a85015647567a5a48526d526b5a585432463351304e4451317059576d46335a586868643255774d6a4d3950567464573246335a567045547a6b774d6e63304f5846337a71717772724c627a72764a0a7a3757397862624674733361494c43687a744c46777372487637544534386d317763752f7173484c7a64757777737574734b456744516f3d1a005a00"
                .autoHexToBytes().loadAs(MsgTransmit.PbMultiMsgTransmit.serializer())

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
                    +PlainText(" r")
                    +PlainText("[骰子]") // client does not support
                    +PlainText("[合并转发]请升级新版本查看") // client support but mirai does not.
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

    @Test
    fun `test nested forward refinement`() = runBlockingUnit {
        val redefined = MiraiImpl.run { testCases.nestedForward.toForwardMessageNodes(bot) }
        assertNodesEquals(
            listOf(
                ForwardMessage.Node(
                    senderId = 1234567890,
                    time = 1617378436,
                    senderName = "枫琳·雨莹",
                    messageChain = buildMessageChain {
                        +redefined[0].messageChain[MessageOrigin]!!
                        val rf = redefined[0].messageChain[ForwardMessage]!!
                        +ForwardMessage(
                            preview = rf.preview,
                            title = rf.title,
                            brief = rf.brief,
                            source = rf.source,
                            summary = rf.summary,
                            nodeList = listOf(
                                ForwardMessage.Node(1234567890, 1617378430, "枫琳·雨莹", PlainText("TsMsg")),
                                ForwardMessage.Node(
                                    1234567890,
                                    1617378431,
                                    "枫琳·雨莹",
                                    PlainText("ASJWEJXCcffAWc\nsarf") + Image("{F0B8105B-CF03-7A32-73A6-D413E9A30C93}.mirai")
                                ),
                                ForwardMessage.Node(1234567890, 1617378432, "枫琳·雨莹", PlainText("TsMsg")),
                                ForwardMessage.Node(
                                    1234567890,
                                    1617378433,
                                    "群垃圾，时不时来被gc",
                                    PlainText("VGVzZHRmRkZXT2F3Q0NDQ1pYWmF3ZXhhd2UwMjM9PVtdW2F3ZVpETzkwMnc0OXF3zqqwrrLbzrvJ\nz7W9xbbFts3aILChztLFwsrHv7TE48m1wcu/qsHLzduwwsutsKEgDQo=")
                                ),
                            )
                        )
                    }
                ),
                ForwardMessage.Node(
                    1234567890, 1617378549, "群垃圾，时不时来被gc", PlainText("5")
                ),
                ForwardMessage.Node(
                    1234567890, 1617382639, "群垃圾，时不时来被gc", redefined[2].messageChain[QuoteReply]!! + PlainText("aseff")
                ),
            ),
            redefined,
        )
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
    assertMessageChainEquals(expected, actual)
}

private fun assertNodesEquals(excepted: List<ForwardMessage.Node>, actual: List<ForwardMessage.Node>) {
    assert(excepted.size == actual.size) { "Length not match" }
    for (i in excepted.indices) {
        val en = excepted[i]
        val an = actual[i]
        assertEquals(en.senderId, an.senderId)
        assertEquals(en.time, an.time)
        assertEquals(en.senderName, an.senderName)
        assertMessageChainEquals(en.messageChain, an.messageChain)
    }
}

private fun assertMessageChainEquals(expected: MessageChain, actual: MessageChain) {
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
                is ForwardMessage -> {
                    if (a !is ForwardMessage) return false
                    if (e.brief != a.brief) return false
                    if (e.summary != a.summary) return false
                    if (e.source != a.source) return false
                    if (e.title != a.title) return false
                    if (e.preview != a.preview) return false
                    assertNodesEquals(e.nodeList, a.nodeList)
                }
                is Image -> {
                    if (a !is Image) return false
                    if (e.imageId != a.imageId) return false
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
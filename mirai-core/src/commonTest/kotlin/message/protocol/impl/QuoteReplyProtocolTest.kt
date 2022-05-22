/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.decodeAndRefineLight
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.hexToBytes
import org.junit.jupiter.api.Test

internal class QuoteReplyProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(QuoteReplyProtocol(), TextProtocol())

    @Test
    fun `decode group reference group`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(1803),
                        senderUin = 1230001,
                        time = 1653147259,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                        ),
                        pbReserve = "18 AB 85 9D 81 82 80 80 80 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                ),
            )
//            message(
//                QuoteReply(
////                    OfflineMessageSourceImplData(
////
////                    )
//                )
//            )
            targetGroup()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    @Test
    fun `can decode`() {
        doDecoderChecks(
            messageChainOf(Face(Face.YIN_XIAN)),
        ) {
            decodeAndRefineLight(
                listOf(
                    net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                        face = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Face(
                            index = 108,
                            old = "14 AD".hexToBytes(),
                        ),
                    )
                ),
                groupIdOrZero = 0,
                MessageSourceKind.GROUP,
                bot,
            )
        }

    }

    private fun CodingChecksBuilder.targetGroup() {
        target(bot.addGroup(1, 1))
    }

    private fun CodingChecksBuilder.targetFriend() {
        target(bot.addFriend(1))
    }


}
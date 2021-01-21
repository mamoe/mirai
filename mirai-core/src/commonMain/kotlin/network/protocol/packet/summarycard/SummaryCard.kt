/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.summarycard

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.ReqHead
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestDataVersion2
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.internal.utils.io.serialization.readJceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.Tars
import net.mamoe.mirai.internal.utils.io.serialization.writeJceStruct
import net.mamoe.mirai.internal.utils.soutv
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.internal.network.protocol.data.jce.ReqSummaryCard as JceReqSummaryCard
import net.mamoe.mirai.internal.network.protocol.data.jce.RespSummaryCard as JceRespSummaryCard

internal object SummaryCard {
    internal object ReqSummaryCard : OutgoingPacketFactory<ReqSummaryCard.RespSummaryCard>(
        "SummaryCard.ReqSummaryCard"
    ) {
        internal class RespSummaryCard(
            override val nickname: String,
            override val email: String,
            override val age: Int,
            override val qLevel: Int,
            override val sex: UserProfile.Sex,
            override val sign: String,
        ) : Packet, UserProfile {
            override fun toString(): String {
                return "SummaryCard.RespSummaryCard(nickname=$nickname, email=$email, age=$age, qLevel=$qLevel, sex=$sex, sign=$sign)"
            }
        }

        operator fun invoke(
            client: QQAndroidClient,
            uin: Long,
        ): OutgoingPacket = buildOutgoingUniPacket(client) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    funcName = "ReqSummaryCard",
                    servantName = "SummaryCardServantObj",
                    version = 3,
                    sBuffer = jceRequestSBuffer {
                        "ReqHead"(ReqHead.serializer(), ReqHead(2))
                        "ReqSummaryCard"(
                            JceReqSummaryCard.serializer(), JceReqSummaryCard(
                                uin = uin,
                                eComeFrom = 31,
                                getControl = 69181,
                                eAddFriendSource = 3001,
                                vSecureSig = byteArrayOf(0x00),
                                reqMedalWallInfo = 0,
                                vReq0x5ebFieldId = listOf(
                                    27225, 27224, 42122, 42121, 27236,
                                    27238, 42167, 42172, 40324, 42284,
                                    42326, 42325, 42356, 42363, 42361,
                                    42367, 42377, 42425, 42505, 42488
                                ),
                                reqNearbyGodInfo = 1,
                                reqExtendCard = 1,
                            )
                        )
                    }
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RespSummaryCard {
            val fullMap = readJceStruct(
                RequestPacket.serializer()
            ).sBuffer.read {
                readJceStruct(RequestDataVersion2.serializer())
            }.map
            val map = fullMap["RespSummaryCard"] ?: error("Missing RespSummaryCard in response")
            val pck = map["SummaryCard.RespSummaryCard"]
                ?: map["SummaryCard_Old.RespSummaryCard"]
                ?: error("No Response found")
            val response = pck.read {
                discardExact(1)
                Tars.UTF_8.load(JceRespSummaryCard.serializer(), this)
            }
            return RespSummaryCard(
                nickname = response.nick ?: "",
                email = response.email ?: "",
                age = response.age?.let { it.toInt() and 0xFF } ?: -1,
                qLevel = response.iLevel ?: -1,
                sex = when (response.sex?.let { it.toInt() and 0xFF }) {
                    0 -> UserProfile.Sex.MALE
                    1 -> UserProfile.Sex.FEMALE
                    else -> UserProfile.Sex.UNKNOWN
                },
                sign = response.sign ?: ""
            )
        }
    }
}

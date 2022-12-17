/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import net.mamoe.mirai.internal.contact.CommonGroupImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetGroupMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetRoamMsgReq
import net.mamoe.mirai.internal.utils.indexFirstBE

internal class RoamingMessagesImplGroup(
    override val contact: CommonGroupImpl
) : TimeBasedRoamingMessagesImpl() {
    override suspend fun requestRoamMsg(
        timeStart: Long,
        lastMessageTime: Long,
        random: Long // unused field
    ): MessageSvcPbGetRoamMsgReq.Response {
        val lastMsgSeq = contact.bot.network.sendAndExpect(
            TroopManagement.GetGroupLastMsgSeq(
                client = contact.bot.client,
                groupUin = contact.uin
            )
        )
        return when (lastMsgSeq) {
            is TroopManagement.GetGroupLastMsgSeq.Response.Success -> {
                val results = mutableListOf<MsgComm.Msg>()
                var currentSeq = lastMsgSeq.seq

                while (true) {
                    if (currentSeq <= 0) break

                    val resp = contact.bot.network.sendAndExpect(
                        MessageSvcPbGetGroupMsg(
                            client = contact.bot.client,
                            groupUin = contact.uin,
                            messageSequence = currentSeq,
                            20 // maximum 20
                        )
                    )
                    if (resp is MessageSvcPbGetGroupMsg.Failed) break
                    if ((resp as MessageSvcPbGetGroupMsg.Success).msgElem.isEmpty()) break

                    // the message may be sorted increasing by message time,
                    // if so, additional sortBy will not take cost.
                    val msgElems = resp.msgElem.sortedBy { it.msgHead.msgTime }
                    results.addAll(0, msgElems)

                    val firstMsgElem = msgElems.first()
                    if (firstMsgElem.msgHead.msgTime < timeStart) {
                        break
                    } else {
                        currentSeq = (firstMsgElem.msgHead.msgSeq - 1).toLong()
                    }
                }

                // use binary search to find the first message that message time is lager than lastMessageTime
                var right = results.indexFirstBE(lastMessageTime) { it.msgHead.msgTime.toLong() }
                // check messages with same time
                if (results[right].msgHead.msgTime.toLong() == lastMessageTime) {
                    do {
                        right++
                    } while (right <= results.size - 1 && results[right].msgHead.msgTime <= lastMessageTime)
                }
                // loops at most 20 times, just traverse
                val left = results.indexOfFirst { it.msgHead.msgTime >= timeStart }

                MessageSvcPbGetRoamMsgReq.Response(
                    if (left == right) null else results.subList(left, right),
                    if (left == right) -1L else results[right - 1].msgHead.msgTime.toLong(), -1L, byteArrayOf()
                )
            }

            is TroopManagement.GetGroupLastMsgSeq.Response.Failed -> {
                MessageSvcPbGetRoamMsgReq.Response(null, -1L, -1L, byteArrayOf())
            }
        }
    }
}
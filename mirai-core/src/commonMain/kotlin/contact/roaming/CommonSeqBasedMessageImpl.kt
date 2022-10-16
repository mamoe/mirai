/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.contact.roaming.SeqBasedRoamingMessages
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.message.data.MessageChain

internal abstract class CommonSeqBasedMessageImpl : CommonRoamingMessagesImpl(), SeqBasedRoamingMessages {
    override suspend fun getMessagesIn(seq: Int, count: Int, filter: RoamingMessageFilter?): Flow<MessageChain> {
        return flow {
            val resp = getResp(seq, count)
            var i = 0;
            for (msg in resp.msg) {
                println("$i ${msg.toMessageChainOnline(contact.bot)}")
            }
        }
    }

    abstract fun getResp(seq: Int, count: Int): MsgSvc.PbGetGroupMsgResp
}
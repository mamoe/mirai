/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.decoders

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.getGroupByUin
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.NoticePipelineContext.Companion.KEY_MSG_INFO
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.components.SyncController.Companion.syncController
import net.mamoe.mirai.internal.network.components.syncOnlinePush
import net.mamoe.mirai.internal.network.notice.GroupAware
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack.SvcReqPushMsg
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.*

/**
 * Decodes [SvcReqPushMsg] to [MsgInfo] then re-fire [MsgType0x210] or [MsgType0x2DC]
 */
internal class MsgInfoDecoder(
    private val logger: MiraiLogger,
) : SimpleNoticeProcessor<SvcReqPushMsg>(type()) {
    override suspend fun NoticePipelineContext.processImpl(data: SvcReqPushMsg) {
        // SvcReqPushMsg is fully handled here, no need to set consumed.

        for (msgInfo in data.vMsgInfos) {
            decodeMsgInfo(msgInfo)
        }
    }

    private suspend fun NoticePipelineContext.decodeMsgInfo(data: MsgInfo) {
        if (!bot.syncController.syncOnlinePush(data)) return
        @Suppress("MoveVariableDeclarationIntoWhen") // for debug
        val id = data.shMsgType.toUShort().toInt()
        when (id) {
            // 528
            0x210 -> processAlso(data.vMsg.loadAs(MsgType0x210.serializer()), KEY_MSG_INFO to data)

            // 732
            0x2dc -> {
                data.vMsg.read {
                    val groupCode = readInt().toUInt().toLong()
                    val group = bot.getGroup(groupCode) ?: bot.getGroupByUin(groupCode)
                    ?: return // group has not been initialized
                    group.checkIsGroupImpl()

                    val kind = readByte().toInt()
                    discardExact(1)

                    processAlso(MsgType0x2DC(kind, group, this.readBytes()), KEY_MSG_INFO to data)
                }
            }
            else -> {
                logger.debug { "Unknown MsgInfo kind ${data.shMsgType.toInt()}, data=${data.vMsg.toUHexString()}" }
            }
        }
    }
}

internal interface BaseMsgType0x2DC<V> : GroupAware {
    val kind: Int
    override val group: GroupImpl
    val buf: V

    override val bot get() = group.bot
}

internal data class MsgType0x2DC(
    override val kind: Int, // inner kind, read from vMsg
    override val group: GroupImpl,
    override val buf: ByteArray,
) : ProtocolStruct, BaseMsgType0x2DC<ByteArray> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false

        if (kind != other.kind) return false
        if (group != other.group) return false
        if (!buf.contentEquals(other.buf)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kind
        result = 31 * result + group.hashCode()
        result = 31 * result + buf.contentHashCode()
        return result
    }
}
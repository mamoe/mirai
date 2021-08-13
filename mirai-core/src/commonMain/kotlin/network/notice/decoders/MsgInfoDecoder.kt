/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.decoders

import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.components.SyncController.Companion.syncController
import net.mamoe.mirai.internal.network.components.syncOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack.SvcReqPushMsg
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.toUHexString

/**
 * Decodes [SvcReqPushMsg] to [MsgInfo] then re-fire [MsgType0x210] or [MsgType0x2DC]
 */
internal class MsgInfoDecoder(
    private val logger: MiraiLogger,
) : SimpleNoticeProcessor<SvcReqPushMsg>(type()) {
    override suspend fun PipelineContext.processImpl(data: SvcReqPushMsg) {
        // SvcReqPushMsg is fully handled here, no need to set consumed.

        for (msgInfo in data.vMsgInfos) {
            decodeMsgInfo(msgInfo)
        }
    }

    private suspend fun PipelineContext.decodeMsgInfo(data: MsgInfo) {
        if (!bot.syncController.syncOnlinePush(data)) return
        when (data.shMsgType.toUShort().toInt()) {
            // 528
            0x210 -> processAlso(data.vMsg.loadAs(MsgType0x210.serializer()))

            // 732
            0x2dc -> {
                data.vMsg.read {
                    val group = bot.getGroup(readUInt().toLong()) ?: return // group has not been initialized
                    group.checkIsGroupImpl()

                    val kind = readByte().toInt()
                    discardExact(1)

                    processAlso(MsgType0x2DC(kind, group, this.readBytes()))
                }
            }
            else -> {
                logger.debug { "Unknown MsgInfo kind ${data.shMsgType.toInt()}, data=${data.vMsg.toUHexString()}" }
            }
        }
    }
}

internal interface BaseMsgType0x2DC<V> {
    val kind: Int
    val group: GroupImpl
    val buf: V

    fun Long.findMember() = group[this]
    fun String.findMember() = this.toLongOrNull()?.let { group[it] }
}

internal data class MsgType0x2DC(
    override val kind: Int, // inner kind, read from vMsg
    override val group: GroupImpl,
    override val buf: ByteArray,
) : ProtocolStruct, BaseMsgType0x2DC<ByteArray> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MsgType0x2DC

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
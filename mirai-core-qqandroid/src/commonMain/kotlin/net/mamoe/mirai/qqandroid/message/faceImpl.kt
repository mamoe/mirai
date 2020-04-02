package net.mamoe.mirai.qqandroid.message

import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.utils.hexToBytes
import net.mamoe.mirai.qqandroid.utils.toByteArray

internal val FACE_BUF = "00 01 00 04 52 CC F5 D0".hexToBytes()

internal fun Face.toJceData(): ImMsgBody.Face {
    return ImMsgBody.Face(
        index = this.id,
        old = (0x1445 - 4 + this.id).toShort().toByteArray(),
        buf = FACE_BUF
    )
}

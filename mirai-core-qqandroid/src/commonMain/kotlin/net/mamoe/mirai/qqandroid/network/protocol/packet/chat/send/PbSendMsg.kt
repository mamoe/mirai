package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.send

import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.MsgSvc


internal fun MessageChain.constructPbSendMsgReq(): MsgSvc.PbSendMsgReq {
    val request = MsgSvc.PbSendMsgReq()

    this.forEach {
        when (it) {
            is PlainText -> {
                request.msgBody.richText.elems.add(ImMsgBody.Elem(text = ImMsgBody.Text(str = it.stringValue)))
            }
            is At -> {

            }
        }
    }


    return request
}
/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

internal class OtherClientImpl(
    bot: Bot,
    coroutineContext: CoroutineContext,
    override val info: OtherClientInfo,
) : OtherClient, AbstractContact(bot, coroutineContext) {
    override suspend fun sendMessage(message: Message): MessageReceipt<OtherClient> {
        throw UnsupportedOperationException("OtherClientImpl.sendMessage is not yet supported.")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        throw UnsupportedOperationException("OtherClientImpl.uploadImage is not yet supported.")
    }

    override fun toString(): String {
        return "OtherClient(bot=${bot.id},deviceName=${info.deviceName},platform=${info.platform})"
    }
}

/*
contentHead=ContentHead#522561765 {
}
msgBody=MsgBody#-1622349855 {
        msgContent=08 04 12 1E 08 E9 07 10 B7 F7 8B 80 02 18 E9 07 20 00 28 DD F1 92 B7 07 30 DD F1 92 B7 07 48 02 50 03 32 1E 08 88 80 F8 92 CD 84 80 80 10 10 01 18 00 20 01 2A 0C 0A 0A 08 01 12 06 E5 95 8A E5 95 8A
        richText=RichText#-184909407 {
                elems=[]
        }
}
msgHead=MsgHead#1128220129 {
        authUin=0x0000000000000000(0)
        c2cCmd=0x00000007(7)
        cpid=0x0000000000000000(0)
        fromUin=0x0000000076E4B8DD(1994701021)
        isSrcMsg=false
        msgInstCtrl=InstCtrl#1220180502 {
                msgExcludeInst=[]
                msgFromInst=InstInfo#-1165404375 {
                        apppid=0x000003E9(1001)
                        enumDeviceType=0x00000002(2)
                        instid=0x2002FBB7(537066423)
                }
                msgSendToInst=[InstInfo#-1165404375 {
                        apppid=0x000003E9(1001)
                        enumDeviceType=0x00000003(3)
                }]
        }
        msgSeq=0x000073C8(29640)
        msgTime=0x5FE34926(1608730918)
        msgType=0x00000211(529)
        msgUid=0x0100000076360F0E(72057596021182222)
        toUin=0x0000000076E4B8DD(1994701021)
}
 */
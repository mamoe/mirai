package net.mamoe.mirai.network.protocol.tim.packet.action

import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.withSession
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.ImageLink
import net.mamoe.mirai.network.protocol.tim.packet.action.FriendImageIdDownloadLinkRequestPacket.ImageLinkResponse
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount


suspend fun QQ.getLink(image: Image): ImageLink = withSession {
    FriendImageIdDownloadLinkRequestPacket(bot.qqAccount, bot.sessionKey, id, image.id).sendAndExpect<ImageLinkResponse>().link
}

suspend inline fun QQ.downloadAsByteArray(image: Image): ByteArray = this.getLink(image).downloadAsByteArray()
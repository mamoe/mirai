package net.mamoe.mirai.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.handler.DataPacketSocket
import net.mamoe.mirai.utils.getGTK

/**
 * 一次会话. 当登录完成后, 客户端会拿到 sessionKey. 此时建立 session, 开始处理消息等事务
 *
 * @author Him188moe
 */
class LoginSession(
        val bot: Bot,
        val sessionKey: ByteArray,
        val socket: DataPacketSocket
) {
    lateinit var cookies: String
    var sKey: String = ""
        set(value) {
            field = value
            gtk = getGTK(value)
        }
    var gtk: Int = 0
}
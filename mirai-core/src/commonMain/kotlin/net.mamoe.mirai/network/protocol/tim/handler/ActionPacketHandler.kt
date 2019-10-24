@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.isOpen
import net.mamoe.mirai.network.protocol.tim.packet.RequestAccountInfoPacket
import net.mamoe.mirai.network.protocol.tim.packet.ResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.RequestSKeyPacket
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.utils.log

/**
 * 动作: 获取好友列表, 点赞, 踢人等.
 * 处理动作事件, 承担动作任务.
 *
 * @author Him188moe
 */
class ActionPacketHandler(session: BotSession) : PacketHandler(session) {
    companion object Key : PacketHandler.Key<ActionPacketHandler>


    private var sKeyRefresherJob: Job? = null


    @ExperimentalStdlibApi
    override suspend fun onPacketReceived(packet: ServerPacket) = with(session) {
        when (packet) {
            //is AddFriendPacket.Response -> {
            //    this.uploadImageSessions.forEach {
            //        it.onPacketReceived(packet)
            //    }
            //}

            is RequestSKeyPacket.Response -> {
                sKey = packet.sKey
                cookies = "uin=o$qqAccount;skey=$sKey;"


                if (sKeyRefresherJob?.isActive != true) {
                    sKeyRefresherJob = NetworkScope.launch {
                        while (isOpen) {
                            delay(1800000)
                            try {
                                requestSKey()
                            } catch (e: Throwable) {
                                e.log()
                            }
                        }
                    }
                }
            }

            is ServerEventPacket.Raw.Encrypted -> socket.distributePacket(packet.decrypt(sessionKey))
            is ServerEventPacket.Raw -> socket.distributePacket(packet.distribute())
            is ResponsePacket.Encrypted<*> -> socket.distributePacket(packet.decrypt(sessionKey))

            else -> {
            }
        }
    }

    private suspend fun requestSKey() = with(session) {
        withContext(NetworkScope.coroutineContext) {
            socket.sendPacket(RequestSKeyPacket())
        }
    }


    suspend fun requestAccountInfo() = with(session) {
        withContext(NetworkScope.coroutineContext) {
            socket.sendPacket(RequestAccountInfoPacket(qqAccount, sessionKey))
        }
    }

    override fun close() {
        this.sKeyRefresherJob?.cancel()
        this.sKeyRefresherJob = null
    }

}
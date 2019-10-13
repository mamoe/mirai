package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.action.AddFriendResult
import net.mamoe.mirai.network.protocol.tim.packet.action.ClientAddFriendPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ClientCanAddFriendPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerCanAddFriendResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.login.ClientSKeyRefreshmentRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.ClientSKeyRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.ServerSKeyResponsePacket
import net.mamoe.mirai.utils.getGTK
import net.mamoe.mirai.utils.hexToBytes

/**
 * 动作: 获取好友列表, 点赞, 踢人等.
 * 处理动作事件, 承担动作任务.
 *
 * @author Him188moe
 */
class ActionPacketHandler(session: LoginSession) : PacketHandler(session) {
    private val addFriendSessions = mutableListOf<AddFriendSession>()
    private val uploadImageSessions = mutableListOf<UploadImageSession>()

    private var sKeyRefresherJob: Job? = null


    @ExperimentalStdlibApi
    override suspend fun onPacketReceived(packet: ServerPacket) {
        when (packet) {
            is ServerCanAddFriendResponsePacket -> {
                this.uploadImageSessions.forEach {
                    it.onPacketReceived(packet)
                }
            }
            is ServerTryGetImageIDSuccessPacket -> {
                // ImageNetworkUtils.postImage(packet.uKey.toUHexString(), )
            }

            is ServerTryGetImageIDFailedPacket -> {

            }

            is ServerTryGetImageIDResponsePacket.Encrypted -> session.socket.distributePacket(packet.decrypt(session.sessionKey))

            is ServerAccountInfoResponsePacket.Encrypted -> session.socket.distributePacket(packet.decrypt(session.sessionKey))
            is ServerAccountInfoResponsePacket -> {

            }

            is ServerSKeyResponsePacket.Encrypted -> session.socket.distributePacket(packet.decrypt(session.sessionKey))
            is ServerSKeyResponsePacket -> {
                session.sKey = packet.sKey
                session.cookies = "uin=o" + session.bot.account.qqNumber + ";skey=" + session.sKey + ";"


                sKeyRefresherJob = session.scope.launch {
                    while (session.isOpen) {
                        delay(1800000)
                        session.socket.sendPacket(ClientSKeyRefreshmentRequestPacket(session.bot.account.qqNumber, session.sessionKey))
                    }
                }

                session.gtk = getGTK(session.sKey)
            }

            is ServerEventPacket.Raw.Encrypted -> session.socket.distributePacket(packet.decrypt(session.sessionKey))
            is ServerEventPacket.Raw -> session.socket.distributePacket(packet.distribute())

            else -> {
            }
        }
    }

    //@JvmSynthetic
    suspend fun addFriend(qqNumber: Long, message: Lazy<String> = lazyOf("")): CompletableDeferred<AddFriendResult> {
        val future = CompletableDeferred<AddFriendResult>()
        val session = AddFriendSession(qqNumber, future, message)
        //  uploadImageSessions.add(session)
        session.sendAddRequest()
        return future
    }


    suspend fun requestSKey() {
        session.socket.sendPacket(ClientSKeyRequestPacket(session.bot.account.qqNumber, session.sessionKey))
    }


    suspend fun requestAccountInfo() {
        session.socket.sendPacket(ClientAccountInfoRequestPacket(session.bot.account.qqNumber, session.sessionKey))
    }

    override fun close() {
        this.sKeyRefresherJob?.cancel()
        this.sKeyRefresherJob = null
    }

    private inner class UploadImageSession(
            private val group: Long,
            private val future: CompletableDeferred<AddFriendResult>
            //private val image: BufferedImage
    ) {
        lateinit var id: ByteArray


        fun onPacketReceived(packet: ServerPacket) {
            if (!::id.isInitialized) {
                return
            }

            when (packet) {
                is ServerCanAddFriendResponsePacket -> {
                    if (!(packet.idByteArray[2] == id[0] && packet.idByteArray[3] == id[1])) {
                        return
                    }

                    when (packet.state) {
                        ServerCanAddFriendResponsePacket.State.FAILED -> {
                            future.complete(AddFriendResult.FAILED)
                            close()
                        }

                        ServerCanAddFriendResponsePacket.State.ALREADY_ADDED -> {
                            future.complete(AddFriendResult.ALREADY_ADDED)
                            close()
                        }

                        ServerCanAddFriendResponsePacket.State.REQUIRE_VERIFICATION -> {
                            //           session.socket.sendPacket(ClientAddFriendPacket(session.bot.account.qqNumber, qq, session.sessionKey))
                        }

                        ServerCanAddFriendResponsePacket.State.NOT_REQUIRE_VERIFICATION -> {

                        }
                    }
                }


            }
        }

        fun sendRequest() {

        }

        fun close() {
            uploadImageSessions.remove(this)
        }
    }

    private inner class AddFriendSession(
            private val qq: Long,
            private val future: CompletableDeferred<AddFriendResult>,
            private val message: Lazy<String>
    ) {
        lateinit var id: ByteArray


        suspend fun onPacketReceived(packet: ServerPacket) {
            if (!::id.isInitialized) {
                return
            }

            when (packet) {
                is ServerCanAddFriendResponsePacket -> {
                    if (!(packet.idByteArray.contentEquals(id))) {
                        return
                    }

                    when (packet.state) {
                        ServerCanAddFriendResponsePacket.State.FAILED -> {
                            future.complete(AddFriendResult.FAILED)
                            close()
                        }

                        ServerCanAddFriendResponsePacket.State.ALREADY_ADDED -> {
                            future.complete(AddFriendResult.ALREADY_ADDED)
                            close()
                        }

                        ServerCanAddFriendResponsePacket.State.REQUIRE_VERIFICATION -> {
                            session.socket.sendPacket(ClientAddFriendPacket(session.bot.account.qqNumber, qq, session.sessionKey))
                        }

                        ServerCanAddFriendResponsePacket.State.NOT_REQUIRE_VERIFICATION -> {

                        }
                    }
                }


            }
        }


        suspend fun sendAddRequest() {
            session.socket.sendPacket(ClientCanAddFriendPacket(session.bot.account.qqNumber, qq, session.sessionKey).also { this.id = it.idHex.hexToBytes() })
        }

        fun close() {
            //         uploadImageSessions.remove(this)
        }
    }
}
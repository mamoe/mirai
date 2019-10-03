package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.packet.ClientAccountInfoRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerAccountInfoResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.AddFriendResult
import net.mamoe.mirai.network.protocol.tim.packet.action.ClientAddFriendPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ClientCanAddFriendPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerCanAddFriendResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.image.ServerTryGetImageIDFailedPacket
import net.mamoe.mirai.network.protocol.tim.packet.image.ServerTryGetImageIDResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.image.ServerTryGetImageIDSuccessPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.ClientSKeyRefreshmentRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.ClientSKeyRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.ServerSKeyResponsePacket
import net.mamoe.mirai.task.MiraiThreadPool
import net.mamoe.mirai.utils.getGTK
import java.awt.image.BufferedImage
import java.io.Closeable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * 动作: 获取好友列表, 点赞, 踢人等.
 * 处理动作事件, 承担动作任务.
 *
 * @author Him188moe
 */
class ActionPacketHandler(session: LoginSession) : PacketHandler(session) {
    private val addFriendSessions = Collections.synchronizedCollection(mutableListOf<AddFriendSession>())
    private val uploadImageSessions = Collections.synchronizedCollection(mutableListOf<UploadImageSession>())

    private var sKeyRefresherFuture: ScheduledFuture<*>? = null


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

                sKeyRefresherFuture = MiraiThreadPool.instance.scheduleWithFixedDelay({
                    runBlocking {
                        session.socket.sendPacket(ClientSKeyRefreshmentRequestPacket(session.bot.account.qqNumber, session.sessionKey))
                    }
                }, 1800000, 1800000, TimeUnit.MILLISECONDS)

                session.gtk = getGTK(session.sKey)
            }

            is ServerEventPacket.Raw.Encrypted -> session.socket.distributePacket(packet.decrypt(session.sessionKey))
            is ServerEventPacket.Raw -> session.socket.distributePacket(packet.distribute())

            else -> {
            }
        }
    }


    suspend fun addFriend(qqNumber: Long, message: Supplier<String>) {
        addFriend(qqNumber, lazy { message.get() })
    }


    @JvmSynthetic
    suspend fun addFriend(qqNumber: Long, message: Lazy<String> = lazyOf("")): CompletableFuture<AddFriendResult> {
        val future = CompletableFuture<AddFriendResult>()
        val session = AddFriendSession(qqNumber, future, message)
        //  uploadImageSessions.add(session)
        session.sendAddRequest();
        return future
    }


    suspend fun requestSKey() {
        session.socket.sendPacket(ClientSKeyRequestPacket(session.bot.account.qqNumber, session.sessionKey))
    }


    suspend fun requestAccountInfo() {
        session.socket.sendPacket(ClientAccountInfoRequestPacket(session.bot.account.qqNumber, session.sessionKey))
    }

    override fun close() {
        this.sKeyRefresherFuture?.cancel(true)
        this.sKeyRefresherFuture = null
    }

    private inner class UploadImageSession(
            private val group: Long,
            private val future: CompletableFuture<AddFriendResult>,
            private val image: BufferedImage
    ) : Closeable {
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

        override fun close() {
            uploadImageSessions.remove(this)
        }
    }

    private inner class AddFriendSession(
            private val qq: Long,
            private val future: CompletableFuture<AddFriendResult>,
            private val message: Lazy<String>
    ) : Closeable {
        lateinit var id: ByteArray


        suspend fun onPacketReceived(packet: ServerPacket) {
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
                            session.socket.sendPacket(ClientAddFriendPacket(session.bot.account.qqNumber, qq, session.sessionKey))
                        }

                        ServerCanAddFriendResponsePacket.State.NOT_REQUIRE_VERIFICATION -> {

                        }
                    }
                }


            }
        }


        suspend fun sendAddRequest() {
            session.socket.sendPacket(ClientCanAddFriendPacket(session.bot.account.qqNumber, qq, session.sessionKey).also { this.id = it.packetIdLast })
        }

        override fun close() {
            //         uploadImageSessions.remove(this)
        }
    }
}
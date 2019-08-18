package net.mamoe.mirai.network

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.bytes.ByteArrayDecoder
import io.netty.handler.codec.bytes.ByteArrayEncoder
import net.mamoe.mirai.network.packet.client.ClientPacket
import net.mamoe.mirai.network.packet.client.login.*
import net.mamoe.mirai.network.packet.client.writeHex
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginResponseFailedPacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginResponseResendPacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginResponseSucceedPacket
import net.mamoe.mirai.network.packet.server.login.ServerLoginResponseVerificationCodePacket
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacket
import net.mamoe.mirai.util.getRandomKey
import net.mamoe.mirai.utils.MiraiLogger
import java.net.DatagramPacket
import java.net.InetSocketAddress

/**
 * [number] is a QQ number.
 *
 * @author Him188moe @ Mirai Project
 */
class Robot(val number: Int, private val password: String) {
    private var sequence: Int = 0

    private var channel: Channel? = null

    private lateinit var token00BA: ByteArray
    private lateinit var token0825: ByteArray
    private var loginTime: Int = 0
    private lateinit var loginIP: String
    private var tgtgtKey: ByteArray? = null

    @ExperimentalUnsignedTypes
    private var md5_32: ByteArray = getRandomKey(32)


    @ExperimentalUnsignedTypes
    private fun onPacketReceived(packet: ServerPacket) {
        packet.decode()
        when (packet) {
            is ServerTouchResponsePacket -> {
                if (packet.serverIP != null) {//redirection
                    connect(packet.serverIP!!)
                    sendPacket(ClientServerRedirectionPacket(
                            serverIP = packet.serverIP!!,
                            qq = number
                    ))
                } else {//password submission
                    this.loginIP = packet.loginIP
                    this.loginTime = packet.loginTime
                    this.token0825 = packet.token
                    this.tgtgtKey = packet.tgtgtKey
                    sendPacket(ClientPasswordSubmissionPacket(
                            qq = this.number,
                            password = this.password,
                            loginTime = packet.loginTime,
                            loginIP = packet.loginIP,
                            token0825 = packet.token,
                            tgtgtKey = packet.tgtgtKey
                    ))
                }
            }

            is ServerLoginResponseFailedPacket -> {
                channel = null
                println("Login failed: " + packet.state.toString())
                return
            }

            is ServerLoginResponseVerificationCodePacket -> {
                //[token00BA]可能来自这里
                this.token00BA = packet.token00BA
                if (packet.unknownBoolean != null && packet.unknownBoolean!!) {
                    this.sequence = 1
                    sendPacket(ClientLoginVerificationCodePacket(
                            qq = this.number,
                            token0825 = this.token0825,
                            token00BA = this.token00BA,
                            sequence = this.sequence
                    ))
                }

            }

            is ServerLoginResponseSucceedPacket -> {

            }

            //这个有可能是客户端发送验证码之后收到的回复验证码是否正确?
            is ServerLoginResponseResendPacket -> {
                if (packet.flag == ServerLoginResponseResendPacket.Flag.`08 36 31 03`) {
                    this.tgtgtKey = packet.tgtgtKey
                    sendPacket(ClientLoginResendPacket3104(
                            tgtgtKey = packet.tgtgtKey,
                            token00BA = packet.token,
                            qq = this.number,
                            password = this.password,
                            loginIP = this.loginIP,
                            loginTime = this.loginTime,
                            token0825 = this.token0825
                    ))
                } else {
                    sendPacket(ClientLoginResendPacket3106(
                            tgtgtKey = packet.tgtgtKey,
                            token00BA = packet.token,
                            qq = this.number,
                            password = this.password,
                            loginIP = this.loginIP,
                            loginTime = this.loginTime,
                            token0825 = this.token0825
                    ))
                }
            }

            else -> throw IllegalStateException()
        }

    }

    @ExperimentalUnsignedTypes
    private fun sendPacket(packet: ClientPacket) {
        packet.encode()
        packet.writeHex(Protocol.tail)
        channel!!.writeAndFlush(DatagramPacket(packet.toByteArray()))
    }

    companion object {
        private fun DatagramPacket(toByteArray: ByteArray): DatagramPacket = DatagramPacket(toByteArray, toByteArray.size)
    }

    @ExperimentalUnsignedTypes
    @Throws(InterruptedException::class)
    fun connect(host: String, port: Int = 8000) {
        val group = NioEventLoopGroup()
        try {
            val b = Bootstrap()

            b.group(group)
                    .channel(NioSocketChannel::class.java)
                    .remoteAddress(InetSocketAddress(host, port))
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        @Throws(Exception::class)
                        override fun initChannel(ch: SocketChannel) {
                            println("connected server...")
                            ch.pipeline().addLast(ByteArrayEncoder())
                            ch.pipeline().addLast(ByteArrayDecoder())
                            ch.pipeline().addLast(object : SimpleChannelInboundHandler<ByteArray>() {
                                override fun channelRead0(ctx: ChannelHandlerContext, bytes: ByteArray) {
                                    try {
                                        /*val remaining = Reader.read(bytes);
                                        if (Reader.isPacketAvailable()) {
                                            robot.onPacketReceived(Reader.toServerPacket())
                                            Reader.init()
                                            remaining
                                        }*/
                                        this@Robot.onPacketReceived(ServerPacket.ofByteArray(bytes, tgtgtKey))
                                    } catch (e: Exception) {
                                        MiraiLogger.catching(e)
                                    }
                                }

                                override fun channelActive(ctx: ChannelHandlerContext) {
                                    println("Successfully connected to server")
                                }

                                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                    MiraiLogger.catching(cause)
                                }
                            })
                        }
                    })

            channel = b.connect().sync().channel()
            channel!!.closeFuture().sync()
        } finally {
            group.shutdownGracefully().sync()
        }
    }
}
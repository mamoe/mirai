package net.mamoe.mirai.network

import io.netty.channel.Channel
import net.mamoe.mirai.network.packet.client.ClientPacket
import net.mamoe.mirai.network.packet.client.login.*
import net.mamoe.mirai.network.packet.client.writeHex
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.login.*
import net.mamoe.mirai.network.packet.server.security.ServerSessionKeyResponsePacket
import net.mamoe.mirai.network.packet.server.security.ServerSessionKeyResponsePacketEncrypted
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacket
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacketEncrypted
import net.mamoe.mirai.util.getRandomKey
import net.mamoe.mirai.util.toHexString
import net.mamoe.mirai.utils.MiraiLogger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * A robotNetworkHandler account.
 *
 * @author Him188moe
 */
class RobotNetworkHandler(val number: Int, private val password: String) {
    private var sequence: Int = 0

    private var channel: Channel? = null

    var serverIP: String = ""
        set(value) {
            serverAddress = InetSocketAddress(value, 8000)
            field = value
        }

    private lateinit var serverAddress: InetSocketAddress

    private lateinit var token00BA: ByteArray
    private lateinit var token0825: ByteArray
    private var loginTime: Int = 0
    private lateinit var loginIP: String
    private var tgtgtKey: ByteArray? = null

    /**
     * Kind of key, similar to sessionKey
     */
    private lateinit var tlv0105: ByteArray
    private lateinit var sessionKey: ByteArray
    /**
     * Kind of key, similar to sessionKey
     */
    private lateinit var _0828_rec_decr_key: ByteArray

    @ExperimentalUnsignedTypes
    private var md5_32: ByteArray = getRandomKey(32)


    @ExperimentalUnsignedTypes
    private fun onPacketReceived(packet: ServerPacket) {
        packet.decode()
        println("Packet received: $packet")
        when (packet) {
            is ServerTouchResponsePacket -> {
                if (packet.serverIP != null) {//redirection
                    serverIP = packet.serverIP!!
                    //connect(packet.serverIP!!)
                    sendPacket(ClientServerRedirectionPacket(packet.serverIP!!, number))
                } else {//password submission
                    this.loginIP = packet.loginIP
                    this.loginTime = packet.loginTime
                    this.token0825 = packet.token0825
                    this.tgtgtKey = packet.tgtgtKey
                    sendPacket(ClientPasswordSubmissionPacket(this.number, this.password, packet.loginTime, packet.loginIP, packet.tgtgtKey, packet.token0825))
                }
            }

            is ServerLoginResponseFailedPacket -> {
                channel = null
                println("Login failed: " + packet.state.toString())
                return
            }

            is ServerLoginResponseVerificationCodePacket -> {
                //[token00BA]来源之一: 验证码
                this.token00BA = packet.token00BA
                if (packet.unknownBoolean != null && packet.unknownBoolean!!) {
                    this.sequence = 1
                    sendPacket(ClientLoginVerificationCodePacket(this.number, this.token0825, this.sequence, this.token00BA))
                }

            }

            is ServerLoginResponseSuccessPacket -> {
                this._0828_rec_decr_key = packet._0828_rec_decr_key
                sendPacket(ClientLoginSucceedConfirmationPacket(this.number, this.serverIP, this.loginIP, this.md5_32, packet.token38, packet.token88, packet.encryptionKey, this.tlv0105))
            }

            //是ClientPasswordSubmissionPacket之后服务器回复的
            is ServerLoginResponseResendPacket -> {
                if (packet.token00BA != null) {
                    this.token00BA = packet.token00BA!!
                    println(token00BA)
                }
                if (packet.flag == ServerLoginResponseResendPacket.Flag.`08 36 31 03`) {
                    this.tgtgtKey = packet.tgtgtKey
                    sendPacket(ClientLoginResendPacket3104(this.number, this.password, this.loginTime, this.loginIP, this.tgtgtKey!!, this.token0825, this.token00BA, packet._0836_tlv0006_encr))
                } else {
                    sendPacket(ClientLoginResendPacket3106(this.number, this.password, this.loginTime, this.loginIP, this.tgtgtKey!!, this.token0825, this.token00BA, packet._0836_tlv0006_encr))
                }
            }

            is ServerSessionKeyResponsePacket -> {
                this.sessionKey = packet.sessionKey
                this.tlv0105 = packet.tlv0105
            }

            is ServerLoginResponseResendPacketEncrypted -> onPacketReceived(packet.decrypt(this.tgtgtKey!!))
            is ServerLoginResponseSuccessPacketEncrypted -> onPacketReceived(packet.decrypt(this.tgtgtKey!!))
            is ServerSessionKeyResponsePacketEncrypted -> onPacketReceived(packet.decrypt(this._0828_rec_decr_key))
            is ServerTouchResponsePacketEncrypted -> onPacketReceived(packet.decrypt())

            else -> throw IllegalArgumentException(packet.toString())
        }

    }

    @ExperimentalUnsignedTypes
    fun sendPacket(packet: ClientPacket) {
        try {
            MiraiLogger log "Encoding"
            packet.encode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        packet.writeHex(Protocol.tail)
        /*val p = DatagramPacket(packet.toByteArray());
        p.socketAddress = this.serverAddress*/
        //ctx.writeAndFlush(packet.toByteArray()).sync()
        MiraiLogger info "Sending: $packet"
        //GlobalScope.launch {
            send(packet.toByteArray())
        //}
        //println(channel!!.writeAndFlush(packet.toByteArray()).channel().connect(serverAddress).sync().get())
    }

    //  private val socket = DatagramSocket(15314)

    @ExperimentalUnsignedTypes
    fun send(data: ByteArray) {
        try {
            val socket = DatagramSocket((15314 + Math.random() * 10).toInt())
            socket.connect(this.serverAddress)

            val dp1 = DatagramPacket(ByteArray(22312), 22312)
            socket.send(DatagramPacket(data, data.size))
            MiraiLogger info "Packet sent: ${data.toUByteArray().toHexString()}"
            socket.receive(dp1)
            val zeroByte: Byte = 0
            var i = dp1.data.size - 1;
            while (dp1.data[i] == zeroByte) {
                --i
            }
            socket.close()
            onPacketReceived(ServerPacket.ofByteArray(dp1.data.copyOfRange(0, i + 1)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    /*
private lateinit var ctx: ChannelHandlerContext
    @ExperimentalUnsignedTypes
    @Throws(InterruptedException::class)
    fun connect(ip: String) {
        this.serverIP = ip


         NioDatagramConnector().let { it.handler = object : IoHandlerAdapter(), IoHandler {

         } }
        IoConnector connector=udpClient.getConnector();
        connector.getFilterChain().addLast("codec",
                 ProtocolCodecFilter(
                         TextLineCodecFactory(
                                Charset.forName("UTF-8"),
                LineDelimiter.WINDOWS.getValue(),
                LineDelimiter.WINDOWS.getValue())));

        ConnectFuture connectFuture=connector.connect(udpClient.getInetSocketAddress());
        // 等待是否连接成功，相当于是转异步执行为同步执行。
        connectFuture.awaitUninterruptibly();
        //连接成功后获取会话对象。如果没有上面的等待，由于connect()方法是异步的，
        //connectFuture.getSession(),session可能会无法获取。
        udpClient.setSession(connectFuture.getSession());
        udpClient.getSession().write("Hello，UDPServer!");

        val group = NioEventLoopGroup()
        try {
            val b = Bootstrap()

            MiraiLogger.info("Connecting")
            b.group(group)
                    .channel(NioDatagramChannel::class.java)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(object : ChannelInitializer<NioDatagramChannel>() {

                        override fun channelActive(ctx: ChannelHandlerContext?) {
                            this@RobotNetworkHandler.ctx = ctx!!
                            super.channelActive(ctx)
                        }

                        @Throws(Exception::class)
                        override fun initChannel(ch: NioDatagramChannel) {
                            ch.pipeline().addLast(ByteArrayDecoder())
                            ch.pipeline().addLast(ByteArrayEncoder())

                            ch.pipeline().addLast(object : SimpleChannelInboundHandler<ByteArray>() {
                                override fun channelRead0(ctx: ChannelHandlerContext, bytes: ByteArray) {
                                    try {
                                        this@RobotNetworkHandler.onPacketReceived(ServerPacket.ofByteArray(bytes))
                                    } catch (e: Exception) {
                                        MiraiLogger.catching(e)
                                    }
                                }

                                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                    MiraiLogger.catching(cause)
                                }
                            })

                            ch.pipeline().addLast(object : SimpleChannelInboundHandler<DatagramPacket>() {
                                override fun channelRead0(ctx: ChannelHandlerContext, bytes: DatagramPacket) {
                                    try {
                                        this@RobotNetworkHandler.onPacketReceived(ServerPacket.ofByteArray(bytes.data))
                                    } catch (e: Exception) {
                                        MiraiLogger.catching(e)
                                    }
                                }

                                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                    MiraiLogger.catching(cause)
                                }
                            })
                        }
                    })

            channel = b.bind(15345).sync().channel()

            MiraiLogger info "Succeed"
            sendPacket(ClientTouchPacket(this@RobotNetworkHandler.number, serverIP))
            channel!!.closeFuture().sync()
        } finally {
            group.shutdownGracefully().sync()
        }
    }*/
}

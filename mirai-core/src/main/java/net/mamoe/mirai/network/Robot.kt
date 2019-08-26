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
import net.mamoe.mirai.util.*
import net.mamoe.mirai.utils.MiraiLogger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*

/**
 * A robot account.
 *
 * @author Him188moe
 */
class Robot(val number: Int, private val password: String) {
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
                if (packet.tokenUnknown != null) {
                    //this.token00BA = packet.token00BA!!
                    //println("token00BA changed!!! to " + token00BA.toUByteArray())
                }
                if (packet.flag == ServerLoginResponseResendPacket.Flag.`08 36 31 03`) {
                    this.tgtgtKey = packet.tgtgtKey
                    sendPacket(ClientLoginResendPacket3104(
                            this.number,
                            this.password,
                            this.loginTime,
                            this.loginIP,
                            this.tgtgtKey!!,
                            this.token0825,
                            when (packet.tokenUnknown != null) {
                                true -> packet.tokenUnknown!!
                                false -> this.token00BA
                            },
                            packet._0836_tlv0006_encr
                    ))
                } else {
                    sendPacket(ClientLoginResendPacket3106(
                            this.number,
                            this.password,
                            this.loginTime,
                            this.loginIP,
                            this.tgtgtKey!!,
                            this.token0825,
                            when (packet.tokenUnknown != null) {
                                true -> packet.tokenUnknown!!
                                false -> this.token00BA
                            },
                            packet._0836_tlv0006_encr
                    ))
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
        sendPacketDebug(packet);
        if (true) return;
        try {
            //MiraiLogger log "Encoding"
            packet.encode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        packet.writeHex(Protocol.tail)
        /*val p = DatagramPacket(packet.toByteArray());
        p.socketAddress = this.serverAddress*/
        //ctx.writeAndFlush(packet.toByteArray()).sync()
        MiraiLogger info "Sending: $packet"
        packet.logging()
        packet.toByteArray().packetSentDebugLogging()
        //GlobalScope.launch {
        send(packet.toByteArray())
        //}
        //println(channel!!.writeAndFlush(packet.toByteArray()).channel().connect(serverAddress).sync().get())
    }

    val packetQueue: MutableList<String> = LinkedList()

    init {
        packetQueue.add("02 37 13 08 25 31 01 76 E4 B8 DD 00 00 00 13 46 E6 ED CE BF 3B EC 4C A0 B9 0C 84 D5 88 5C DE 99 7A 64 CF CF 59 35 C5 58 AE BD 0B F5 94 65 25 9E 23 01 88 BF D5 B5 51 DC AA 45 C5 97 8E 40 14 0F 46 50 31 CF 1C CB 10 28 33 F7 40 93 A9 B0 F3 EA 18 51 14 FD 61 C3 FD E3 A3 D8 1B 20 6F 60 EA 47 28 91 87 94 2E 5A E4 0A 4D 4D B7 14 57 03 E7 9D D7 28 E2 F4 59 62 34 89 19 1E 10 B8 90 97 C3 11 8A FE D6 3A 9A D8 03")
        packetQueue.add("02 37 13 08 25 31 02 76 E4 B8 DD 00 00 00 80 96 0C 13 5F 40 31 75 D6 13 3D 59 65 F0 7A 1E 03 22 0B F1 BE F4 D8 BB 46 6C 2E 55 39 A1 05 8B 85 4E A4 D4 F3 A9 D6 CC CA 4D 2E 1A F2 E4 1D 49 4D 1F 23 66 BB 60 92 17 EC D3 35 32 93 E2 3D 8A A0 CE 3C 83 66 1F 0A A0 9A C7 11 91 8E E5 06 54 09 DE 1D 88 6C 0F 16 EB BC 7B 95 BF 71 A2 71 2B 21 F8 AD C8 03 C0 54 DE 03")
        packetQueue.add("02 37 13 08 36 31 03 76 E4 B8 DD 00 00 00 BD 57 C9 45 45 E4 52 BE DA 05 67 A3 49 0F B7 90 AD 3E 47 34 A9 A8 B3 D9 82 E1 45 95 A4 41 F0 66 56 20 D5 0C B7 AF 9E A3 3A 32 FE 89 B2 0A AD 81 EC D0 D1 7A 17 00 51 5E FA BD 75 D1 DB E9 12 DC 89 25 A8 6D 80 F4 00 21 68 70 A0 77 E3 EF FA 9C 80 25 47 5B 55 E1 A0 1D D9 6B FE B7 7F 6A 3B 67 45 A5 F1 CE 33 F4 43 67 1D FD 83 F6 88 9F 2E 7E F3 8B 0E DE 68 76 B1 48 9A 5C B2 B2 8D 12 E3 FA CE 0F 22 F1 7C 20 4D AD 01 09 36 C6 64 3A BE CA 33 68 46 19 8A A9 66 7A 13 DC F2 EE 04 91 74 FB CB 57 B5 48 84 BF 99 24 3C 1E 5C 04 56 F1 28 E1 49 95 0D 71 39 FB A2 AE EE C5 E6 99 91 A3 A3 59 48 CA DE 10 66 F2 FA 88 D8 6F 46 2B B3 F4 33 C0 64 92 92 99 83 06 43 C2 3C FC 0F 34 38 7C 0F F8 3C 35 D0 CD 23 05 06 5B 61 B3 AD 38 D9 E2 5F 51 A4 A0 CA AF 4A A9 86 11 C9 AC 2C 44 11 08 52 E3 3C 0D 1B 91 B6 C6 70 FC 15 CC 16 F6 3B C9 97 C0 82 D1 8B 24 2F AA 35 50 61 E9 11 F8 E1 09 29 B9 20 5E 3A 73 33 BF 78 9C CC D0 A7 BF 23 66 65 3B D1 1F 71 40 C2 E2 0D CA 6F 57 D8 E1 46 B6 47 65 9E 43 04 0E 30 54 EB 70 42 49 6F 75 55 C5 63 3A A4 9D FF 0B F8 56 3B 89 74 14 56 6B 6E 9D 32 D4 DD FA E7 C6 B2 6B 61 F0 54 EF 05 7E E1 49 D6 38 A0 C1 B3 F6 B4 7A 4A 03 31 1D E7 88 BA 56 9D 50 03 95 FF DB 23 DC 3C B9 51 1B 4B 06 1E 5E C5 B1 96 EA 8B 64 92 48 24 65 A4 92 EC BA 90 42 AD BA 04 81 4F 42 FB 41 60 E9 93 68 1F 59 67 57 57 5F 40 22 1B F2 D9 C5 5D 53 34 2C E4 82 ED D2 A8 3B F1 C2 05 2A 4D F9 45 63 21 E7 92 5A 01 D4 A4 3A 98 D8 57 39 34 D4 E2 CC F6 D1 76 12 76 00 A5 89 18 66 9C F9 18 31 52 E6 92 B2 11 46 73 8D 37 92 99 7A 3B FC 82 36 A1 7A 7B 91 D0 F7 59 C2 64 76 7A 4F 7E 88 8A AF 11 AA 90 5C 0D E2 9F F7 A8 9B 04 A7 05 48 EC 92 01 2A 19 0E 8C A7 1B 9C 1F B2 F8 BD C5 AE 98 D5 86 C7 C6 D2 D5 BC B5 BB D7 F9 05 52 F1 5A 6D B5 94 2C 44 86 11 A9 B3 EB 9D D7 30 BF 21 1F 22 2D FB AC 0C 5C 94 C4 69 C2 82 C8 48 6C 86 40 95 EF 67 9B B1 60 17 09 56 AE CB 85 EF FD 60 7D BA A3 1D 13 05 10 93 ED 5D 91 6B 3B 8C 23 C4 45 EF 02 BA 86 0E F7 8E 46 C7 3D 07 8A 67 94 3B 5C 4B 05 BD 64 76 DF 1A 3B A5 C9 26 AA F6 A5 36 4E EC 00 AD D8 B7 5E 32 53 02 9F CF 3C 23 9C 94 BB 03 F8 97 9F 53 CC A0 68 77 4D A4 DE D0 CE DE 68 FC A2 07 A5 9E 65 28 E2 A2 95 E0 1D 45 11 47 E9 03 1A BE F5 1F 48 36 37 B8 EA EA 6B 9C 73 93 7D 21 CA 77 F7 62 73 BF BA 54 BB C2 38 0C 04 68 A4 E0 05 98 18 6E 5D EC 40 EE 54 27 9C 67 5C 79 5D 89 3C 4F DC 29 50 46 87 D9 EB F9 12 03")
    }


    @ExperimentalUnsignedTypes
    fun sendPacketDebug(packet: ClientPacket) {
        try {
            //MiraiLogger log "Encoding"
            packet.encode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        packet.writeHex(Protocol.tail)
        /*val p = DatagramPacket(packet.toByteArray());
        p.socketAddress = this.serverAddress*/
        //ctx.writeAndFlush(packet.toByteArray()).sync()
        MiraiLogger info "Sending: $packet"
        packet.logging()
        packet.toByteArray().packetSentDebugLogging()
        //GlobalScope.launch {
        this.onPacketReceived(ServerPacket.ofByteArray(packetQueue.removeAt(0).hexToBytes()))
        //send(packet.toByteArray())
        //}a
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
            MiraiLogger info "Packet sent: ${data.toUByteArray().toUHexString()}"
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
            repeat(100) { println() }
            println(DebugLogger.buff.toString())
            System.exit(1)
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
                            this@Robot.ctx = ctx!!
                            super.channelActive(ctx)
                        }

                        @Throws(Exception::class)
                        override fun initChannel(ch: NioDatagramChannel) {
                            ch.pipeline().addLast(ByteArrayDecoder())
                            ch.pipeline().addLast(ByteArrayEncoder())

                            ch.pipeline().addLast(object : SimpleChannelInboundHandler<ByteArray>() {
                                override fun channelRead0(ctx: ChannelHandlerContext, bytes: ByteArray) {
                                    try {
                                        this@Robot.onPacketReceived(ServerPacket.ofByteArray(bytes))
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
                                        this@Robot.onPacketReceived(ServerPacket.ofByteArray(bytes.data))
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
            sendPacket(ClientTouchPacket(this@Robot.number, serverIP))
            channel!!.closeFuture().sync()
        } finally {
            group.shutdownGracefully().sync()
        }
    }*/
}

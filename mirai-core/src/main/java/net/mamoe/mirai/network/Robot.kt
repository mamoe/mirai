package net.mamoe.mirai.network

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.bytes.ByteArrayDecoder
import io.netty.handler.codec.bytes.ByteArrayEncoder
import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.client.Client0825ResponsePacket
import net.mamoe.mirai.network.packet.server.Server0825Packet
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.utils.MiraiLogger
import java.net.InetSocketAddress

/**
 * [number] is a QQ number.
 *
 * @author Him188moe @ Mirai Project
 */
class Robot(val number: Int) {
    private lateinit var ctx: ChannelHandlerContext

    internal fun onPacketReceived(packet: Packet) {
        if (packet !is ServerPacket) {
            return
        }

        packet.decode()
        if (packet is Server0825Packet) {//todo 检查是否需要修改 UDP 连接???
            sendPacket(Client0825ResponsePacket(packet.serverIP, number))
        }
    }

    private fun sendPacket(packet: Packet) {

    }

    @Throws(InterruptedException::class)
    fun connect(host: String, port: Int) {
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
                                        this@Robot.ctx = ctx
                                        /*val remaining = Reader.read(bytes);
                                        if (Reader.isPacketAvailable()) {
                                            robot.onPacketReceived(Reader.toServerPacket())
                                            Reader.init()
                                            remaining
                                        }*/
                                        this@Robot.onPacketReceived(ServerPacket.ofByteArray(bytes))
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

            val cf = b.connect().sync()

            cf.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully().sync()
        }
    }

    private object Reader {
        private var length: Int? = null
        private lateinit var bytes: ByteArray

        fun init(bytes: ByteArray) {
            this.length = bytes.size
            this.bytes = bytes
        }

        /**
         * Reads bytes, combining them to create a packet, returning remaining bytes.
         */
        fun read(bytes: ByteArray): ByteArray? {
            checkNotNull(this.length)
            val needSize = length!! - this.bytes.size//How many bytes we need
            if (needSize == bytes.size || needSize > bytes.size) {
                this.bytes += bytes
                return null
            }

            //We got more than we need
            this.bytes += bytes.copyOfRange(0, needSize)
            return bytes.copyOfRange(needSize, bytes.size - needSize)//We got remaining bytes, that is of another packet
        }

        fun isPacketAvailable() = this.length == this.bytes.size

        fun toServerPacket(): ServerPacket {
            return ServerPacket.ofByteArray(this.bytes)
        }
    }
}
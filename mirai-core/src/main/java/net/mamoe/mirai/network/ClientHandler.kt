package net.mamoe.mirai.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import lombok.extern.log4j.Log4j2
import net.mamoe.mirai.MiraiServer
import net.mamoe.mirai.network.packet.server.ServerPacket

/**
 * @author Him188moe @ Mirai Project
 */
@Log4j2
class ClientHandler(val robot: Robot) : SimpleChannelInboundHandler<ByteArray>() {
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
            val needSize = length!! - this.bytes.size;//How many bytes we need
            if (needSize == bytes.size || needSize > bytes.size) {
                this.bytes += bytes
                return null;
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

    override fun channelRead0(ctx: ChannelHandlerContext?, bytes: ByteArray) {
        try {
            /*val remaining = Reader.read(bytes);
            if (Reader.isPacketAvailable()) {
                robot.onPacketReceived(Reader.toServerPacket())
                Reader.init()
                remaining
            }*/
            robot.onPacketReceived(ServerPacket.ofByteArray(bytes))
        } catch (e: Exception) {
            MiraiServer.getLogger().catching(e)
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("Successfully connected to server")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        MiraiServer.getLogger().catching(cause)
    }
}
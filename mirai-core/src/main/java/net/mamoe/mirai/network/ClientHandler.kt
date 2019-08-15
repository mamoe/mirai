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
    override fun channelRead0(ctx: ChannelHandlerContext?, bytes: ByteArray?) {
        try {
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
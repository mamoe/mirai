package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.mamoe.mirai.network.packet.client.ClientPacket;

import java.net.InetSocketAddress;
import java.util.List;

public class UDPPacketSender {
    private final EventLoopGroup group;

    private final Channel channel;

    public UDPPacketSender(InetSocketAddress address) throws InterruptedException {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new LogEventEncoder(address));
        channel = bootstrap.bind(0).sync().channel();
    }

    private static final class LogEventEncoder extends MessageToMessageEncoder<ClientPacket> {
        private final InetSocketAddress remoteAddress;

        private LogEventEncoder(InetSocketAddress remoteAddress) {
            this.remoteAddress = remoteAddress;
        }


        @Override
        protected void encode(ChannelHandlerContext ctx, ClientPacket packet, List<Object> out) {
            var buffer = ctx.alloc().buffer();
            buffer.writeBytes(packet.toByteArray());
            out.add(new DatagramPacket(buffer, remoteAddress));
        }
    }

    private void sendPacket(ClientPacket packet) {
        channel.writeAndFlush(packet);
    }

    public void stop() {
        group.shutdownGracefully();
    }
}

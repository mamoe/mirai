package net.mamoe.mirai.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MiraiUDPServer {
    public MiraiUDPServer() {
        log.info("creating server");
        new Thread(() -> {
            Bootstrap b = new Bootstrap();
            EventLoopGroup group = new NioEventLoopGroup();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
                                throws Exception {
                            // 读取收到的数据
                            ByteBuf buf = packet.copy().content();
                            byte[] req = new byte[buf.readableBytes()];
                            buf.readBytes(req);
                            String body = new String(req, CharsetUtil.UTF_8);
                            System.out.println("【NOTE】>>>>>> 收到客户端的数据：" + body);

                            // 回复一条信息给客户端
                            ctx.writeAndFlush(new DatagramPacket(
                                    Unpooled.copiedBuffer("Hello，我是Server，我的时间戳是" + System.currentTimeMillis()
                                            , CharsetUtil.UTF_8)
                                    , packet.sender())).sync();
                        }
                    });

            // 服务端监听在9999端口
            try {
                b.bind(9999).sync().channel().closeFuture().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        log.info("created server");
    }
}

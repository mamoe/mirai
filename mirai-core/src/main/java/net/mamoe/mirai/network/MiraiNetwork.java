package net.mamoe.mirai.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.Getter;
import net.mamoe.mirai.MiraiServer;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;

import java.io.IOException;
import java.net.ServerSocket;

public class MiraiNetwork {

    private static ServerBootstrap server;
    private static Thread thread;

    @Getter
    private static volatile Throwable lastError = null;

    public static void start(int port){
        thread =  new Thread(() -> {
            if (server != null) {
                throw new RuntimeException("there is already a ServerBootstrap instance");
            }

            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                server = new ServerBootstrap();
                server.group(bossGroup, workerGroup);
                server.channel(NioServerSocketChannel.class);
                //b.option(ChannelOption.SO_BACKLOG, 100);
                //b.handler(new LoggingHandler(LogLevel.INFO));
                server.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("bytesDecoder", new ByteArrayDecoder());
                        pipeline.addLast("bytesEncoder", new ByteArrayEncoder());
                        pipeline.addLast("handler", new NetworkPacketHandler());
                    }
                });


                server.bind(port).sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                lastError = e;
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
        thread.start();
        MiraiServer.getInstance().getEventManager().onEvent(ServerDisableEvent.class).setHandler(a -> {
            thread.interrupt();
        });
    }


    public static int getAvailablePort() throws IOException {
        ServerSocket serverSocket =  new ServerSocket(0); //读取空闲的可用端口
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }



}

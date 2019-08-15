package net.mamoe.mirai.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class MiraiNetwork {
    public static void connect(String host, int port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("connected server...");
                            ch.pipeline().addLast(new ByteArrayEncoder());
                            ch.pipeline().addLast(new ByteArrayDecoder());
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });

            ChannelFuture cf = b.connect().sync();

            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }


    public static int getAvailablePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0); //读取空闲的可用端口
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }


}

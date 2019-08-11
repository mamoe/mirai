package net.mamoe.mirai.network;

import io.netty.bootstrap.ServerBootstrap;
import lombok.Getter;


import java.io.IOException;
import java.net.ServerSocket;

public class MiraiNetwork {


    @Getter
    private static volatile Throwable lastError = null;

    public static void start(int port){

    }


    public static int getAvailablePort() throws IOException {
        ServerSocket serverSocket =  new ServerSocket(0); //读取空闲的可用端口
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }



}

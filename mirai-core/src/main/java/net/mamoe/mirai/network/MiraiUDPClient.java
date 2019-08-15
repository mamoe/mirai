package net.mamoe.mirai.network;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

@Log4j2
public class MiraiUDPClient {
    private DatagramSocket localUDPSocket;
    private Thread thread;

    public MiraiUDPClient(InetAddress target, int targetPort, int localPort) {
        log.info("creating client");
        try{
            this.localUDPSocket = new DatagramSocket(localPort);
            this.localUDPSocket.connect(target,targetPort);
            this.localUDPSocket.setReuseAddress(true);
            this.thread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] data = new byte[1024];
                        // 接收数据报的包
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        DatagramSocket localUDPSocket = MiraiUDPClient.this.localUDPSocket;
                        if ((localUDPSocket == null) || (localUDPSocket.isClosed())) continue;
                        // 阻塞直到收到数据
                        localUDPSocket.receive(packet);
                        // 解析服务端发过来的数据
                        MiraiUDPClient.this.onReceive(packet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            this.thread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        log.info("created client");
    }

    public void onReceive(DatagramPacket packet){
        System.out.println(new String(packet.getData(), 0 , packet.getLength(), StandardCharsets.UTF_8));
    }

    public void send(DatagramPacket packet) throws IOException {
        this.localUDPSocket.send(packet);
    }
}


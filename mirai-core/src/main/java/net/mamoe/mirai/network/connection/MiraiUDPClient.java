package net.mamoe.mirai.network.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.mamoe.mirai.utils.MiraiLogger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP Client
 * Try to keep long-alive UDP connection in order to improve performance
 *
 */
public class MiraiUDPClient {

    private LocalUDPListener listener;

    public MiraiUDPClient(InetAddress address, short serverPort, short localPort){
        this.listener = new LocalUDPListener(new LocalUDPSocketProvider(
                address,localPort,serverPort,null
        ));
    }
}


@AllArgsConstructor
class LocalUDPSocketProvider
{
    private InetAddress address;
    private short localPort;
    private short serverPort;

    @Getter
    private DatagramSocket socket = null;



    public void initSocket()
    {
        try
        {
            this.socket = new DatagramSocket(this.localPort);
            this.socket.connect(this.address, this.serverPort);
            this.socket.setReuseAddress(true);
        }
        catch (Exception e)
        {
            MiraiLogger.INSTANCE.catching(e);
        }
    }
}



class LocalUDPListener
{
    private Thread thread = null;

    protected LocalUDPSocketProvider provider;

    public LocalUDPListener(LocalUDPSocketProvider provider){
        this.provider = provider;
        if(this.provider.getSocket() == null){
            this.provider.initSocket();
        }
    }

    public void startup()
    {
        this.thread = new Thread(() -> {
            try
            {
                LocalUDPListener.this.listener();
            }
            catch (Exception e)
            {
                MiraiLogger.INSTANCE.catching(e);
            }
        });
        this.thread.start();
    }

    private void listener() throws Exception
    {
        while (true)
        {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            DatagramSocket localUDPSocket = this.provider.getSocket();

            if ((localUDPSocket == null) || (localUDPSocket.isClosed()))
                continue;

            localUDPSocket.receive(packet);

            //todo use CALLBACK
            String pFromServer = new String(packet.getData(), 0 , packet.getLength(), "UTF-8");
            System.out.println("【NOTE】>>>>>> 收到服务端的消息："+pFromServer);
        }
    }
}
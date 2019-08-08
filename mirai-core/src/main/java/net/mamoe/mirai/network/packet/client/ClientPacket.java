package net.mamoe.mirai.network.packet.client;

import net.mamoe.mirai.network.packet.Packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Him188moe @ Mirai Project
 */
public abstract class ClientPacket extends DataOutputStream implements Packet {
    public ClientPacket() {
        super(new ByteArrayOutputStream());
    }

    private final byte packageId;

    {
        packageId = 0x0058;
    }

    protected void writeHead() throws IOException {
        this.writeByte(0x02);
    }

    public static void main(String[] args) throws IOException {
        var pk = new ClientPacket() {
            @Override
            public void encode() throws IOException {
                writeHead();
            }
        };
        pk.encode();
        System.out.println(Arrays.toString(((ByteArrayOutputStream) pk.out).toByteArray()));
    }

    protected void writeVersion() throws IOException {
        this.writeByte(0x37_13);
    }

    protected void writePacketId() {

    }

    protected void writeQQ(long qq) throws IOException {
        this.writeLong(qq);
    }


    /**
     * Encode this packet
     */
    public abstract void encode() throws IOException;
}

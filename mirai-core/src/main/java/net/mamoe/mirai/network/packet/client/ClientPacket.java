package net.mamoe.mirai.network.packet.client;

import lombok.Getter;
import net.mamoe.mirai.network.packet.Packet;
import net.mamoe.mirai.network.packet.PacketId;

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

    @Getter
    private final int packageId;

    {
        var annotation = this.getClass().getAnnotation(PacketId.class);
        packageId = annotation.value();

        try {
            writeHead();
            writeVersion();
            writePacketId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    protected void writeHead() throws IOException {
        this.writeByte(0x02);
    }

    protected void writeVersion() throws IOException {
        this.writeByte(0x37_13);
    }

    protected void writePacketId() throws IOException {
        this.writeByte(this.packageId);
    }

    protected void writeFixVer() throws IOException {
        this.writeByte(0x03);
        this.writeByte(0x00);
        this.writeByte(0x00);
        this.writeByte(0x00);
        this.writeByte(0x01);
        this.writeByte(0x2E);
        this.writeByte(0x01);
        this.writeByte(0x00);
        this.writeByte(0x00);
        this.writeByte(0x68);
        this.writeByte(0x52);
        this.writeByte(0x00);
        this.writeByte(0x00);
        this.writeByte(0x00);
        this.writeByte(0x00);
    }

    protected void write0825Key() throws IOException {
        this.writeLong(0xA4_F1_91_88);
        this.writeLong(0xC9_82_14_99);
        this.writeLong(0x0C_9E_56_55);
        this.writeLong(0x91_23_C8_3D);
    }

    protected void writeQQ(long qq) throws IOException {
        this.writeLong(qq);
    }


    /**
     * Encode this packet
     */
    public abstract void encode() throws IOException;
}

package net.mamoe.mirai.network.packet.client;

import lombok.Getter;
import net.mamoe.mirai.network.Protocol;
import net.mamoe.mirai.network.packet.Packet;
import net.mamoe.mirai.network.packet.PacketId;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
            this.writeHex(Protocol.head);
            this.writeHex(Protocol.ver);
            writePacketId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeIp(String ip) throws IOException {
        for (String s : ip.split("\\.")) {
            this.writeInt(Integer.parseInt(s));
        }
    }

    protected void writePacketId() throws IOException {
        this.writeInt(this.packageId);
    }

    protected void writeHex(String hex) throws IOException {
        for (String s : hex.split(" ")) {
            s = s.trim();
            if (s.isEmpty()) {
                continue;
            }
            this.writeByte(Byte.valueOf(s, 16));
        }
    }

    protected void writeRandom(int length) throws IOException {
        for (int i = 0; i < length; i++) {
            this.writeByte((byte) (int) (Math.random() * 255));
        }
    }

    protected void writeQQ(long qq) throws IOException {
        this.writeLong(qq);
    }


    /**
     * Encode this packet.
     * <p>
     * Before sending the packet, an {@linkplain Protocol#tail tail} will be added.
     */// TODO: 2019/8/9 添加 tail
    public abstract void encode() throws IOException;

    public byte[] toByteArray() {
        return ((ByteArrayOutputStream) this.out).toByteArray();
    }
}

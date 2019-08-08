package net.mamoe.mirai.network.packet.client;

import net.mamoe.mirai.network.packet.Packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * @author Him188moe @ Mirai Project
 */
public abstract class ClientPacket extends DataOutputStream implements Packet {
    public ClientPacket() {
        super(new ByteArrayOutputStream());
    }

    protected void writeQQHex(long qq) {
        this.write
    }

    /**
     * Encode this packet
     */
    public abstract void encode();
}

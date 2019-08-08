package net.mamoe.mirai.network.packet.client;

import net.mamoe.mirai.network.packet.PacketId;

import java.io.IOException;

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId(0x0058)
public class ClientHeartbeatPacket extends ClientPacket {
    @Override
    public void encode() throws IOException {

    }
}

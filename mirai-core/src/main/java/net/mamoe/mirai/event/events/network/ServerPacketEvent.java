package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.network.packet.ServerPacket;

/**
 * @author Him188moe
 */
public abstract class ServerPacketEvent extends PacketEvent {
    public ServerPacketEvent(ServerPacket packet) {
        super(packet);
    }

    @Override
    public ServerPacket getPacket() {
        return (ServerPacket) super.getPacket();
    }
}

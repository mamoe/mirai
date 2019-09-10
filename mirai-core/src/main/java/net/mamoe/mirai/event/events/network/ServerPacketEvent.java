package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.network.packet.ServerPacket;

/**
 * @author Him188moe
 */
public abstract class ServerPacketEvent extends PacketEvent {
    public ServerPacketEvent(Bot bot, ServerPacket packet) {
        super(bot, packet);
    }

    @Override
    public ServerPacket getPacket() {
        return (ServerPacket) super.getPacket();
    }
}

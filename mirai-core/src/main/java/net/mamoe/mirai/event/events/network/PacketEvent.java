package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.event.events.MiraiEvent;
import net.mamoe.mirai.network.packet.Packet;

/**
 * @author Him188moe
 */
public abstract class PacketEvent extends MiraiEvent {
    private final Packet packet;

    public PacketEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }
}

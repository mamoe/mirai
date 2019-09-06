package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.network.packet.ClientPacket;
import org.jetbrains.annotations.NotNull;

/**
 * @author Him188moe
 */
public abstract class ClientPacketEvent extends PacketEvent {
    public ClientPacketEvent(@NotNull ClientPacket packet) {
        super(packet);
    }

    @Override
    public ClientPacket getPacket() {
        return (ClientPacket) super.getPacket();
    }
}

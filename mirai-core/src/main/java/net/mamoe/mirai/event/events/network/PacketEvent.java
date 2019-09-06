package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.event.MiraiEvent;
import net.mamoe.mirai.network.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public abstract class PacketEvent extends MiraiEvent {
    private final Packet packet;

    public PacketEvent(@NotNull Packet packet) {
        this.packet = Objects.requireNonNull(packet);
    }

    public Packet getPacket() {
        return packet;
    }
}

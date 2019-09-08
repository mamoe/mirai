package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.bot.BotEvent;
import net.mamoe.mirai.network.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public abstract class PacketEvent extends BotEvent {
    private final Packet packet;

    public PacketEvent(@NotNull Bot bot, @NotNull Packet packet) {
        super(bot);
        this.packet = Objects.requireNonNull(packet);
    }

    public Packet getPacket() {
        return packet;
    }
}

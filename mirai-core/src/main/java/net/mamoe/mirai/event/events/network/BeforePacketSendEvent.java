package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Cancellable;
import net.mamoe.mirai.network.packet.ClientPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Packet 已经 encoded, 即将被发送
 *
 * @author Him188moe
 */
public final class BeforePacketSendEvent extends ClientPacketEvent implements Cancellable {
    public BeforePacketSendEvent(@NotNull Bot bot, @NotNull ClientPacket packet) {
        super(bot, packet);
    }
}

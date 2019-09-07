package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.event.Cancellable;
import net.mamoe.mirai.network.packet.ClientPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Packet 已经 {@link ClientPacket#encode()}, 即将被发送
 *
 * @author Him188moe
 */
public final class BeforePacketSendEvent extends ClientPacketEvent implements Cancellable {
    public BeforePacketSendEvent(@NotNull ClientPacket packet) {
        super(packet);
    }
}

package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.event.Cancellable;
import net.mamoe.mirai.network.packet.ClientPacket;
import org.jetbrains.annotations.NotNull;

/**
 * @author Him188moe
 */
public final class BeforePacketSendEvent extends ClientPacketEvent implements Cancellable {
    public BeforePacketSendEvent(@NotNull ClientPacket packet) {
        super(packet);
    }
}

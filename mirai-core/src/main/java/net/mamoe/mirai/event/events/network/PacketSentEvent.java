package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.network.packet.ClientPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Packet 已经发出
 *
 * @author Him188moe
 */
public final class PacketSentEvent extends ClientPacketEvent {
    public PacketSentEvent(@NotNull Robot robot, @NotNull ClientPacket packet) {
        super(robot, packet);
    }
}

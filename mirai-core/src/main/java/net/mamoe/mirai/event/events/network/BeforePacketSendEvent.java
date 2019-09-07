package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.event.Cancellable;
import net.mamoe.mirai.network.packet.ClientPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Packet 已经 encoded, 即将被发送
 *
 * @author Him188moe
 */
public final class BeforePacketSendEvent extends ClientPacketEvent implements Cancellable {
    public BeforePacketSendEvent(@NotNull Robot robot, @NotNull ClientPacket packet) {
        super(robot, packet);
    }
}

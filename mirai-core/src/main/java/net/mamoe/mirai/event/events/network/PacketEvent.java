package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.event.events.robot.RobotEvent;
import net.mamoe.mirai.network.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public abstract class PacketEvent extends RobotEvent {
    private final Packet packet;

    public PacketEvent(@NotNull Robot robot, @NotNull Packet packet) {
        super(robot);
        this.packet = Objects.requireNonNull(packet);
    }

    public Packet getPacket() {
        return packet;
    }
}

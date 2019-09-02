package net.mamoe.mirai.event.events.qq;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.event.events.robot.RobotEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Him188moe
 */
public abstract class FriendEvent extends RobotEvent {
    private final QQ qq;

    public FriendEvent(@NotNull Robot robot, @NotNull QQ qq) {
        super(robot);
        this.qq = Objects.requireNonNull(qq);
    }

    @NotNull
    public QQ getQQ() {
        return qq;
    }
}

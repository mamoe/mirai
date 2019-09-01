package net.mamoe.mirai.event.events.robot;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.event.events.MiraiEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class RobotEvent extends MiraiEvent {
    private final Robot robot;

    public RobotEvent(@NotNull Robot robot) {
        this.robot = Objects.requireNonNull(robot);
    }

    @NotNull
    public Robot getRobot() {
        return robot;
    }

}

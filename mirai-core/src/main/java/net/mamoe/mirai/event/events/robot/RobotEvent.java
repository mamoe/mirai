package net.mamoe.mirai.event.events.robot;

import lombok.Getter;
import net.mamoe.mirai.Robot;
import net.mamoe.mirai.event.events.MiraiEvent;

public abstract class RobotEvent extends MiraiEvent {
    @Getter
    private final Robot robot;

    public RobotEvent(Robot robot){
        this.robot = robot;
    }
}

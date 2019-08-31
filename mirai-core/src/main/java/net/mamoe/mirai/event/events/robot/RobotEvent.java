package net.mamoe.mirai.event.events.robot;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.event.events.MiraiEvent;

public abstract class RobotEvent extends MiraiEvent {
    private final Robot robot;

    public Robot getRobot() {
        return robot;
    }

    public RobotEvent(Robot robot){
        this.robot = robot;
    }
}

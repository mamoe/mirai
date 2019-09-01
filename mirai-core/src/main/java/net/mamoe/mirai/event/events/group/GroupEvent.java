package net.mamoe.mirai.event.events.group;

import net.mamoe.mirai.Robot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.robot.RobotEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Him188moe
 */
public abstract class GroupEvent extends RobotEvent {
    private final Group group;

    public GroupEvent(Robot robot, Group group) {
        super(robot);
        this.group = group;
    }

    @NotNull
    public Group getGroup() {
        return group;
    }
}

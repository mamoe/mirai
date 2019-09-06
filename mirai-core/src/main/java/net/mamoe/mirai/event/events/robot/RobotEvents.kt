package net.mamoe.mirai.event.events.robot

import net.mamoe.mirai.Robot
import net.mamoe.mirai.event.MiraiEvent

/**
 * @author Him188moe
 */
class RobotLoginEvent(val robot: Robot) : MiraiEvent()

class RobotLogoutEvent(val robot: Robot) : MiraiEvent()

class RobotMessageReceivedEvent(val robot: Robot, val type: Type, val message: String) : MiraiEvent() {
    enum class Type {
        FRIEND,
        GROUP
    }
}

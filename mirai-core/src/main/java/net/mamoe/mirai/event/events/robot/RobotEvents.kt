package net.mamoe.mirai.event.events.robot

import net.mamoe.mirai.event.events.MiraiEvent
import net.mamoe.mirai.network.Robot

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


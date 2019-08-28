package net.mamoe.mirai.event.events.robot

import net.mamoe.mirai.event.events.MiraiEvent
import net.mamoe.mirai.network.RobotNetworkHandler

/**
 * @author Him188moe
 */
class RobotLoginEvent(val robotNetworkHandler: RobotNetworkHandler) : MiraiEvent()

class RobotLogoutEvent(val robotNetworkHandler: RobotNetworkHandler) : MiraiEvent()

class RobotMessageReceivedEvent(val robotNetworkHandler: RobotNetworkHandler, val type: Type, val message: String) : MiraiEvent() {
    enum class Type {
        FRIEND,
        GROUP
    }
}


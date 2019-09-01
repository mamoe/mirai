package net.mamoe.mirai.event

import net.mamoe.mirai.event.events.MiraiEvent
import net.mamoe.mirai.event.events.robot.RobotLoginSucceedEvent
import kotlin.reflect.KClass


fun <C : Class<E>, E : MiraiEvent> C.hookAlways(hook: (E) -> Unit) {
    MiraiEventManager.getInstance().hookAlways(MiraiEventHook<E>(this, hook))
}

fun <C : Class<E>, E : MiraiEvent> C.hookOnce(hook: (E) -> Unit) {
    MiraiEventManager.getInstance().hookOnce(MiraiEventHook<E>(this, hook))
}


fun <C : KClass<E>, E : MiraiEvent> C.hookAlways(hook: (E) -> Unit) {
    this.java.hookOnce(hook)
}

fun <C : KClass<E>, E : MiraiEvent> C.hookOnce(hook: (E) -> Unit) {
    this.java.hookOnce(hook)
}


fun main() {
    RobotLoginSucceedEvent::class.hookOnce {
    }
}
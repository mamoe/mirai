package net.mamoe.mirai.event

import net.mamoe.mirai.event.events.MiraiEvent
import kotlin.reflect.KClass

object EventManager : MiraiEventManager()
typealias MiraiEventManagerKt = EventManager
typealias EventMgr = EventManager

fun <C : Class<E>, E : MiraiEvent> C.hookAlways(hook: (E) -> Unit) {
    MiraiEventManager.getInstance().hookAlways(MiraiEventHook<E>(this, hook))
}

fun <C : Class<E>, E : MiraiEvent> C.hookOnce(hook: (E) -> Unit) {
    MiraiEventManager.getInstance().hookOnce(MiraiEventHook<E>(this, hook))
}

fun <C : Class<E>, E : MiraiEvent> C.hookWhile(hook: (E) -> Boolean) {
    MiraiEventManager.getInstance().hookAlways(MiraiEventHookSimple<E>(this, hook))
}


fun <C : KClass<E>, E : MiraiEvent> C.hookAlways(hook: (E) -> Unit) {
    this.java.hookAlways(hook)
}

fun <C : KClass<E>, E : MiraiEvent> C.hookOnce(hook: (E) -> Unit) {
    this.java.hookOnce(hook)
}

fun <C : KClass<E>, E : MiraiEvent> C.hookWhile(hook: (E) -> Boolean) {
    this.java.hookWhile(hook)
}


private class MiraiEventHookSimple<E : MiraiEvent>(clazz: Class<E>, val hook: (E) -> Boolean) : MiraiEventHook<E>(clazz) {
    override fun accept(event: MiraiEvent?): Boolean {
        @Suppress("UNCHECKED_CAST")
        return hook.invoke(event as E)
    }
}
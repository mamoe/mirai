package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.Event
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf

@Suppress("UNCHECKED_CAST")
internal actual inline fun <E : Event> loopAllListeners(clazz: KClass<E>, consumer: (EventListeners<in E>) -> Unit) {
    clazz.allSuperclasses.forEach {
        if (Event::class.isSuperclassOf(it)) {
            consumer((it as KClass<out Event>).listeners as EventListeners<in E>)
        }
    }
}
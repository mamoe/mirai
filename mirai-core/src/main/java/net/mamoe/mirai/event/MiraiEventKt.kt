@file:JvmName("MiraiEventKt")

package net.mamoe.mirai.event

fun <E : MiraiEvent> E.broadcast(): E {
    MiraiEventManager.getInstance().broadcastEvent(this as MiraiEvent)
    return this
}
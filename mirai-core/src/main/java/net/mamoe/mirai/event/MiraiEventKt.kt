@file:JvmName("MiraiEventKt")

package net.mamoe.mirai.event

fun <E : MiraiEvent> E.broadcastSmart(): E {
    MiraiEventManager.getInstance().broadcastEvent(this as MiraiEvent)
    return this
}
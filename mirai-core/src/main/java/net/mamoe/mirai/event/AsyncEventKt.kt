@file:JvmName("AsyncEventKt")

package net.mamoe.mirai.event

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

fun <E : AsyncEvent> E.broadcastAsync(callback: Consumer<E>): CompletableFuture<E> {
    return MiraiEventManager.getInstance().broadcastEventAsync(this, callback)
}

fun <E : AsyncEvent> E.broadcastAsync(callback: Runnable): CompletableFuture<out AsyncEvent> {
    return MiraiEventManager.getInstance().broadcastEventAsync(this, callback)
}
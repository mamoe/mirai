package net.mamoe.mirai.event

import kotlinx.coroutines.runBlocking


// TODO 添加更多
/**
 * Jvm 调用实现(阻塞)
 */
object Events {
    @JvmStatic
    fun <E : Event> subscribe(type: Class<E>, handler: suspend (E) -> ListeningStatus) =
        runBlocking { type.kotlin.subscribe(handler) }
}

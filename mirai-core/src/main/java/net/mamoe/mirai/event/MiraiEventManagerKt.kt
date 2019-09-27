@file:JvmName("MiraiEventManagerKt")

package net.mamoe.mirai.event

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import kotlin.reflect.KClass

/**
 * [MiraiEventManager] 的 kotlin 简易化实现.
 * 若要 hook 一个事件, 你可以:
 * FriendMessageEvent::class.hookOnce {}
 * FriendMessageEvent::class.hookAlways {}
 *
 * @author Him188moe
 */

object EventManager : MiraiEventManager()

/**
 * 每次事件触发时都会调用 hook
 */
fun <C : Class<E>, E : MiraiEvent> C.hookAlways(bot: Bot? = null, hook: suspend (E) -> Unit) {
    MiraiEventManager.getInstance().hookAlways(MiraiEventHook<E>(this) {
        runBlocking {
            hook(it)
        }
    })
}

/**
 * 当下一次事件触发时调用 hook
 */
fun <C : Class<E>, E : MiraiEvent> C.hookOnce(bot: Bot? = null, hook: suspend (E) -> Unit) {
    //todo bot 限制 实现. 或重写 hook
    MiraiEventManager.getInstance().hookOnce(MiraiEventHook<E>(this) {
        runBlocking {
            hook(it)
        }
    })
}

/**
 * 每次事件触发时都会调用 hook, 直到 hook 返回 false 时停止 hook
 */
fun <C : Class<E>, E : MiraiEvent> C.hookWhile(bot: Bot? = null, hook: suspend (E) -> Boolean) {
    MiraiEventManager.getInstance().hookAlways(MiraiEventHookSimple(this, hook))
}


/**
 * 每次事件触发时都会调用 hook
 *
 * @param bot 指定仅限于某个 [Bot] 的事件.
 */
fun <C : KClass<E>, E : MiraiEvent> C.hookAlways(bot: Bot? = null, hook: suspend (E) -> Unit) {
    this.java.hookAlways(bot, hook)
}

/**
 * 当下一次事件触发时调用 hook
 */
fun <C : KClass<E>, E : MiraiEvent> C.hookOnce(bot: Bot? = null, hook: suspend (E) -> Unit) {
    this.java.hookOnce(bot, hook)
}

/**
 * 每次事件触发时都会调用 hook, 直到 hook 返回 false 时停止 hook
 */
fun <C : KClass<E>, E : MiraiEvent> C.hookWhile(bot: Bot? = null, hook: suspend (E) -> Boolean) {
    this.java.hookWhile(bot, hook)
}


private class MiraiEventHookSimple<E : MiraiEvent>(clazz: Class<E>, val hook: suspend (E) -> Boolean) : MiraiEventHook<E>(clazz) {
    override fun accept(event: MiraiEvent?): Boolean {
        @Suppress("UNCHECKED_CAST")
        return runBlocking {
            return@runBlocking !hook.invoke(event as E)
        }
    }
}
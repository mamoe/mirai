package net.mamoe.mirai.event

import kotlinx.coroutines.runBlocking
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * @author Him188moe
 */
class MiraiEventHookKt<E : MiraiEvent>(eventClass: Class<E>) : MiraiEventHook<E>(eventClass) {
    fun onEvent(handler: suspend (E) -> Unit) {
        this@MiraiEventHookKt.handler = Consumer {
            runBlocking {
                handler(it)
            }
        }
    }

    fun validChecker(predicate: suspend (E) -> Boolean) {//todo 把 mirai event 变为 suspend, 而不是在这里 run blocking
        this@MiraiEventHookKt.validChecker = Predicate {
            runBlocking {
                predicate(it)
            }
        }
    }
}


/**
 * Kotlin 风格回调
 * 你的代码可以这样(并且 validChecker 是可选的):
 *
 * event.hook {
 *  onEvent {}
 *  validChecker {}
 * }
 */
fun <E : MiraiEvent> E.hook(handler: MiraiEventHookKt<E>.() -> Unit): MiraiEventHookKt<E> {
    return MiraiEventHookKt(this.javaClass).apply(handler)
}
package net.mamoe.mirai.event

import java.util.function.Consumer
import java.util.function.Predicate

/**
 * @author Him188moe
 */
class MiraiEventHookKt<E : MiraiEvent>(eventClass: Class<E>) : MiraiEventHook<E>(eventClass) {
    fun onEvent(handler: (E) -> Unit) {
        this@MiraiEventHookKt.handler = Consumer(handler)
    }

    fun validChecker(predicate: (E) -> Boolean) {
        this@MiraiEventHookKt.validChecker = Predicate(predicate)
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
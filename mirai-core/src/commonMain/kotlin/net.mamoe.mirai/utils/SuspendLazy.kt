@file:Suppress("unused", "FunctionName")

package net.mamoe.mirai.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.mamoe.mirai.contact.QQ

/**
 * 创建一个在当前 [CoroutineScope] 下执行的 [SuspendLazy]
 */
fun <R> CoroutineScope.SuspendLazy(initializer: suspend () -> R): SuspendLazy<R> = SuspendLazy(this, initializer)

/**
 * 挂起初始化的 [lazy], 是属性不能 `suspend` 的替代品.
 *
 * 本对象代表值初始化时将会通过 [CoroutineScope.async] 运行 [initializer]
 *
 * [SuspendLazy] 是:
 * - 线程安全
 * - 只会被初始化一次.
 * - `SuspendLazy<R>.value` 返回 `Deferred<R>`
 * - 可以用作属性代表 (`val profile: by SuspendLazy(GlobalScope) { calculateValue() }`)
 *
 * @sample QQ.profile
 */
class SuspendLazy<R>(scope: CoroutineScope, val initializer: suspend () -> R) : Lazy<Deferred<R>> {
    private val valueUpdater: Deferred<R> by lazy { scope.async { initializer() } }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val value: Deferred<R>
        get() = if (isInitialized()) {
            CompletableDeferred(valueUpdater.getCompleted())
        } else {
            CompletableDeferred<R>().apply {
                valueUpdater.invokeOnCompletion {
                    this.complete(valueUpdater.getCompleted())
                }
            }
        }

    override fun isInitialized(): Boolean = valueUpdater.isCompleted
}
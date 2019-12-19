@file:Suppress("unused", "FunctionName")

package net.mamoe.mirai.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.mamoe.mirai.contact.QQ
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 创建一个在当前 [CoroutineScope] 下执行初始化的 [suspendLazy]
 *
 * ```kotlin
 * val image: Deferred<Image> by suspendLazy{  /* intializer */  }
 * ```
 */
fun <R> CoroutineScope.suspendLazy(context: CoroutineContext = EmptyCoroutineContext, initializer: suspend () -> R): Lazy<Deferred<R>> =
    SuspendLazy(this, context, initializer)

/**
 * 挂起初始化的 [lazy], 是属性不能 `suspend` 的替代品.
 *
 * 本对象代表值初始化时将会通过 [CoroutineScope.async] 运行 [valueUpdater]
 *
 * [SuspendLazy] 是:
 * - 线程安全
 * - 只会被初始化一次.
 * - `SuspendLazy<R>.value` 返回 `Deferred<R>`
 * - 可以用作属性代表 (`val profile: by SuspendLazy(GlobalScope) { calculateValue() }`)
 *
 * @sample QQ.profile
 */
@PublishedApi
internal class SuspendLazy<R>(scope: CoroutineScope, coroutineContext: CoroutineContext = EmptyCoroutineContext, initializer: suspend () -> R) :
    Lazy<Deferred<R>> {
    private val valueUpdater: Deferred<R> by lazy { scope.async(context = coroutineContext) { initializer() } }

    override val value: Deferred<R>
        get() = valueUpdater

    override fun isInitialized(): Boolean = valueUpdater.isCompleted
}
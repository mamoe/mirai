/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "FunctionName", "NOTHING_TO_INLINE")

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
inline fun <R> CoroutineScope.suspendLazy(context: CoroutineContext = EmptyCoroutineContext, noinline initializer: suspend () -> R): Lazy<Deferred<R>> =
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
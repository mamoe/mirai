/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Mirror of kotlinx-coroutines-debug to be used in common sources.
 */
@Suppress("RedundantSetter", "RedundantGetter")
expect object DebugProbes {

    /**
     * Whether coroutine creation stack traces should be sanitized.
     * Sanitization removes all frames from `kotlinx.coroutines` package except
     * the first one and the last one to simplify diagnostic.
     */
    var sanitizeStackTraces: Boolean get set

    /**
     * Whether coroutine creation stack traces should be captured.
     * When enabled, for each created coroutine a stack trace of the current
     * thread is captured and attached to the coroutine.
     * This option can be useful during local debug sessions, but is recommended
     * to be disabled in production environments to avoid stack trace dumping overhead.
     */
    var enableCreationStackTraces: Boolean get set

    /**
     * Determines whether debug probes were [installed][DebugProbes.install].
     */
    val isInstalled: Boolean get

    /**
     * Installs a [DebugProbes] instead of no-op stdlib probes by redefining
     * debug probes class using the same class loader as one loaded [DebugProbes] class.
     */
    fun install()

    /**
     * Uninstall debug probes.
     */
    fun uninstall()

    /**
     * Invokes given block of code with installed debug probes and uninstall probes in the end.
     */
    inline fun withDebugProbes(block: () -> Unit)

    /**
     * Returns string representation of the coroutines [job] hierarchy with additional debug information.
     * Hierarchy is printed from the [job] as a root transitively to all children.
     */
    fun jobToString(job: Job): String

    /**
     * Returns string representation of all coroutines launched within the given [scope].
     * Throws [IllegalStateException] if the scope has no a job in it.
     */
    fun scopeToString(scope: CoroutineScope): String

    /**
     * Prints [job] hierarchy representation from [jobToString] to the given [out].
     */
    public fun printJob(job: Job): Unit

    /**
     * Prints all coroutines launched within the given [scope].
     * Throws [IllegalStateException] if the scope has no a job in it.
     */
    public fun printScope(scope: CoroutineScope): Unit

    /**
     * Returns all existing coroutines info.
     * The resulting collection represents a consistent snapshot of all existing coroutines at the moment of invocation.
     */
//    public fun dumpCoroutinesInfo(): List<CoroutineInfo>

    /**
     * Dumps all active coroutines into the given output stream, providing a consistent snapshot of all existing coroutines at the moment of invocation.
     * The output of this method is similar to `jstack` or a full thread dump. It can be used as the replacement to
     * "Dump threads" action.
     *
     * Example of the output:
     * ```
     * Coroutines dump 2018/11/12 19:45:14
     *
     * Coroutine "coroutine#42":StandaloneCoroutine{Active}@58fdd99, state: SUSPENDED
     *     at MyClass$awaitData.invokeSuspend(MyClass.kt:37)
     * (Coroutine creation stacktrace)
     *     at MyClass.createIoRequest(MyClass.kt:142)
     *     at MyClass.fetchData(MyClass.kt:154)
     *     at MyClass.showData(MyClass.kt:31)
     * ...
     * ```
     */
    fun dumpCoroutines(): Unit
}

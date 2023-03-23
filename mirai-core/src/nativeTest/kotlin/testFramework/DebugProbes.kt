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

@Suppress("UNUSED_PARAMETER", "unused")
actual object DebugProbes {

    /**
     * Prints  hierarchy representation from [jobToString] to the given .
     */
//    public fun printJob(job: Job, out: PrintStream = System.out): Unit

    /**
     * Prints all coroutines launched within the given .
     * Throws [IllegalStateException] if the scope has no a job in it.
     */
//    public fun printScope(scope: CoroutineScope, out: PrintStream = System.out): Unit

    /**
     * Returns all existing coroutines info.
     * The resulting collection represents a consistent snapshot of all existing coroutines at the moment of invocation.
     */
//    public fun dumpCoroutinesInfo(): List<CoroutineInfo>
    /**
     * Whether coroutine creation stack traces should be sanitized.
     * Sanitization removes all frames from `kotlinx.coroutines` package except
     * the first one and the last one to simplify diagnostic.
     */
    actual var sanitizeStackTraces: Boolean
        get() = false
        set(value) {}

    /**
     * Whether coroutine creation stack traces should be captured.
     * When enabled, for each created coroutine a stack trace of the current
     * thread is captured and attached to the coroutine.
     * This option can be useful during local debug sessions, but is recommended
     * to be disabled in production environments to avoid stack trace dumping overhead.
     */
    actual var enableCreationStackTraces: Boolean
        get() = false
        set(value) {}

    /**
     * Determines whether debug probes were [installed][DebugProbes.install].
     */
    actual val isInstalled: Boolean
        get() = false

    /**
     * Installs a [DebugProbes] instead of no-op stdlib probes by redefining
     * debug probes class using the same class loader as one loaded [DebugProbes] class.
     */
    actual fun install() {
    }

    /**
     * Uninstall debug probes.
     */
    actual fun uninstall() {
    }

    /**
     * Invokes given block of code with installed debug probes and uninstall probes in the end.
     */
    actual inline fun withDebugProbes(block: () -> Unit) {
    }

    /**
     * Returns string representation of the coroutines [job] hierarchy with additional debug information.
     * Hierarchy is printed from the [job] as a root transitively to all children.
     */
    actual fun jobToString(job: Job): String {
        return ""
    }

    /**
     * Returns string representation of all coroutines launched within the given [scope].
     * Throws [IllegalStateException] if the scope has no a job in it.
     */
    actual fun scopeToString(scope: CoroutineScope): String {
        return ""
    }

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
    actual fun dumpCoroutines() {
    }

    /**
     * Prints [job] hierarchy representation from [jobToString] to the given .
     */
    actual fun printJob(job: Job) {
    }

    /**
     * Prints all coroutines launched within the given [scope].
     * Throws [IllegalStateException] if the scope has no a job in it.
     */
    actual fun printScope(scope: CoroutineScope) {
    }

}
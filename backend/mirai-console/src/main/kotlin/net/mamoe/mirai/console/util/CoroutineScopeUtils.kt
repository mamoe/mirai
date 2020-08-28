/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("CoroutineScopeUtils")

package net.mamoe.mirai.console.util

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.internal.plugin.NamedSupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal fun CoroutineContext.overrideWithSupervisorJob(name: String? = null): CoroutineContext =
    this + NamedSupervisorJob(name ?: "<unnamed>", this[Job])

internal fun CoroutineScope.childScope(
    name: String? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineScope =
    CoroutineScope(this.childScopeContext(name, context))

internal fun CoroutineScope.childScopeContext(
    name: String? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineContext =
    this.coroutineContext.overrideWithSupervisorJob(name) + context.let {
        if (name != null) it + CoroutineName(name)
        else it
    }

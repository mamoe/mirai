/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")
@file:JvmBlockingBridge

package net.mamoe.mirai.message.action

import kotlinx.coroutines.Deferred
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn

/**
 * [MessageSource.recallIn] 的结果.
 *
 * @see MessageSource.recallIn
 */
public expect class AsyncRecallResult internal constructor(
    /**
     * 撤回时产生的异常.
     */
    exception: Deferred<Throwable?>,
) {
    public val exception: Deferred<Throwable?>

    /**
     * 撤回是否成功.
     */
    public val isSuccess: Deferred<Boolean>

    /**
     * 等待撤回完成, 返回撤回时产生的异常.
     */
    public suspend fun awaitException(): Throwable?

    /**
     * 等待撤回完成, 返回撤回的结果.
     */
    public suspend fun awaitIsSuccess(): Boolean
}


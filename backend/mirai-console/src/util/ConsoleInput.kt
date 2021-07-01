/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INAPPLICABLE_JVM_NAME", "unused")
@file:JvmMultifileClass
@file:JvmName("ConsoleUtils")

package net.mamoe.mirai.console.util

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.util.ConsoleInputImpl

/**
 * Console 输入. 由于 console 接管了 [标准输入][System. in], [readLine] 等操作需要在这里进行.
 */
public interface ConsoleInput {
    /**
     * 挂起当前协程, 以 [提示][hint] 向用户索要一个输入, 在用户完成输入时返回输入结果.
     */
    @JvmBlockingBridge
    public suspend fun requestInput(hint: String): String

    public companion object INSTANCE : ConsoleInput by ConsoleInputImpl
}

// don't move into INSTANCE, Compilation error
@JvmSynthetic
public suspend inline fun MiraiConsole.requestInput(hint: String): String = ConsoleInput.requestInput(hint)
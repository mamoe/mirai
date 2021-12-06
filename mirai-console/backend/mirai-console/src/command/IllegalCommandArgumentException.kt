/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException

/**
 * 在处理参数时遇到的 _正常_ 错误. 如参数不符合规范, 参数值越界等.
 *
 * [message] 将会发送给指令调用方.
 *
 * 如果指令调用方是 [ConsoleCommandSender],
 * 还会将 [cause], [suppressedExceptions] 发送给 [ConsoleCommandSender] (如果存在)
 *
 * @see CommandArgumentParserException
 */
public open class IllegalCommandArgumentException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
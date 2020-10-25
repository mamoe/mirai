/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.command.IllegalCommandArgumentException
import net.mamoe.mirai.console.command.descriptor.AbstractCommandValueArgumentParser.Companion.illegalArgument

/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范等.
 *
 * [message] 将会发送给指令调用方.
 *
 * @see IllegalCommandArgumentException
 * @see CommandValueArgumentParser
 * @see AbstractCommandValueArgumentParser.illegalArgument
 */
public class CommandArgumentParserException : IllegalCommandArgumentException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}
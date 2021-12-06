/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.IllegalCommandArgumentException
import net.mamoe.mirai.console.command.descriptor.AbstractCommandValueArgumentParser.Companion.illegalArgument
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.internal.data.classifierAsKClassOrNull
import net.mamoe.mirai.console.internal.data.qualifiedNameOrTip
import kotlin.reflect.KType


internal val KType.qualifiedName: String
    get() = this.classifierAsKClassOrNull()?.qualifiedNameOrTip ?: classifier.toString()

@ExperimentalCommandDescriptors
public open class NoValueArgumentMappingException(
    public val argument: CommandValueArgument,
    public val forType: KType,
) : CommandResolutionException("Cannot find a CommandArgument mapping for ${forType.qualifiedName}")

public open class CommandResolutionException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

@ExperimentalCommandDescriptors
public open class CommandDeclarationClashException(
    public val command: Command,
    public val signatures: List<CommandSignature>,
) : CommandDeclarationException("Declaration clash for command '${command.primaryName}': \n${signatures.joinToString("\n")}")

public open class CommandDeclarationException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范等.
 *
 * [message] 将会发送给指令调用方.
 *
 * @see IllegalCommandArgumentException
 * @see CommandValueArgumentParser
 * @see AbstractCommandValueArgumentParser.illegalArgument
 */
public class CommandArgumentParserException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null,
) : IllegalCommandArgumentException(message, cause)
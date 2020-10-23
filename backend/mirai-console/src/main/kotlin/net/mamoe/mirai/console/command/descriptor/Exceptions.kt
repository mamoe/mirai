/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.command.parse.CommandCall
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

@ExperimentalCommandDescriptors
public open class UnresolvedCommandCallException(
    public val call: CommandCall,
) : CommandResolutionException("Unresolved call: $call")

public open class CommandResolutionException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

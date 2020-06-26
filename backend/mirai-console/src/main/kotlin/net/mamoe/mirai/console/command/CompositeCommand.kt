/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.description.CommandArgParser
import net.mamoe.mirai.console.command.description.CommandParserContext
import net.mamoe.mirai.console.command.description.EmptyCommandParserContext
import net.mamoe.mirai.console.command.description.plus
import net.mamoe.mirai.console.command.internal.CompositeCommandImpl
import net.mamoe.mirai.console.command.internal.isValidSubName
import kotlin.reflect.KClass


/**
 * 功能最集中的 Commend
 * 只支持有sub的指令
 * 例:
 *  /mute add
 *  /mute remove
 *  /mute addandremove  (sub is case insensitive, lowercase are recommend)
 *  /mute add and remove('add and remove' consider as a sub)
 */
abstract class CompositeCommand @JvmOverloads constructor(
    final override val owner: CommandOwner,
    vararg names: String,
    description: String = "no description available",
    final override val permission: CommandPermission = CommandPermission.Default,
    final override val prefixOptional: Boolean = false,
    overrideContext: CommandParserContext = EmptyCommandParserContext
) : Command, CompositeCommandImpl() {
    final override val description = description.trimIndent()
    final override val names: Array<out String> =
        names.map(String::trim).filterNot(String::isEmpty).map(String::toLowerCase).also { list ->
            list.firstOrNull { !it.isValidSubName() }?.let { error("Name is not valid: $it") }
        }.toTypedArray()

    /**
     * [CommandArgParser] 的环境
     */
    val context: CommandParserContext = CommandParserContext.Builtins + overrideContext

    final override val usage: String get() = super.usage

    /** 指定子指令要求的权限 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class Permission(val permission: KClass<out CommandPermission>)

    /** 标记一个函数为子指令 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class SubCommand(vararg val name: String)

    /** 指令描述 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class Description(val description: String)

    /** 参数名, 将参与构成 [usage] */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class Name(val name: String)

    final override suspend fun CommandSender.onCommand(args: Array<out Any>) {
        matchSubCommand(args)?.parseAndExecute(this, args) ?: kotlin.run {
            defaultSubCommand.onCommand(this, args)
        }
    }
}

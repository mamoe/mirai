/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import kotlin.reflect.KProperty

internal const val FOR_BINARY_COMPATIBILITY = "for binary compatibility"

/**
 * 指令
 *
 * @see register 注册这个指令
 */
interface Command {
    val owner: CommandOwner
    val descriptor: CommandDescriptor

    /*
    @Deprecated(FOR_BINARY_COMPATIBILITY, level = DeprecationLevel.HIDDEN)
    suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
        return true
    }*/

    /**
     * 执行这个指令.
     */
    suspend fun onCommand(sender: CommandSender, args: CommandArgs): Boolean
}

/**
 * 指令实际参数列表. 参数顺序与 [Command.descriptor] 的 [CommandDescriptor.params] 相同.
 */
class CommandArgs private constructor(
    @JvmField
    internal val values: List<Any>,
    private val fromCommand: Command
) : List<Any> by values {
    /**
     * 获取第一个类型为 [R] 的参数
     */
    @JvmSynthetic
    inline fun <reified R> getReified(): R {
        for (value in this) {
            if (value is R) {
                return value
            }
        }
        error("Cannot find argument typed ${R::class.qualifiedName}")
    }

    /**
     * 获取名称为 [name] 的参数.
     *
     * 若 [name] 为 `null` 则获取第一个匿名参数
     * @throws NoSuchElementException 找不到这个名称的参数时抛出
     */
    operator fun get(name: String?): Any {
        val index = fromCommand.descriptor.params.indexOfFirst { it.name == name }
        if (index == -1) {
            throw NoSuchElementException("Cannot find argument named $name")
        }
        return values[index]
    }

    /**
     * 获取名称为 [name] 的参数. 并强转为 [R].
     *
     * 若 [name] 为 `null` 则获取第一个匿名参数
     * @throws IllegalStateException 无法强转时抛出
     */
    fun <R> getAs(name: String?): R {
        @Suppress("UNCHECKED_CAST")
        return this[name] as? R ?: error("Argument $name has a type $")
    }

    /** 获取第一个类型为 [R] 的参数并提供委托 */
    inline operator fun <reified R : Any> getValue(thisRef: Any?, property: KProperty<*>): R = getReified()

    companion object {
        fun parseFrom(command: Command, sender: CommandSender, rawArgs: List<Any>): CommandArgs {
            val params = command.descriptor.params

            require(rawArgs.size >= params.size) { "No enough rawArgs: required ${params.size}, found only ${rawArgs.size}" }

            command.descriptor.params.asSequence().zip(rawArgs.asSequence()).map { (commandParam, any) ->
                command.parserFor(commandParam)?.parse(any, sender)
                    ?: error("ICould not find a parser for param named ${commandParam.name}")
            }.toList().let { bakedArgs ->
                return CommandArgs(bakedArgs, command)
            }
        }
    }
}

inline val Command.fullName get() = descriptor.fullName
inline val Command.usage get() = descriptor.usage
inline val Command.params get() = descriptor.params
inline val Command.description get() = descriptor.description
inline val Command.context get() = descriptor.context
inline val Command.aliases get() = descriptor.aliases
inline val Command.permission get() = descriptor.permission
inline val Command.allNames get() = descriptor.allNames

abstract class PluginCommand(
    final override val owner: PluginBase,
    descriptor: CommandDescriptor
) : AbstractCommand(descriptor)

internal abstract class ConsoleCommand(
    descriptor: CommandDescriptor
) : AbstractCommand(descriptor) {
    final override val owner: MiraiConsole get() = MiraiConsole
}

sealed class AbstractCommand(
    final override val descriptor: CommandDescriptor
) : Command


/**
 * For Java
 */
@Suppress("unused")
abstract class BlockingCommand(
    owner: PluginBase,
    descriptor: CommandDescriptor
) : PluginCommand(owner, descriptor) {
    final override suspend fun onCommand(sender: CommandSender, args: CommandArgs): Boolean {
        return withContext(Dispatchers.IO) { onCommandBlocking(sender, args) }
    }

    abstract fun onCommandBlocking(sender: CommandSender, args: CommandArgs): Boolean
}
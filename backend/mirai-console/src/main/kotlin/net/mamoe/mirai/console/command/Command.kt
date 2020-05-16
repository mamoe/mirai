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

import net.mamoe.mirai.console.command.description.CommandParam
import net.mamoe.mirai.console.command.description.CommandParserContext
import net.mamoe.mirai.console.command.description.plus
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KClass

/**
 * 指令
 * 通常情况下, 你的指令应继承 @see CompositeCommand/SimpleCommand
 * @see register 注册这个指令
 */
interface Command {
    val names: Array<String>
    val description: String
    val permission: CommandPermission
    val prefixOptional: Boolean

    val owner: CommandOwner

    suspend fun onCommand(sender: CommandSender, args: Array<out Any>)
}

/**
 * 功能最集中的Commend
 * 支持且只支持有sub的指令
 * 例:
 *  /mute add
 *  /mute remove
 *  /mute addandremove  (sub is case insensitive, lowercase are recommend)
 *  /mute add and remove('add and remove' consider as a sub)
 */
abstract class CompositeCommand @JvmOverloads constructor(
    override val owner: CommandOwner,
    override val names: Array<String>,
    override val description: String,
    override val prefixOptional: Boolean = false,
    overrideContext: CommandParserContext
) : Command {
    val context: CommandParserContext = CommandParserContext.Builtins + overrideContext

    /**
     * Permission of the command
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class Permission(val permission: KClass<out Permission>)

    /**
     * 你应当使用 @SubCommand 来注册 sub 指令
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class SubCommand(val name: String)


    /**
     * Usage of the sub command
     * you should not include arg names, which will be insert automatically
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class Usage(val usage: String)

    /**
     * name of the parameter
     *
     * by default available
     */
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class Name(val name: String)

    final override suspend fun onCommand(sender: CommandSender, args: Array<out Any>) {
        matchSubCommand(args).parseAndExecute(sender, args)
        subCommands
    }

    internal val defaultSubCommand: SubCommandDescriptor by lazy {
        TODO()
    }
    internal val subCommands: Array<SubCommandDescriptor> by lazy {
        TODO()
    }

    @JvmField
    internal val bakedCommandNameToSubDescriptorArray: Map<Array<String>, SubCommandDescriptor> = kotlin.run {
        val map = LinkedHashMap<Array<String>, SubCommandDescriptor>(subCommands.size * 2)
        for (descriptor in subCommands) {
            for (name in descriptor.bakedSubNames) {
                map[name] = descriptor
            }
        }
        map.toSortedMap(Comparator { o1, o2 -> o1!!.contentHashCode() - o2!!.contentHashCode() })
    }

    internal inner class SubCommandDescriptor(
        val names: Array<String>,
        val params: Array<CommandParam<*>>,
        val description: String,
        val permission: CommandPermission,
        val onCommand: suspend (sender: CommandSender, parsedArgs: List<Any>) -> Boolean
    ) {
        internal suspend inline fun parseAndExecute(
            sender: CommandSender,
            argsWithSubCommandNameNotRemoved: Array<out Any>
        ) {
            if (!onCommand(sender, parseArgs(sender, argsWithSubCommandNameNotRemoved, names.size))) {
                // TODO: 2020/5/16 SEND USAGE
            }
        }

        @JvmField
        internal val bakedSubNames: Array<Array<String>> = names.map { it.bakeSubName() }.toTypedArray()
        private fun parseArgs(sender: CommandSender, rawArgs: Array<out Any>, offset: Int): List<Any> {
            require(rawArgs.size >= offset + this.params.size) { "No enough args. Required ${params.size}, but given ${rawArgs.size}" }

            return this.params.mapIndexed { index, param ->
                val rawArg = rawArgs[offset + index]
                when (rawArg) {
                    is String -> context[param.type]?.parse(rawArg, sender)
                    is SingleMessage -> context[param.type]?.parse(rawArg, sender)
                    else -> throw IllegalArgumentException("Illegal argument type: ${rawArg::class.qualifiedName}")
                } ?: error("Cannot find a parser for $rawArg")
            }
        }
    }

    /**
     * @param rawArgs 元素类型必须为 [SingleMessage] 或 [String], 且已经经过扁平化处理. 否则抛出异常 [IllegalArgumentException]
     */
    internal fun matchSubCommand(rawArgs: Array<out Any>): SubCommandDescriptor {
        val maxCount = rawArgs.size - 1
        var cur = 0
        bakedCommandNameToSubDescriptorArray.forEach { (name, descriptor) ->
            if (name.size != cur) {
                if (cur++ == maxCount) return defaultSubCommand
            }
            if (name.contentEqualsOffset(rawArgs, offset = cur)) {
                return descriptor
            }
        }
        return defaultSubCommand
    }
}


abstract class SimpleCommand(
    override val owner: CommandOwner,
    val name: String,
    val alias: Array<String> = arrayOf()
) : Command {
    abstract suspend fun CommandSender.onCommand(args: List<Any>)
}

abstract class RawCommand(
    final override val owner: CommandOwner,
    name: String,
    alias: Array<String> = arrayOf()
) : Command {
    final override val names: Array<String> = arrayOf(name, *alias)
}


private fun <T> Array<T>.contentEqualsOffset(other: Array<out Any>, offset: Int): Boolean {
    for (index in other.indices) {
        if (other[index + offset].toString() != this[index]) {
            return false
        }
    }
    return true
}

internal val ILLEGAL_SUB_NAME_CHARS = "\\/!@#$%^&*()_+-={}[];':\",.<>?`~".toCharArray()
internal fun String.isValidSubName(): Boolean = ILLEGAL_SUB_NAME_CHARS.none { it in this }
internal fun String.bakeSubName(): Array<String> = split(' ').filterNot { it.isBlank() }.toTypedArray()

internal fun Any.flattenCommandComponents(): ArrayList<Any> {
    val list = ArrayList<Any>()
    when (this) {
        is String -> list.addAll(split(' ').filterNot { it.isBlank() })
        is PlainText -> list.addAll(content.flattenCommandComponents())
        is SingleMessage -> list.add(this)
        is Iterable<*> -> this.asSequence().forEach { if (it != null) list.addAll(it.flattenCommandComponents()) }
        else -> list.add(this.toString())
    }
    return list
}
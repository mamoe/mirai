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
import net.mamoe.mirai.console.command.description.EmptyCommandParserContext
import net.mamoe.mirai.console.command.description.plus
import net.mamoe.mirai.console.plugins.MyArg
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

/**
 * 指令
 * 通常情况下, 你的指令应继承 @see CompositeCommand/SimpleCommand
 * @see register 注册这个指令
 */
interface Command {
    val names: Array<out String>
    val usage: String
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
    override vararg val names: String,
    override val description: String,
    override val permission: CommandPermission = CommandPermission.Default,
    override val prefixOptional: Boolean = false,
    overrideContext: CommandParserContext = EmptyCommandParserContext
) : Command {
    val context: CommandParserContext = CommandParserContext.Builtins + overrideContext
    override val usage: String by lazy { TODO() }

    /** 指定子指令要求的权限 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class Permission(val permission: KClass<out Permission>)

    /** 标记一个函数为子指令 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class SubCommand(val name: String)

    /** 指令描述 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class Description(val description: String)

    /** 参数名, 将参与构成 [usage] */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class Name(val name: String)

    final override suspend fun onCommand(sender: CommandSender, args: Array<out Any>) {
        matchSubCommand(args)?.parseAndExecute(sender, args) ?: kotlin.run {
            defaultSubCommand.onCommand(sender, args)
        }
        subCommands
    }

    internal val defaultSubCommand: DefaultSubCommandDescriptor by lazy {
        DefaultSubCommandDescriptor(
            "",
            CommandPermission.Default,
            onCommand = block { sender: CommandSender, args: Array<out Any> ->
                println("default finally got args: ${args.joinToString()}")
                true
            }
        )
    }

    internal val subCommands: Array<SubCommandDescriptor> by lazy {
        this::class.declaredFunctions.filter { it.hasAnnotation<SubCommand>() }.map { function ->
            SubCommandDescriptor(
                arrayOf(function.name),
                arrayOf(CommandParam("p", MyArg::class)),
                "",
                CommandPermission.Default,
                onCommand = block { sender: CommandSender, args: Array<out Any> ->
                    println("subname finally gor args: ${args.joinToString()}")
                    true
                }
            )
        }.toTypedArray()
    }

    private fun block(block: suspend (CommandSender, Array<out Any>) -> Boolean): suspend (CommandSender, Array<out Any>) -> Boolean {
        return block
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

    internal inner class DefaultSubCommandDescriptor(
        val description: String,
        val permission: CommandPermission,
        val onCommand: suspend (sender: CommandSender, rawArgs: Array<out Any>) -> Boolean
    )

    internal inner class SubCommandDescriptor(
        val names: Array<String>,
        val params: Array<CommandParam<*>>,
        val description: String,
        val permission: CommandPermission,
        val onCommand: suspend (sender: CommandSender, parsedArgs: Array<out Any>) -> Boolean
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
        private fun parseArgs(sender: CommandSender, rawArgs: Array<out Any>, offset: Int): Array<out Any> {
            require(rawArgs.size >= offset + this.params.size) { "No enough args. Required ${params.size}, but given ${rawArgs.size - offset}" }

            return Array(this.params.size) { index ->
                val param = params[index]
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
    internal fun matchSubCommand(rawArgs: Array<out Any>): SubCommandDescriptor? {
        val maxCount = rawArgs.size - 1
        var cur = 0
        bakedCommandNameToSubDescriptorArray.forEach { (name, descriptor) ->
            if (name.size != cur) {
                if (cur++ == maxCount) return null
            }
            if (name.contentEqualsOffset(rawArgs, length = cur)) {
                return descriptor
            }
        }
        return null
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


private fun <T> Array<T>.contentEqualsOffset(other: Array<out Any>, length: Int): Boolean {
    repeat(length) { index ->
        if (other[index].toString() != this[index]) {
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
    when (this::class.java) {
        String::class.java -> (this as String).splitToSequence(' ').filterNot { it.isBlank() }.forEach { list.add(it) }
        PlainText::class.java -> (this as PlainText).content.splitToSequence(' ').filterNot { it.isBlank() }
            .forEach { list.add(it) }
        SingleMessage::class.java -> list.add(this as SingleMessage)
        Array<Any>::class.java -> (this as Array<*>).forEach { if (it != null) list.addAll(it.flattenCommandComponents()) }
        Iterable::class.java -> (this as Iterable<*>).forEach { if (it != null) list.addAll(it.flattenCommandComponents()) }
        else -> list.add(this.toString())
    }
    return list
}

internal inline fun <reified T : Annotation> KAnnotatedElement.hasAnnotation(): Boolean =
    findAnnotation<T>() != null

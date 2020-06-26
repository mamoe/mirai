/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command.internal

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.description.CommandParam
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.*

internal abstract class CompositeCommandImpl : Command {
    @JvmField
    @Suppress("PropertyName")
    internal var _usage: String = "<command build failed>"

    override val usage: String  // initialized by subCommand reflection
        get() = _usage

    internal val defaultSubCommand: DefaultSubCommandDescriptor by lazy {
        DefaultSubCommandDescriptor(
            "",
            CommandPermission.Default,
            onCommand = block { sender: CommandSender, args: Array<out Any> ->
                false//not supported yet
            }
        )
    }

    internal val subCommands: Array<SubCommandDescriptor> by lazy {
        @Suppress("CAST_NEVER_SUCCEEDS")
        this as CompositeCommand

        val buildUsage = StringBuilder(this.description).append(": \n")

        this::class.declaredFunctions.filter { it.hasAnnotation<CompositeCommand.SubCommand>() }.map { function ->
            val notStatic = !function.hasAnnotation<JvmStatic>()
            val overridePermission = function.findAnnotation<CompositeCommand.Permission>()//optional
            val subDescription =
                function.findAnnotation<CompositeCommand.Description>()?.description ?: "no description available"

            if ((function.returnType.classifier as? KClass<*>)?.isSubclassOf(Boolean::class) != true) {
                error("Return Type of SubCommand must be Boolean")
            }

            val parameters = function.parameters.toMutableList()
            check(parameters.isNotEmpty()) {
                "First parameter (receiver for kotlin) for sub commend " + function.name + " from " + this.primaryName + " should be <out CommandSender>"
            }

            if (notStatic) parameters.removeAt(0) // instance

            (parameters.removeAt(0)).let { receiver ->
                check(!receiver.isVararg && !((receiver.type.classifier as? KClass<*>).also { print(it) }
                    ?.isSubclassOf(CommandSender::class) != true)) {
                    "First parameter (receiver for kotlin) for sub commend " + function.name + " from " + this.primaryName + " should be <out CommandSender>"
                }
            }

            val commandName = function.findAnnotation<CompositeCommand.SubCommand>()!!.name.map {
                if (!it.isValidSubName()) {
                    error("SubName $it is not valid")
                }
                it
            }.toTypedArray()

            //map parameter
            val params = parameters.map { param ->
                buildUsage.append("/$primaryName ")

                if (param.isVararg) error("parameter for sub commend " + function.name + " from " + this.primaryName + " should not be var arg")
                if (param.isOptional) error("parameter for sub commend " + function.name + " from " + this.primaryName + " should not be var optional")

                val argName = param.findAnnotation<CompositeCommand.Name>()?.name ?: param.name ?: "unknown"
                buildUsage.append("<").append(argName).append("> ").append(" ")
                CommandParam(
                    argName,
                    (param.type.classifier as? KClass<*>)
                        ?: throw IllegalArgumentException("unsolved type reference from param " + param.name + " in " + function.name + " from " + this.primaryName)
                )
            }.toTypedArray()

            buildUsage.append(subDescription).append("\n")

            SubCommandDescriptor(
                commandName,
                params,
                subDescription,
                overridePermission?.permission?.getInstance() ?: permission,
                onCommand = block { sender: CommandSender, args: Array<out Any> ->
                    if (notStatic) {
                        function.callSuspend(this, sender, *args) as Boolean
                    } else {
                        function.callSuspend(sender, *args) as Boolean
                    }
                }
            )
        }.toTypedArray().also {
            _usage = buildUsage.toString()
        }
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

    internal class DefaultSubCommandDescriptor(
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
                sender.sendMessage(usage)
            }
        }

        @JvmField
        internal val bakedSubNames: Array<Array<String>> = names.map { it.bakeSubName() }.toTypedArray()
        private fun parseArgs(sender: CommandSender, rawArgs: Array<out Any>, offset: Int): Array<out Any> {
            @Suppress("CAST_NEVER_SUCCEEDS")
            this as CompositeCommand
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

internal fun <T> Array<T>.contentEqualsOffset(other: Array<out Any>, length: Int): Boolean {
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
    when (this::class.java) { // faster than is
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

internal inline fun <T : Any> KClass<out T>.getInstance(): T {
    return this.objectInstance ?: this.createInstance()
}

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

import net.mamoe.mirai.console.command.description.*
import net.mamoe.mirai.console.command.description.CommandParam
import net.mamoe.mirai.console.command.description.CommandParserContext
import net.mamoe.mirai.console.command.description.EmptyCommandParserContext
import net.mamoe.mirai.console.command.description.plus
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import java.lang.Exception
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.*

/**
 * 指令
 * 通常情况下, 你的指令应继承 @see CompositeCommand/SimpleCommand
 * @see register 注册这个指令
 */
interface Command {
    val names: Array<out String>

    fun getPrimaryName():String = names[0]

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
    vararg names: String,
    override val description: String = "no description available",
    override val permission: CommandPermission = CommandPermission.Default,
    override val prefixOptional: Boolean = false,
    overrideContext: CommandParserContext = EmptyCommandParserContext
) : Command {

    class IllegalParameterException(message:String): Exception(message)


    override val names: Array<out String> =
        names.map(String::trim).filterNot(String::isEmpty).map(String::toLowerCase).toTypedArray()
    val context: CommandParserContext = CommandParserContext.Builtins + overrideContext

    override lateinit var usage: String

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
                false//not supported yet
            }
        )
    }

    internal val subCommands: Array<SubCommandDescriptor> by lazy {

        val buildUsage = StringBuilder(this.description).append(": \n")

        this@CompositeCommand::class.declaredFunctions.filter { it.hasAnnotation<SubCommand>() }.map { function ->

            val notStatic = function.findAnnotation<JvmStatic>()==null
            val overridePermission = function.findAnnotation<Permission>()//optional
            val subDescription = function.findAnnotation<Description>()?.description?:"no description available"

            if((function.returnType.classifier as? KClass<*>)?.isSubclassOf(Boolean::class) != true){
                throw IllegalParameterException("Return Type of SubCommand must be Boolean")
            }

            val parameter = function.parameters.toMutableList()
            if (parameter.isEmpty()){
                throw IllegalParameterException("First parameter (receiver for kotlin) for sub commend " + function.name + " from " + this.getPrimaryName() + " should be <out CommandSender>")
            }

            if(notStatic){
                parameter.removeAt(0)
            }

            (parameter.removeAt(0)).let {receiver ->
                if (
                    receiver.isVararg ||
                    ((receiver.type.classifier as? KClass<*>).also { print(it) }
                        ?.isSubclassOf(CommandSender::class) != true)
                ) {
                    throw IllegalParameterException("First parameter (receiver for kotlin) for sub commend " + function.name + " from " + this.getPrimaryName() + " should be <out CommandSender>")
                }
            }

            val commandName = function.findAnnotation<SubCommand>()!!.name.map {
                if(!it.isValidSubName()){
                    error("SubName $it is not valid")
                }
                it
            }.toTypedArray()

            //map parameter
            val parms = parameter.map {
                buildUsage.append("/" + getPrimaryName() + " ")

                if(it.isVararg){
                    throw IllegalParameterException("parameter for sub commend " + function.name + " from " + this.getPrimaryName() + " should not be var arg")
                }
                if(it.isOptional){
                    throw IllegalParameterException("parameter for sub commend " + function.name + " from " + this.getPrimaryName() + " should not be var optional")
                }

                val argName = it.findAnnotation<Name>()?.name?:it.name?:"unknown"
                buildUsage.append("<").append(argName).append("> ")
                CommandParam(
                    argName,
                    (it.type.classifier as? KClass<*>)?: throw IllegalParameterException("unsolved type reference from param " + it.name + " in " + function.name + " from " + this.getPrimaryName()))
            }.toTypedArray()

            buildUsage.append(subDescription).append("\n")

            SubCommandDescriptor(
                commandName,
                parms,
                subDescription,
                overridePermission?.permission?.getInstance() ?: permission,
                onCommand = block { sender: CommandSender, args: Array<out Any> ->
                    if(notStatic) {
                        function.callSuspend(this,sender, *args) as Boolean
                    }else{
                        function.callSuspend(sender, *args) as Boolean
                    }
                }
            )

        }.toTypedArray().also {
            usage = buildUsage.toString()
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
                sender.sendMessage(usage)
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

internal inline fun <T:Any> KClass<out T>.getInstance():T {
    return this.objectInstance ?: this.createInstance()
}


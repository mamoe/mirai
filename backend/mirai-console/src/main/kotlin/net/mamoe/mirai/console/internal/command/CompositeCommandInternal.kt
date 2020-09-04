/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.console.internal.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.description.CommandArgumentContext
import net.mamoe.mirai.console.command.description.CommandArgumentContextAware
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.buildMessageChain
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

internal object CompositeCommandSubCommandAnnotationResolver :
    AbstractReflectionCommand.SubCommandAnnotationResolver {
    override fun hasAnnotation(baseCommand: AbstractReflectionCommand, function: KFunction<*>) =
        function.hasAnnotation<CompositeCommand.SubCommand>()

    override fun getSubCommandNames(baseCommand: AbstractReflectionCommand, function: KFunction<*>): Array<out String> =
        function.findAnnotation<CompositeCommand.SubCommand>()!!.value
}

internal object SimpleCommandSubCommandAnnotationResolver :
    AbstractReflectionCommand.SubCommandAnnotationResolver {
    override fun hasAnnotation(baseCommand: AbstractReflectionCommand, function: KFunction<*>) =
        function.hasAnnotation<SimpleCommand.Handler>()

    override fun getSubCommandNames(baseCommand: AbstractReflectionCommand, function: KFunction<*>): Array<out String> =
        baseCommand.names
}

internal abstract class AbstractReflectionCommand @JvmOverloads constructor(
    owner: CommandOwner,
    names: Array<out String>,
    description: String = "<no description available>",
    permission: CommandPermission = CommandPermission.Default,
    prefixOptional: Boolean = false
) : Command, AbstractCommand(
    owner,
    names = names,
    description = description,
    permission = permission,
    prefixOptional = prefixOptional
), CommandArgumentContextAware {
    internal abstract val subCommandAnnotationResolver: SubCommandAnnotationResolver

    @JvmField
    @Suppress("PropertyName")
    internal var _usage: String = "<not yet initialized>"

    override val usage: String  // initialized by subCommand reflection
        get() {
            subCommands // ensure init
            return _usage
        }

    abstract suspend fun CommandSender.onDefault(rawArgs: MessageChain)

    internal val defaultSubCommand: DefaultSubCommandDescriptor by lazy {
        DefaultSubCommandDescriptor(
            "",
            permission,
            onCommand = { sender: CommandSender, args: MessageChain ->
                sender.onDefault(args)
            }
        )
    }

    internal open fun checkSubCommand(subCommands: Array<SubCommandDescriptor>) {

    }

    interface SubCommandAnnotationResolver {
        fun hasAnnotation(baseCommand: AbstractReflectionCommand, function: KFunction<*>): Boolean
        fun getSubCommandNames(baseCommand: AbstractReflectionCommand, function: KFunction<*>): Array<out String>
    }

    internal val subCommands: Array<SubCommandDescriptor> by lazy {
        this::class.declaredFunctions.filter { subCommandAnnotationResolver.hasAnnotation(this, it) }
            .also { subCommandFunctions ->
                // overloading not yet supported
                val overloadFunction = subCommandFunctions.groupBy { it.name }.entries.firstOrNull { it.value.size > 1 }
                if (overloadFunction != null) {
                    error("Sub command overloading is not yet supported. (at ${this::class.qualifiedNameOrTip}.${overloadFunction.key})")
                }
            }.map { function ->
                createSubCommand(function, context)
            }.toTypedArray().also {
                _usage = it.createUsage(this)
            }.also { checkSubCommand(it) }
    }

    internal val bakedCommandNameToSubDescriptorArray: Map<Array<String>, SubCommandDescriptor> by lazy {
        kotlin.run {
            val map = LinkedHashMap<Array<String>, SubCommandDescriptor>(subCommands.size * 2)
            for (descriptor in subCommands) {
                for (name in descriptor.bakedSubNames) {
                    map[name] = descriptor
                }
            }
            map.toSortedMap { o1, o2 -> o1!!.contentHashCode() - o2!!.contentHashCode() }
        }
    }

    internal class DefaultSubCommandDescriptor(
        val description: String,
        val permission: CommandPermission,
        val onCommand: suspend (sender: CommandSender, rawArgs: MessageChain) -> Unit
    )

    internal inner class SubCommandDescriptor(
        val names: Array<out String>,
        val params: Array<CommandParameter<*>>,
        val description: String,
        val permission: CommandPermission,
        val onCommand: suspend (sender: CommandSender, parsedArgs: Array<out Any>) -> Boolean,
        val context: CommandArgumentContext
    ) {
        val usage: String = createUsage(this@AbstractReflectionCommand)
        internal suspend fun parseAndExecute(
            sender: CommandSender,
            argsWithSubCommandNameNotRemoved: MessageChain,
            removeSubName: Boolean
        ) {
            val args = parseArgs(sender, argsWithSubCommandNameNotRemoved, if (removeSubName) names.size else 0)
            if (!this.permission.testPermission(sender)) {
                sender.sendMessage(usage) // TODO: 2020/8/26 #127
                return
            }
            if (args == null || !onCommand(sender, args)) {
                sender.sendMessage(usage)
            }
        }

        @JvmField
        internal val bakedSubNames: Array<Array<String>> = names.map { it.bakeSubName() }.toTypedArray()
        private fun parseArgs(sender: CommandSender, rawArgs: MessageChain, offset: Int): Array<out Any>? {
            if (rawArgs.size < offset + this.params.size)
                return null
            //require(rawArgs.size >= offset + this.params.size) { "No enough args. Required ${params.size}, but given ${rawArgs.size - offset}" }

            return Array(this.params.size) { index ->
                val param = params[index]
                val rawArg = rawArgs[offset + index]
                when (rawArg) {
                    is PlainText -> context[param.type]?.parse(rawArg.content, sender)
                    else -> context[param.type]?.parse(rawArg, sender)
                } ?: error("Cannot find a parser for $rawArg")
            }
        }
    }

    /**
     * @param rawArgs 元素类型必须为 [SingleMessage] 或 [String], 且已经经过扁平化处理. 否则抛出异常 [IllegalArgumentException]
     */
    internal fun matchSubCommand(rawArgs: MessageChain): SubCommandDescriptor? {
        val maxCount = rawArgs.size
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

internal fun <T> Array<T>.contentEqualsOffset(other: MessageChain, length: Int): Boolean {
    repeat(length) { index ->
        if (!other[index].toString().equals(this[index].toString(), ignoreCase = true)) {
            return false
        }
    }
    return true
}

internal val ILLEGAL_SUB_NAME_CHARS = "\\/!@#$%^&*()_+-={}[];':\",.<>?`~".toCharArray()
internal fun String.isValidSubName(): Boolean = ILLEGAL_SUB_NAME_CHARS.none { it in this }
internal fun String.bakeSubName(): Array<String> = split(' ').filterNot { it.isBlank() }.toTypedArray()

internal fun Any.flattenCommandComponents(): MessageChain = buildMessageChain {
    when (this@flattenCommandComponents) {
        is PlainText -> this@flattenCommandComponents.content.splitToSequence(' ').filterNot { it.isBlank() }
            .forEach { +it }
        is CharSequence -> this@flattenCommandComponents.splitToSequence(' ').filterNot { it.isBlank() }.forEach { +it }
        is SingleMessage -> +(this@flattenCommandComponents)
        is Array<*> -> this@flattenCommandComponents.forEach { if (it != null) addAll(it.flattenCommandComponents()) }
        is Iterable<*> -> this@flattenCommandComponents.forEach { if (it != null) addAll(it.flattenCommandComponents()) }
        else -> add(this@flattenCommandComponents.toString())
    }
}

internal inline fun <reified T : Annotation> KAnnotatedElement.hasAnnotation(): Boolean =
    findAnnotation<T>() != null

internal inline fun <T : Any> KClass<out T>.getInstance(): T {
    return this.objectInstance ?: this.createInstance()
}

internal val KClass<*>.qualifiedNameOrTip: String get() = this.qualifiedName ?: "<anonymous class>"

internal fun Array<AbstractReflectionCommand.SubCommandDescriptor>.createUsage(baseCommand: AbstractReflectionCommand): String =
    buildString {
        appendLine(baseCommand.description)
        appendLine()

        for (subCommandDescriptor in this@createUsage) {
            appendLine(subCommandDescriptor.usage)
        }
    }.trimEnd()

internal fun AbstractReflectionCommand.SubCommandDescriptor.createUsage(baseCommand: AbstractReflectionCommand): String =
    buildString {
        if (!baseCommand.prefixOptional) {
            append("(")
            append(CommandManager.commandPrefix)
            append(")")
        } else {
            append(CommandManager.commandPrefix)
        }
        if (baseCommand is CompositeCommand) {
            append(baseCommand.primaryName)
            append(" ")
        }
        append(names.first())
        append(" ")
        append(params.joinToString(" ") { "<${it.name}>" })
        append("   ")
        append(description)
        appendLine()
    }.trimEnd()

internal fun AbstractReflectionCommand.createSubCommand(
    function: KFunction<*>,
    context: CommandArgumentContext
): AbstractReflectionCommand.SubCommandDescriptor {
    val notStatic = !function.hasAnnotation<JvmStatic>()
    val overridePermission = function.findAnnotation<CompositeCommand.Permission>()//optional
    val subDescription =
        function.findAnnotation<CompositeCommand.Description>()?.value ?: ""

    fun KClass<*>.isValidReturnType(): Boolean {
        return when (this) {
            Boolean::class, Void::class, Unit::class, Nothing::class -> true
            else -> false
        }
    }

    check((function.returnType.classifier as? KClass<*>)?.isValidReturnType() == true) {
        error("Return type of sub command ${function.name} must be one of the following: kotlin.Boolean, java.lang.Boolean, kotlin.Unit (including implicit), kotlin.Nothing, boolean or void (at ${this::class.qualifiedNameOrTip}.${function.name})")
    }

    check(!function.returnType.isMarkedNullable) {
        error("Return type of sub command ${function.name} must not be marked nullable in Kotlin, and must be marked with @NotNull or @NonNull explicitly in Java. (at ${this::class.qualifiedNameOrTip}.${function.name})")
    }

    val parameters = function.parameters.toMutableList()

    if (notStatic) parameters.removeAt(0) // instance

    var hasSenderParam = false
    check(parameters.isNotEmpty()) {
        "Parameters of sub command ${function.name} must not be empty. (Must have CommandSender as its receiver or first parameter or absent, followed by naturally typed params) (at ${this::class.qualifiedNameOrTip}.${function.name})"
    }

    parameters.forEach { param ->
        check(!param.isVararg) {
            "Parameter $param must not be vararg. (at ${this::class.qualifiedNameOrTip}.${function.name}.$param)"
        }
    }

    (parameters.first()).let { receiver ->
        if ((receiver.type.classifier as? KClass<*>)?.isSubclassOf(CommandSender::class) == true) {
            hasSenderParam = true
            parameters.removeAt(0)
        }
    }

    val commandName =
        subCommandAnnotationResolver.getSubCommandNames(this, function)
            .let { namesFromAnnotation ->
                if (namesFromAnnotation.isNotEmpty()) {
                    namesFromAnnotation.map(String::toLowerCase).toTypedArray()
                } else arrayOf(function.name.toLowerCase())
            }.also { names ->
                names.forEach {
                    check(it.isValidSubName()) {
                        "Name of sub command ${function.name} is invalid"
                    }
                }
            }

    //map parameter
    val params = parameters.map { param ->

        if (param.isOptional) error("optional parameters are not yet supported. (at ${this::class.qualifiedNameOrTip}.${function.name}.$param)")

        val paramName = param.findAnnotation<CompositeCommand.Name>()?.value ?: param.name ?: "unknown"
        CommandParameter(
            paramName,
            (param.type.classifier as? KClass<*>)
                ?: throw IllegalArgumentException("unsolved type reference from param " + param.name + ". (at ${this::class.qualifiedNameOrTip}.${function.name}.$param)")
        )
    }.toTypedArray()

    return SubCommandDescriptor(
        commandName,
        params,
        subDescription,
        overridePermission?.value?.getInstance() ?: permission,
        onCommand = { sender: CommandSender, args: Array<out Any> ->
            val result = if (notStatic) {
                if (hasSenderParam) {
                    function.isSuspend
                    function.callSuspend(this, sender, *args)
                } else function.callSuspend(this, *args)
            } else {
                if (hasSenderParam) {
                    function.callSuspend(sender, *args)
                } else function.callSuspend(*args)
            }

            checkNotNull(result) { "sub command return value is null (at ${this::class.qualifiedName}.${function.name})" }

            result as? Boolean ?: true // Unit, void is considered as true.
        },
        context = context
    )
}
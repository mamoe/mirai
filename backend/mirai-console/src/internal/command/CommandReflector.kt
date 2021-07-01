/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.internal.data.classifierAsKClass
import net.mamoe.mirai.console.internal.data.classifierAsKClassOrNull
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.buildMessageChain
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*


internal val ILLEGAL_SUB_NAME_CHARS = "\\/!@#$%^&*()_+-={}[];':\",.<>?`~".toCharArray()

internal fun Any.flattenCommandComponents(): MessageChain = buildMessageChain {
    when (this@flattenCommandComponents) {
        is PlainText -> this@flattenCommandComponents.content.splitToSequence(' ').filterNot { it.isBlank() }
            .forEach { +PlainText(it) }
        is CharSequence -> this@flattenCommandComponents.splitToSequence(' ').filterNot { it.isBlank() }
            .forEach { +PlainText(it) }
        is SingleMessage -> add(this@flattenCommandComponents)
        is Array<*> -> this@flattenCommandComponents.forEach { if (it != null) addAll(it.flattenCommandComponents()) }
        is Iterable<*> -> this@flattenCommandComponents.forEach { if (it != null) addAll(it.flattenCommandComponents()) }
        else -> add(this@flattenCommandComponents.toString())
    }
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal object CompositeCommandSubCommandAnnotationResolver :
    SubCommandAnnotationResolver {
    override fun hasAnnotation(ownerCommand: Command, function: KFunction<*>) =
        function.hasAnnotation<CompositeCommand.SubCommand>()

    override fun getSubCommandNames(ownerCommand: Command, function: KFunction<*>): Array<out String> {
        val annotated = function.findAnnotation<CompositeCommand.SubCommand>()!!.value
        return if (annotated.isEmpty()) arrayOf(function.name)
        else annotated
    }

    override fun getAnnotatedName(ownerCommand: Command, parameter: KParameter): String? =
        parameter.findAnnotation<CompositeCommand.Name>()?.value

    override fun getDescription(ownerCommand: Command, function: KFunction<*>): String? =
        function.findAnnotation<CompositeCommand.Description>()?.value
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
internal object SimpleCommandSubCommandAnnotationResolver :
    SubCommandAnnotationResolver {
    override fun hasAnnotation(ownerCommand: Command, function: KFunction<*>) =
        function.hasAnnotation<SimpleCommand.Handler>()

    override fun getSubCommandNames(ownerCommand: Command, function: KFunction<*>): Array<out String> =
        emptyArray()

    override fun getAnnotatedName(ownerCommand: Command, parameter: KParameter): String? =
        parameter.findAnnotation<SimpleCommand.Name>()?.value

    override fun getDescription(ownerCommand: Command, function: KFunction<*>): String =
        ownerCommand.description
}

internal interface SubCommandAnnotationResolver {
    fun hasAnnotation(ownerCommand: Command, function: KFunction<*>): Boolean
    fun getSubCommandNames(ownerCommand: Command, function: KFunction<*>): Array<out String>
    fun getAnnotatedName(ownerCommand: Command, parameter: KParameter): String?
    fun getDescription(ownerCommand: Command, function: KFunction<*>): String?
}

@ConsoleExperimentalApi
public class IllegalCommandDeclarationException : Exception {
    public override val message: String?

    public constructor(
        ownerCommand: Command,
        correspondingFunction: KFunction<*>,
        message: String?,
    ) : super("Illegal command declaration: ${correspondingFunction.name} declared in ${ownerCommand::class.qualifiedName}") {
        this.message = message
    }

    public constructor(
        ownerCommand: Command,
        message: String?,
    ) : super("Illegal command declaration: ${ownerCommand::class.qualifiedName}") {
        this.message = message
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
internal class CommandReflector(
    val command: Command,
    val annotationResolver: SubCommandAnnotationResolver,
) {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun KFunction<*>.illegalDeclaration(
        message: String,
    ): Nothing {
        throw IllegalCommandDeclarationException(command, this, message)
    }

    private fun KFunction<*>.isSubCommandFunction(): Boolean = annotationResolver.hasAnnotation(command, this)
    private fun KFunction<*>.checkExtensionReceiver() {
        this.extensionReceiverParameter?.let { receiver ->
            if (receiver.type.classifierAsKClassOrNull()?.isSubclassOf(CommandSender::class) != true) {
                illegalDeclaration("Extension receiver parameter type is not subclass of CommandSender.")
            }
        }
    }

    private fun KFunction<*>.checkNames() {
        val names = annotationResolver.getSubCommandNames(command, this)
        for (name in names) {
            ILLEGAL_SUB_NAME_CHARS.find { it in name }?.let {
                illegalDeclaration("'$it' is forbidden in command name.")
            }
        }
    }

    private fun KFunction<*>.checkModifiers() {
        if (isInline) illegalDeclaration("Command function cannot be inline")
        if (visibility == KVisibility.PRIVATE) illegalDeclaration("Command function must be accessible from Mirai Console, that is, effectively public.")
        if (this.hasAnnotation<JvmStatic>()) illegalDeclaration("Command function must not be static.")

        // should we allow abstract?

        // if (isAbstract) illegalDeclaration("Command function cannot be abstract")
    }

    fun generateUsage(overloads: Iterable<CommandSignatureFromKFunction>): String {
        return generateUsage(command, annotationResolver, overloads)
    }

    companion object {
        fun generateUsage(
            command: Command,
            annotationResolver: SubCommandAnnotationResolver?,
            overloads: Iterable<CommandSignature>
        ): String {
            return overloads.joinToString("\n") { subcommand ->
                buildString {
                    if (command.prefixOptional) {
                        append("(")
                        append(CommandManager.commandPrefix)
                        append(")")
                    } else {
                        append(CommandManager.commandPrefix)
                    }
                    //if (command is CompositeCommand) {
                    append(command.primaryName)
                    append(" ")
                    //}
                    append(subcommand.valueParameters.joinToString(" ") { it.render() })
                    if (annotationResolver != null && subcommand is CommandSignatureFromKFunction) {
                        annotationResolver.getDescription(command, subcommand.originFunction)?.let { description ->
                            append("    # ")
                            append(description)
                        }
                    }
                }
            }
        }

        private fun <T> AbstractCommandValueParameter<T>.render(): String {
            return when (this) {
                is AbstractCommandValueParameter.Extended,
                is AbstractCommandValueParameter.UserDefinedType<*>,
                -> {
                    val nameToRender = this.name ?: this.type.classifierAsKClass().simpleName
                    if (isOptional) "[$nameToRender]" else "<$nameToRender>"
                }
                is AbstractCommandValueParameter.StringConstant -> {
                    this.expectingValue
                }
            }
        }
    }

    fun validate(signatures: List<CommandSignatureFromKFunctionImpl>) {

        data class ErasedParameterInfo(
            val index: Int,
            val name: String?,
            val type: KType, // ignore nullability
            val additional: String?,
        )

        data class ErasedVariantInfo(
            val receiver: ErasedParameterInfo?,
            val valueParameters: List<ErasedParameterInfo>,
        )

        fun CommandParameter<*>.toErasedParameterInfo(index: Int): ErasedParameterInfo {
            return ErasedParameterInfo(
                index,
                this.name,
                this.type.withNullability(false),
                if (this is AbstractCommandValueParameter.StringConstant) this.expectingValue else null
            )
        }

        val candidates = signatures.map { variant ->
            variant to ErasedVariantInfo(
                variant.receiverParameter?.toErasedParameterInfo(0),
                variant.valueParameters.mapIndexed { index, parameter -> parameter.toErasedParameterInfo(index) }
            )
        }

        val groups = candidates.groupBy { it.second }

        val clashes = groups.entries.find { (_, value) ->
            value.size > 1
        } ?: return

        throw CommandDeclarationClashException(command, clashes.value.map { it.first })
    }

    @Throws(IllegalCommandDeclarationException::class)
    fun findSubCommands(): List<CommandSignatureFromKFunctionImpl> {
        return command::class.functions // exclude static later
            .asSequence()
            .filter { it.isSubCommandFunction() }
            .onEach { it.checkExtensionReceiver() }
            .onEach { it.checkModifiers() }
            .onEach { it.checkNames() }
            .flatMap { function ->
                val names = annotationResolver.getSubCommandNames(command, function)
                if (names.isEmpty()) sequenceOf(createMapEntry(null, function))
                else names.associateWith { function }.asSequence()
            }
            .map { (name, function) ->

                val functionNameAsValueParameter =
                    name?.split(' ')?.mapIndexed { index, s -> createStringConstantParameterForName(index, s) }
                        .orEmpty()

                val valueParameters = function.valueParameters.toMutableList()
                var receiverParameter = function.extensionReceiverParameter
                if (receiverParameter == null && valueParameters.isNotEmpty()) {
                    val valueFirstParameter = valueParameters[0]
                    if (valueFirstParameter.type.classifierAsKClassOrNull()
                            ?.isSubclassOf(CommandSender::class) == true
                    ) {
                        receiverParameter = valueFirstParameter
                        valueParameters.removeAt(0)
                    }
                }

                val functionValueParameters =
                    valueParameters.associateBy { it.toUserDefinedCommandParameter() }

                CommandSignatureFromKFunctionImpl(
                    receiverParameter = receiverParameter?.toCommandReceiverParameter(),
                    valueParameters = functionNameAsValueParameter + functionValueParameters.keys,
                    originFunction = function
                ) { call ->
                    val args = LinkedHashMap<KParameter, Any?>()

                    for ((commandParameter, value) in call.resolvedValueArguments) {
                        if (commandParameter is AbstractCommandValueParameter.StringConstant) {
                            continue
                        }
                        val functionParameter =
                            functionValueParameters[commandParameter]
                                ?: error("Could not find a corresponding function parameter '${commandParameter.name}'")
                        args[functionParameter] = value
                    }

                    val instanceParameter = function.instanceParameter
                    if (instanceParameter != null) {
                        check(instanceParameter.type.classifierAsKClass().isInstance(command)) {
                            "Bad command call resolved. " +
                                    "Function expects instance parameter ${instanceParameter.type} whereas actual instance is ${command::class}."
                        }
                        args[instanceParameter] = command
                    }

                    if (receiverParameter != null) {
                        check(receiverParameter.type.classifierAsKClass().isInstance(call.caller)) {
                            "Bad command call resolved. " +
                                    "Function expects receiver parameter ${receiverParameter.type} whereas actual is ${call.caller::class}."
                        }
                        args[receiverParameter] = call.caller
                    }
                    function.callSuspendBy(args)
                }
            }.toList()
    }

    private fun <K, V> createMapEntry(key: K, value: V) = object : Map.Entry<K, V> {
        override val key: K get() = key
        override val value: V get() = value
    }

    private fun KParameter.toCommandReceiverParameter(): CommandReceiverParameter<out CommandSender> {
        check(!this.isVararg) { "Receiver cannot be vararg." }
        check(
            this.type.classifierAsKClass().isSubclassOf(CommandSender::class)
        ) { "Receiver must be subclass of CommandSender" }

        return CommandReceiverParameter(this.type.isMarkedNullable, this.type)
    }

    private fun createStringConstantParameterForName(
        index: Int,
        expectingValue: String
    ): AbstractCommandValueParameter.StringConstant {
        return AbstractCommandValueParameter.StringConstant("#$index", expectingValue, true)
    }

    private fun KParameter.toUserDefinedCommandParameter(): AbstractCommandValueParameter.UserDefinedType<*> {
        return AbstractCommandValueParameter.UserDefinedType<Any?>(
            nameForCommandParameter(),
            this.isOptional,
            this.isVararg,
            this.type
        ) // Any? is erased
    }

    private fun KParameter.nameForCommandParameter(): String? =
        annotationResolver.getAnnotatedName(command, this) ?: this.name
}
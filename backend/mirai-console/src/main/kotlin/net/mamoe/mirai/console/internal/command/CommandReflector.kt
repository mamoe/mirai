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

    override fun getSubCommandNames(ownerCommand: Command, function: KFunction<*>): Array<out String> =
        function.findAnnotation<CompositeCommand.SubCommand>()!!.value

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
        ownerCommand.secondaryNames

    override fun getAnnotatedName(ownerCommand: Command, parameter: KParameter): String? =
        parameter.findAnnotation<SimpleCommand.Name>()?.value

    override fun getDescription(ownerCommand: Command, function: KFunction<*>): String? =
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

    fun generateUsage(overloads: Iterable<CommandSignatureVariantFromKFunction>): String {
        return overloads.joinToString("\n") { subcommand ->
            buildString {
                if (command.prefixOptional) {
                    append("(")
                    append(CommandManager.commandPrefix)
                    append(")")
                } else {
                    append(CommandManager.commandPrefix)
                }
                if (command is CompositeCommand) {
                    append(command.primaryName)
                    append(" ")
                }
                append(subcommand.valueParameters.joinToString(" ") { it.render() })
                annotationResolver.getDescription(command, subcommand.originFunction).let { description ->
                    append("   ")
                    append(description)
                }
            }
        }
    }


    companion object {

        private fun <T> CommandValueParameter<T>.render(): String {
            return when (this) {
                is CommandValueParameter.Extended,
                is CommandValueParameter.UserDefinedType<*>,
                -> {
                    "<${this.name ?: this.type.classifierAsKClass().simpleName}>"
                }
                is CommandValueParameter.StringConstant -> {
                    this.expectingValue
                }
            }
        }
    }

    @Throws(IllegalCommandDeclarationException::class)
    fun findSubCommands(): List<CommandSignatureVariantFromKFunctionImpl> {
        return command::class.functions // exclude static later
            .asSequence()
            .filter { it.isSubCommandFunction() }
            .onEach { it.checkExtensionReceiver() }
            .onEach { it.checkModifiers() }
            .onEach { it.checkNames() }
            .map { function ->

                val functionNameAsValueParameter =
                    annotationResolver.getSubCommandNames(command, function).map { createStringConstantParameter(it) }

                val functionValueParameters =
                    function.valueParameters.map { it.toUserDefinedCommandParameter() }

                CommandSignatureVariantFromKFunctionImpl(
                    receiverParameter = function.extensionReceiverParameter?.toCommandReceiverParameter(),
                    valueParameters = functionNameAsValueParameter + functionValueParameters,
                    originFunction = function
                ) { call ->
                    function.callSuspend(command, *call.resolvedValueArguments.toTypedArray())
                }
            }.toList()
    }

    private fun KParameter.toCommandReceiverParameter(): CommandReceiverParameter<out CommandSender>? {
        check(!this.isVararg) { "Receiver cannot be vararg." }
        check(this.type.classifierAsKClass().isSubclassOf(CommandSender::class)) { "Receiver must be subclass of CommandSender" }

        return CommandReceiverParameter(this.type.isMarkedNullable, this.type)
    }

    private fun createStringConstantParameter(expectingValue: String): CommandValueParameter.StringConstant {
        return CommandValueParameter.StringConstant(null, expectingValue)
    }

    private fun KParameter.toUserDefinedCommandParameter(): CommandValueParameter.UserDefinedType<KParameter> {
        return CommandValueParameter.UserDefinedType(nameForCommandParameter(), this, this.isOptional, this.isVararg, this.type)
    }

    private fun KParameter.nameForCommandParameter(): String? = annotationResolver.getAnnotatedName(command, this) ?: this.name
}
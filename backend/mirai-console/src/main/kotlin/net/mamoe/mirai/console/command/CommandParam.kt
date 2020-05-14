@file:Suppress("unused")

package net.mamoe.mirai.console.command

import kotlin.reflect.KClass

/**
 * 指令形式参数.
 */
data class CommandParam<T : Any>(
    /**
     * 参数名. 不允许重复.
     */
    val name: String,
    /**
     * 参数类型. 将从 [CommandDescriptor.context] 中寻找 [CommandArgParser] 解析.
     */
    val type: KClass<T> // exact type
) {
    constructor(name: String, type: KClass<T>, parser: CommandArgParser<T>) : this(name, type) {
        this._overrideParser = parser
    }

    @Suppress("PropertyName")
    @JvmField
    internal var _overrideParser: CommandArgParser<T>? = null


    /**
     * 覆盖的 [CommandArgParser].
     *
     * 如果非 `null`, 将不会从 [CommandParserContext] 寻找 [CommandArgParser]
     *
     * @see Command.parserFor
     */
    val overrideParser: CommandArgParser<T>? get() = _overrideParser
}

fun <T : Any> CommandParam<T>.parserFrom(command: Command): CommandArgParser<T>? = command.parserFor(this)

fun <T : Any> CommandParam<T>.parserFrom(descriptor: CommandDescriptor): CommandArgParser<T>? =
    descriptor.parserFor(this)

fun <T : Any> CommandParam<T>.parserFrom(context: CommandParserContext): CommandArgParser<T>? = context.parserFor(this)
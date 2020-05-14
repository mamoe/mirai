@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KClass

/**
 * 指令描述. 包含名称, 权限要求, 参数解析器环境, 参数列表.
 */
class CommandDescriptor(
    /**
     * 包含子命令的全名. 如 "`group kick`", 其中 `kick` 为 `group` 的子命令
     */
    fullName: CommandFullName,
    /**
     * 用法说明
     */
    usage: String,
    /**
     * 指令参数列表, 有顺序.
     */
    val params: List<CommandParam<*>>,
    /**
     * 指令说明
     */
    description: String = "",
    /**
     * 指令参数解析器环境.
     */
    val context: CommandParserContext = CommandParserContext.Builtins,
    /**
     * 指令别名
     */
    aliases: Array<CommandFullName> = arrayOf(),
    /**
     * 指令权限
     *
     * @see CommandPermission.or 要求其中一个权限
     * @see CommandPermission.and 同时要求两个权限
     */
    val permission: CommandPermission = CommandPermission.Default
) {
    /**
     * 指令别名
     */
    val aliases: Array<CommandFullName> = aliases.map { it.checkFullName("alias") }.toTypedArray()

    /**
     * 指令说明
     */
    val description: String = description.trim()

    /**
     * 用法说明
     */
    val usage: String = usage.trim()

    /**
     * 包含子命令的全名. 如 "`group kick`", 其中 `kick` 为 `group` 的子命令
     * 元素类型可以为 [Message] 或 [String]
     */
    val fullName: CommandFullName = fullName.checkFullName("fullName")

    /**
     * `fullName + aliases`
     */
    val allNames: Array<CommandFullName> = arrayOf(fullName, *aliases)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandDescriptor

        if (!fullName.contentEquals(other.fullName)) return false
        if (usage != other.usage) return false
        if (params != other.params) return false
        if (description != other.description) return false
        if (context != other.context) return false
        if (!aliases.contentEquals(other.aliases)) return false
        if (permission != other.permission) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fullName.hashCode()
        result = 31 * result + usage.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + context.hashCode()
        result = 31 * result + aliases.contentHashCode()
        result = 31 * result + permission.hashCode()
        return result
    }
}


fun Command.checkArgs(args: CommandArgs) = this.descriptor.checkArgs(args)
fun CommandDescriptor.checkArgs(args: CommandArgs) {
    require(args.size >= this.params.size) { "No enough args. Required ${params.size}, but given ${args.size}" }
    params.forEachIndexed { index, commandParam ->
        require(commandParam.type.isInstance(args[index])) {
            "Illegal arg #$index, required ${commandParam.type.qualifiedName}, but given ${args[index]::class.qualifiedName}"
        }
    }
}

internal fun Any.flattenCommandComponents(): Sequence<Any> = when (this) {
    is Array<*> -> this.asSequence().flatMap {
        it?.flattenCommandComponents() ?: throw java.lang.IllegalArgumentException("unexpected null value")
    }
    is String -> splitToSequence(' ').filterNot { it.isBlank() }
    is PlainText -> content.flattenCommandComponents()
    is SingleMessage -> sequenceOf(this)
    is MessageChain -> this.asSequence().flatMap { it.flattenCommandComponents() }
    else -> throw IllegalArgumentException("Illegal component: $this")
}

internal fun CommandFullName.checkFullName(errorHint: String): CommandFullName {
    return flattenCommandComponents().toList().also {
        require(it.isNotEmpty()) { "$errorHint must not be empty" }
    }.toTypedArray()
}

/**
 * 构建一个 [CommandDescriptor]
 */
@Suppress("FunctionName")
inline fun CommandDescriptor(
    vararg fullName: Any,
    block: CommandDescriptorBuilder.() -> Unit = {}
): CommandDescriptor = CommandDescriptorBuilder(*fullName).apply(block).build()

class CommandDescriptorBuilder(
    vararg fullName: Any
) {
    @PublishedApi
    internal var fullName: CommandFullName = fullName.checkFullName("fullName")

    @PublishedApi
    internal var context: CommandParserContext = CommandParserContext.Builtins

    @PublishedApi
    internal var permission: CommandPermission = CommandPermission.Default

    @PublishedApi
    internal var params: MutableList<CommandParam<*>> = mutableListOf()

    @PublishedApi
    internal var usage: String = "<no usage>"

    @PublishedApi
    internal var aliases: MutableList<CommandFullName> = mutableListOf()

    @PublishedApi
    internal var description: String = ""

    /** 增加指令参数解析器列表 */
    @JvmSynthetic
    inline fun context(block: CommandParserContextBuilder.() -> Unit) {
        this.context += CommandParserContext(block)
    }

    /** 增加指令参数解析器列表 */
    @JvmSynthetic
    inline fun context(context: CommandParserContext): CommandDescriptorBuilder = apply {
        this.context += context
    }

    /** 设置权限要求 */
    fun permission(permission: CommandPermission): CommandDescriptorBuilder = apply {
        this.permission = permission
    }

    /** 设置权限要求 */
    @JvmSynthetic
    inline fun permission(crossinline block: CommandSender.() -> Boolean) {
        this.permission = AnonymousCommandPermission(block)
    }

    fun usage(message: String): CommandDescriptorBuilder = apply {
        usage = message
    }

    fun description(description: String): CommandDescriptorBuilder = apply {
        this.description = description
    }

    /**
     * 添加一个别名
     */
    fun alias(vararg fullName: Any): CommandDescriptorBuilder = apply {
        this.aliases.add(fullName)
    }

    fun param(vararg params: CommandParam<*>): CommandDescriptorBuilder = apply {
        this.params.addAll(params)
    }

    @JvmSynthetic
    fun <T : Any> param(
        name: String?,
        type: KClass<T>,
        overrideParser: CommandArgParser<T>? = null
    ): CommandDescriptorBuilder = apply {
        this.params.add(CommandParam(name, type).apply { this._overrideParser = overrideParser })
    }

    fun <T : Any> param(
        name: String?,
        type: Class<T>,
        overrideParser: CommandArgParser<T>? = null
    ): CommandDescriptorBuilder =
        param(name, type, overrideParser)

    inline fun <reified T : Any> param(
        name: String? = null,
        overrideParser: CommandArgParser<T>? = null
    ): CommandDescriptorBuilder =
        param(name, T::class, overrideParser)

    @JvmSynthetic
    fun param(vararg pairs: Pair<String?, KClass<*>>): CommandDescriptorBuilder = apply {
        for (pair in pairs) {
            this.params.add(CommandParam(pair.first, pair.second))
        }
    }

    @JvmSynthetic
    fun params(block: ParamBlock.() -> Unit): CommandDescriptorBuilder = apply {
        ParamBlock(params).apply(block)
    }

    @JvmSynthetic
    fun param(type: KClass<*>): CommandDescriptorBuilder = apply {
        this.params.add(CommandParam(null, type))
    }

    fun param(type: Class<*>): CommandDescriptorBuilder = apply {
        this.params.add(CommandParam(null, type.kotlin))
    }

    fun build(): CommandDescriptor =
        CommandDescriptor(fullName, usage, params, description, context, aliases.toTypedArray(), permission)
}

@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class ParamBlock internal constructor(@PublishedApi internal val list: MutableList<CommandParam<*>>) {
    /** 添加一个名称为 [this], 类型为 [klass] 的参数. 返回添加成功的对象 */
    infix fun <T : Any> String.typed(klass: KClass<T>): CommandParam<T> =
        CommandParam(this, klass).also { list.add(it) }

    /** 指定 [CommandParam.overrideParser] */
    infix fun <T : Any> CommandParam<T>.using(parser: CommandArgParser<T>): CommandParam<T> =
        this.apply { this._overrideParser = parser }

    /** 覆盖 [CommandArgParser] */
    inline infix fun <reified T : Any> String.using(parser: CommandArgParser<T>): CommandParam<T> =
        this typed T::class using parser
}
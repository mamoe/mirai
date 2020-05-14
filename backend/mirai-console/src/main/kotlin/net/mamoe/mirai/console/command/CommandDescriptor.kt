@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage

/**
 * 指令描述. 包含名称, 权限要求, 参数解析器环境, 参数列表.
 *
 * 这是指令系统较低级的 API. 大部分情况下请使用 [Command]
 */
class CommandDescriptor(
    /**
     * 子指令列表. 第一个元素为默认值.
     */
    val subCommands: List<SubCommandDescriptor>,
    /**
     * 是否建议 console 将这个指令强制注册为需要带 [前缀][CommandPrefix] 的指令.
     */
    val suggestForcePrefix: Boolean = true,
    /** 覆盖内建的指令参数解析器环境. */
    overrideContext: CommandParserContext = CommandParserContext.Empty
) {
    /** 子指令描述 */
    inner class SubCommandDescriptor(
        /** 子指令名, 如 "/mute group add" 中的 "group add". 当表示默认指令时为父指令名. */
        val subName: String,
        /** 用法说明 */
        val usage: String,
        /** 指令参数列表, 有顺序. */
        val params: List<CommandParam<*>>,
        /** 指令说明 */
        val description: String,
        /** 指令别名 */
        val aliases: Array<String> = arrayOf(),
        /**
         * 指令权限
         * @see CommandPermission.or 要求其中一个权限
         * @see CommandPermission.and 同时要求两个权限
         */
        val permission: CommandPermission = CommandPermission.Default,
        /** 指令执行器 */
        val onCommand: suspend (sender: CommandSender, args: CommandArgs) -> Boolean
    ) {
        init {
            require(!(subName.startsWith(' ') || subName.endsWith(' '))) { "subName mustn't start or end with a caret" }
            require(subName.isValidSubName()) { "subName mustn't contain any of $ILLEGAL_SUB_NAME_CHARS" }
        }

        @JvmField
        internal val bakedSubNames: Array<Array<String>> =
            listOf(subName, *aliases).map { it.bakeSubName() }.toTypedArray()
        internal inline val parent: CommandDescriptor get() = this@CommandDescriptor
    }

    /**
     * 指令参数解析器环境.
     */
    val context: CommandParserContext = CommandParserContext.Builtins + overrideContext
}

internal val CommandDescriptor.base: CommandDescriptor.SubCommandDescriptor get() = subCommands[0]


internal val ILLEGAL_SUB_NAME_CHARS = "\\/!@#$%^&*()_+-={}[];':\",.<>?`~".toCharArray()
internal fun String.isValidSubName(): Boolean = ILLEGAL_SUB_NAME_CHARS.none { it in this }
internal fun String.bakeSubName(): Array<String> = split(' ').filterNot { it.isBlank() }.toTypedArray()


/**
 * 检查指令参数数量是否足够, 类型是否匹配.
 * @throws IllegalArgumentException
 */
fun CommandDescriptor.SubCommandDescriptor.checkArgs(args: CommandArgs) {
    require(args.size >= this.params.size) { "No enough args. Required ${params.size}, but given ${args.size}" }
    params.forEachIndexed { index, commandParam ->
        require(commandParam.type.isInstance(args[index])) {
            "Illegal arg #$index, required ${commandParam.type.qualifiedName}, but given ${args[index]::class.qualifiedName}"
        }
    }
}


/*
/**
 * 构建一个 [CommandDescriptor]
 */
@Suppress("FunctionName")
inline fun CommandDescriptor(
    /**
     * 指令全名
     */
    vararg fullNameComponents: String,
    block: CommandDescriptorBuilder.() -> Unit = {}
): CommandDescriptor = CommandDescriptorBuilder(*fullNameComponents).apply(block).build()

class CommandDescriptorBuilder(
    vararg fullName: String
) {
    @PublishedApi
    internal var fullName: Array<String> = fullName.checkFullName("fullName")

    @PublishedApi
    internal var context: CommandParserContext = CommandParserContext.Builtins

    @PublishedApi
    internal var permission: CommandPermission = CommandPermission.Default

    @PublishedApi
    internal var params: MutableList<CommandParam<*>> = mutableListOf()

    @PublishedApi
    internal var usage: String = "<no usage>"

    @PublishedApi
    internal var aliases: MutableList<Array<String>> = mutableListOf()

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
     * @param fullName 全名称. 见 [CommandDescriptor.fullName]
     */
    fun alias(fullName: String): CommandDescriptorBuilder = apply {
        this.aliases.add(fullName.checkFullName("fullName"))
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
    fun param(vararg types: KClass<*>): CommandDescriptorBuilder = apply {
        types.forEach { type -> params.add(CommandParam(null, type)) }
    }

    fun param(vararg types: Class<*>): CommandDescriptorBuilder = apply {
        types.forEach { type -> params.add(CommandParam(null, type.kotlin)) }
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

 */

///////
/// internal


internal fun Any.flattenCommandComponents(): List<String> {
    val list = ArrayList<String>()
    when (this) {
        is String -> list.addAll(split(' ').filterNot { it.isBlank() })
        is PlainText -> list.addAll(content.flattenCommandComponents())
        is SingleMessage -> list.add(this.toString())
        is MessageChain -> this.asSequence().forEach { list.addAll(it.flattenCommandComponents()) }
        else -> throw IllegalArgumentException("Illegal component: $this")
    }
    return list
}

internal fun Any.checkFullName(errorHint: String): Array<String> {
    return flattenCommandComponents().toList().also {
        require(it.isNotEmpty()) { "$errorHint must not be empty" }
    }.toTypedArray()
}
@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import kotlin.reflect.KClass

/**
 * 指令描述. 包含名称, 权限要求, 参数解析器环境, 参数列表.
 */
class CommandDescriptor(
    /**
     * 包含子命令的全名. 如 "`group kick`", 其中 `kick` 为 `group` 的子命令
     */
    val fullName: String,
    /**
     * 指令参数解析器环境.
     */
    val context: CommandParserContext,
    /**
     * 指令参数列表, 有顺序.
     */
    val params: List<CommandParam<*>>,
    /**
     * 指令权限
     *
     * @see CommandPermission.or 要求其中一个权限
     * @see CommandPermission.and 同时要求两个权限
     */
    val permission: CommandPermission = CommandPermission.Default
)

/**
 * 构建一个 [CommandDescriptor]
 */
@Suppress("FunctionName")
inline fun CommandDescriptor(
    fullName: String,
    block: CommandDescriptorBuilder.() -> Unit
): CommandDescriptor = CommandDescriptorBuilder(fullName).apply(block).build()

class CommandDescriptorBuilder(
    val fullName: String
) {
    @PublishedApi
    internal var context: CommandParserContext = CommandParserContext.Builtins

    @PublishedApi
    internal var permission: CommandPermission = CommandPermission.Default

    @PublishedApi
    internal var params: MutableList<CommandParam<*>> = mutableListOf()

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

    fun param(vararg params: CommandParam<*>): CommandDescriptorBuilder = apply {
        this.params.addAll(params)
    }

    @JvmSynthetic
    fun <T : Any> param(
        name: String?,
        type: KClass<T>,
        overrideParser: CommandArgParser<T>? = null
    ): CommandDescriptorBuilder = apply {
        this.params.add(CommandParam(name, type).apply { this.parser = overrideParser })
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

    fun build(): CommandDescriptor = CommandDescriptor(fullName, context, params, permission)
}

@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class ParamBlock internal constructor(@PublishedApi internal val list: MutableList<CommandParam<*>>) {
    /** 添加一个名称为 [this], 类型为 [klass] 的参数. 返回添加成功的对象 */
    infix fun <T : Any> String.typed(klass: KClass<T>): CommandParam<T> =
        CommandParam(this, klass).also { list.add(it) }

    /** 指定 [CommandParam.overrideParser] */
    infix fun <T : Any> CommandParam<T>.using(parser: CommandArgParser<T>): CommandParam<T> =
        this.apply { this.parser = parser }

    /** 覆盖 [CommandArgParser] */
    inline infix fun <reified T : Any> String.using(parser: CommandArgParser<T>): CommandParam<T> =
        this typed T::class using parser
}
@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.AbstractCommandParserContext.Node
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import kotlin.internal.LowPriorityInOverloadResolution
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
 * 指令形式参数.
 */
data class CommandParam<T : Any>(
    /**
     * 参数名, 为 `null` 时即为匿名参数.
     * 参数名允许重复 (尽管并不建议这样做).
     * 参数名仅提供给 [CommandArgParser] 以发送更好的错误信息.
     */
    val name: String?,
    /**
     * 参数类型. 将从 [CommandDescriptor.context] 中寻找 [CommandArgParser] 解析.
     */
    val type: KClass<T> // exact type
) {
    constructor(name: String?, type: KClass<T>, parser: CommandArgParser<T>) : this(name, type) {
        this.parser = parser
    }

    @JvmField
    internal var parser: CommandArgParser<T>? = null


    /**
     * 覆盖的 [CommandArgParser].
     *
     * 如果非 `null`, 将不会从 [CommandParserContext] 寻找 [CommandArgParser]
     */
    val overrideParser: CommandArgParser<T>? get() = parser
}

private fun preview() {
    class MyArg(val string: String)

    CommandDescriptor("test") {
        permission(CommandPermission.GroupOwner or CommandPermission.Console)

        permission {
            println("正在检查 $this 的权限")
            true
        }

        param<String>("test")
        param<Boolean>("test")
        param<String>("test")

        param("p2", String::class)
        param("p2", String::class)

        params {
            "p2" typed String::class
            "testPram" typed CharSequence::class using StringArgParser
            "p3" using StringArgParser
        }

        context {
            MyArg::class with {
                println("正在解析 $it")
                MyArg(it)
            }
        }
    }
}

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

    fun param(name: String?, type: KClass<*>): CommandDescriptorBuilder = apply {
        this.params.add(CommandParam(name, type))
    }

    inline fun <reified T : Any> param(name: String? = null): CommandDescriptorBuilder = apply {
        this.params.add(CommandParam(name, T::class))
    }

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

inline class ParamBlock(@PublishedApi internal val list: MutableList<CommandParam<*>>) {
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

/**
 * [KClass] 到 [CommandArgParser] 的匹配
 */
interface CommandParserContext {
    operator fun <T : Any> get(klass: KClass<T>): CommandArgParser<T>?

    /**
     * 内建的默认 [CommandArgParser]
     */
    object Builtins : CommandParserContext by (CommandParserContext {
        Int::class with IntArgParser
        Byte::class with ByteArgParser
        Short::class with ShortArgParser
        Boolean::class with BooleanArgParser
        String::class with StringArgParser
        Long::class with LongArgParser
        Double::class with DoubleArgParser
        Float::class with FloatArgParser

        Member::class with ExistMemberArgParser
        Group::class with ExistGroupArgParser
        Bot::class with ExistBotArgParser
    })
}

fun <T : Any> CommandParserContext.parserFor(param: CommandParam<T>): CommandArgParser<T>? = this[param.type]

/**
 * 合并两个 [CommandParserContext], [replacer] 将会替换 [this] 中重复的 parser.
 */
operator fun CommandParserContext.plus(replacer: CommandParserContext): CommandParserContext {
    return object : CommandParserContext {
        override fun <T : Any> get(klass: KClass<T>): CommandArgParser<T>? = replacer[klass] ?: this@plus[klass]
    }
}

@Suppress("UNCHECKED_CAST")
open class AbstractCommandParserContext(val list: List<Node<*>>) : CommandParserContext {
    class Node<T : Any>(
        val klass: KClass<T>,
        val parser: CommandArgParser<T>
    )

    override fun <T : Any> get(klass: KClass<T>): CommandArgParser<T>? =
        this.list.firstOrNull { it.klass == klass }?.parser as CommandArgParser<T>?
}

/**
 * 构建一个 [CommandParserContext].
 *
 * ```
 * CommandParserContext {
 *     Int::class with IntArgParser
 *     Member::class with ExistMemberArgParser
 *     Group::class with { s: String, sender: CommandSender ->
 *          Bot.getInstance(s.toLong()).getGroup(s.toLong())
 *     }
 *     Bot::class with { s: String ->
 *          Bot.getInstance(s.toLong())
 *     }
 * }
 * ```
 */
@Suppress("FunctionName")
@JvmSynthetic
inline fun CommandParserContext(block: CommandParserContextBuilder.() -> Unit): CommandParserContext {
    return AbstractCommandParserContext(
        CommandParserContextBuilder().apply(block).distinctByReversed { it.klass })
}

/**
 * @see CommandParserContext
 */
class CommandParserContextBuilder : MutableList<Node<*>> by mutableListOf() {
    @JvmName("add")
    inline infix fun <T : Any> KClass<T>.with(parser: CommandArgParser<T>): Node<*> =
        Node(this, parser)

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T
    ): Node<*> = Node(this, CommandArgParser(parser))

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgParser<T>.(s: String) -> T
    ): Node<*> = Node(this, CommandArgParser { s: String, _: CommandSender -> parser(s) })
}


@PublishedApi
internal inline fun <T, K> Iterable<T>.distinctByReversed(selector: (T) -> K): List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (i in list.indices.reversed()) {
        val element = list[i]
        if (set.add(element.let(selector))) {
            list.add(element)
        }
    }
    return list
}
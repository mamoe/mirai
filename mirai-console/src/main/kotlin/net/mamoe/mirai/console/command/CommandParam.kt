@file:Suppress("unused")

package net.mamoe.mirai.console.command

import kotlin.reflect.KClass

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

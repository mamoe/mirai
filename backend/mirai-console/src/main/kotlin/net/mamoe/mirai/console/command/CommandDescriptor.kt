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
    val names: Array<String> = arrayOf(),

    val subCommands: List<SubCommandDescriptor>,

    val prefixOptional: Boolean = false,
    /** 覆盖内建的指令参数解析器环境. */
    overrideContext: CommandParserContext = CommandParserContext.Empty
) {
    /** 子指令描述 */
    inner class SubCommandDescriptor(
        /** 子指令名, 如 "/mute group add" 中的 "group add". 当表示默认指令时为父指令名. 包含别名*/
        val names: Array<String> = arrayOf(),
        /** 用法说明 */
        val usage: String,
        /** 指令参数列表, 有顺序. */
        val params: List<CommandParam<*>>,
        /** 指令说明 */
        val description: String,
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
            names.forEach {subName ->
                require(!(subName.startsWith(' ') || subName.endsWith(' '))) { "subName mustn't start or end with a caret" }
                require(subName.isValidSubName()) { "subName mustn't contain any of $ILLEGAL_SUB_NAME_CHARS" }
            }
        }

        @JvmField
        internal val bakedSubNames: Array<Array<String>> = names.map { it.bakeSubName() }.toTypedArray()
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
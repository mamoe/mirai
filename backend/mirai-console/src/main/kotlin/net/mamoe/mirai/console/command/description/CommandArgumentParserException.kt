@file:Suppress("unused")

package net.mamoe.mirai.console.command.description

/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范等.
 *
 * [message] 将会发送给指令调用方.
 *
 * @see CommandArgumentParser
 * @see CommandArgumentParser.illegalArgument
 */
public class CommandArgumentParserException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}
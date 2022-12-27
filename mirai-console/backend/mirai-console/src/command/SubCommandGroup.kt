package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.CommandSignatureFromKFunction
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

public interface SubCommandGroup {

    /**
     * 被聚合时提供的子指令
     */
    @ExperimentalCommandDescriptors
    public val overloads: List<@JvmWildcard CommandSignatureFromKFunction>

    /**
     * 标记一个属性为子指令集合，且使用flat策略
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.PROPERTY)
    public annotation class FlattenSubCommands(
    )

    /**
     * 1. 标记一个函数为子指令, 当 [value] 为空时使用函数名.
     * 2. 标记一个属性为子指令集合，且使用sub策略
     * @param value 子指令名
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    public annotation class SubCommand(
        @ResolveContext(ResolveContext.Kind.COMMAND_NAME) vararg val value: String = [],
    )

    /** 指令描述 */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    public annotation class Description(val value: String)

    /** 参数名, 由具体Command决定用途 */
    @ConsoleExperimentalApi("Classname might change")
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    public annotation class Name(val value: String)

}
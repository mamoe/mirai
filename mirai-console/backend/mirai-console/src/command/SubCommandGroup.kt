package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.CommandSignatureFromKFunction
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

public interface SubCommandGroup {

    /**
     * 被聚合时提供的子指令
     */
    @ExperimentalCommandDescriptors
    public val overloads: List<@JvmWildcard CommandSignatureFromKFunction>

}
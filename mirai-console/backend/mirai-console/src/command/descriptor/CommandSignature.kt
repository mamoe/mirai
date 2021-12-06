/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.reflect.KFunction

/**
 * 指令签名. 表示指令定义的需要的参数.
 *
 * @see AbstractCommandSignature
 */
@ExperimentalCommandDescriptors
public interface CommandSignature {
    /**
     * 接收者参数, 为 [CommandSender] 子类
     */
    @ConsoleExperimentalApi
    public val receiverParameter: CommandReceiverParameter<out CommandSender>?

    /**
     * 形式 值参数.
     */
    public val valueParameters: List<AbstractCommandValueParameter<*>>

    /**
     * 调用这个指令.
     */
    public suspend fun call(resolvedCommandCall: ResolvedCommandCall)
}

/**
 * 来自 [KFunction] 反射得到的 [CommandSignature]
 *
 * @see CommandSignatureFromKFunctionImpl
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public interface CommandSignatureFromKFunction : CommandSignature {
    public val originFunction: KFunction<*>
}

/**
 * @see CommandSignatureImpl
 * @see CommandSignatureFromKFunctionImpl
 */
@ExperimentalCommandDescriptors
public abstract class AbstractCommandSignature : CommandSignature {
    override fun toString(): String {
        val receiverParameter = receiverParameter
        return if (receiverParameter == null) {
            "CommandSignature(${valueParameters.joinToString()})"
        } else {
            "CommandSignature($receiverParameter, ${valueParameters.joinToString()})"
        }
    }
}

@ExperimentalCommandDescriptors
public open class CommandSignatureImpl(
    override val receiverParameter: CommandReceiverParameter<out CommandSender>?,
    override val valueParameters: List<AbstractCommandValueParameter<*>>,
    private val onCall: suspend CommandSignatureImpl.(resolvedCommandCall: ResolvedCommandCall) -> Unit,
) : CommandSignature, AbstractCommandSignature() {
    override suspend fun call(resolvedCommandCall: ResolvedCommandCall) {
        return onCall(resolvedCommandCall)
    }
}

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public open class CommandSignatureFromKFunctionImpl(
    override val receiverParameter: CommandReceiverParameter<out CommandSender>?,
    override val valueParameters: List<AbstractCommandValueParameter<*>>,
    override val originFunction: KFunction<*>,
    private val onCall: suspend CommandSignatureFromKFunctionImpl.(resolvedCommandCall: ResolvedCommandCall) -> Unit,
) : CommandSignatureFromKFunction, AbstractCommandSignature() {
    override suspend fun call(resolvedCommandCall: ResolvedCommandCall) {
        return onCall(resolvedCommandCall)
    }
}
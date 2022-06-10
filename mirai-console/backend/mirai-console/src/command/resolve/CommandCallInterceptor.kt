/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.command.resolve

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandCallParser
import net.mamoe.mirai.console.extensions.CommandCallInterceptorProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.util.UNREACHABLE_CLAUSE
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.message.data.Message
import org.jetbrains.annotations.Contract
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * 指令解析和调用拦截器. 用于在指令各解析阶段拦截或转换调用.
 */
@ExperimentalCommandDescriptors
public interface CommandCallInterceptor {
    /**
     * 在指令[语法解析][CommandCallParser]前调用.
     *
     * @return `null` 表示未处理
     */
    public fun interceptBeforeCall(
        message: Message,
        caller: CommandSender,
    ): InterceptResult<Message>? = null

    /**
     * 在指令[语法解析][CommandCallParser]后调用.
     *
     * @return `null` 表示未处理
     */
    public fun interceptCall(
        call: CommandCall,
    ): InterceptResult<CommandCall>? = null

    /**
     * 在指令[调用解析][CommandCallResolver]后调用.
     *
     * @return `null` 表示未处理
     */
    public fun interceptResolvedCall(
        call: ResolvedCommandCall,
    ): InterceptResult<ResolvedCommandCall>? = null

    public companion object {
        /**
         * 使用 [CommandCallInterceptor] 依次调用 [interceptBeforeCall].
         * 在第一个拦截时返回拦截原因, 在所有 [CommandCallInterceptor] 都处理完成后返回结果 [Message]
         */
        @JvmStatic
        public fun Message.intercepted(caller: CommandSender): InterceptResult<Message> {
            return GlobalComponentStorage.foldExtensions(CommandCallInterceptorProvider, this@intercepted) { acc, ext ->
                val intercepted = ext.instance.interceptBeforeCall(acc, caller)
                intercepted?.fold(
                    onIntercepted = { return intercepted },
                    otherwise = { it }
                ) ?: acc
            }.let { InterceptResult(it) }
        }

        /**
         * 使用 [CommandCallInterceptor] 依次调用 [interceptBeforeCall].
         * 在第一个拦截时返回拦截原因, 在所有 [CommandCallInterceptor] 都处理完成后返回结果 [CommandCall]
         */
        @JvmStatic
        public fun CommandCall.intercepted(): InterceptResult<CommandCall> {
            return GlobalComponentStorage.foldExtensions(CommandCallInterceptorProvider, this@intercepted) { acc, ext ->
                val intercepted = ext.instance.interceptCall(acc)
                intercepted?.fold(
                    onIntercepted = { return intercepted },
                    otherwise = { it }
                ) ?: acc
            }.let { InterceptResult(it) }
        }

        /**
         * 使用 [CommandCallInterceptor] 依次调用 [interceptBeforeCall].
         * 在第一个拦截时返回拦截原因, 在所有 [CommandCallInterceptor] 都处理完成后返回结果 [ResolvedCommandCall]
         */
        @JvmStatic
        public fun ResolvedCommandCall.intercepted(): InterceptResult<ResolvedCommandCall> {
            return GlobalComponentStorage.foldExtensions(CommandCallInterceptorProvider, this@intercepted) { acc, ext ->
                val intercepted = ext.instance.interceptResolvedCall(acc)
                intercepted?.fold(
                    onIntercepted = { return intercepted },
                    otherwise = { it }
                ) ?: acc
            }.let { InterceptResult(it) }
        }
    }
}

/**
 * [CommandCallInterceptor] 拦截结果
 */
@ExperimentalCommandDescriptors
public class InterceptResult<T> internal constructor(
    private val _value: Any?,
    @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?,
) {
    /**
     * 构造一个 [InterceptResult], 以 [value] 继续处理后续指令执行.
     */
    public constructor(value: T) : this(value as Any?, null)

    /**
     * 构造一个 [InterceptResult], 以 [原因][reason] 中断指令执行.
     */
    public constructor(reason: InterceptedReason) : this(reason as Any?, null)

    @get:Contract(pure = true)
    public val value: T?
        @Suppress("UNCHECKED_CAST")
        get() {
            val value = this._value
            return if (value is InterceptedReason) null else value as T
        }

    @get:Contract(pure = true)
    public val reason: InterceptedReason?
        get() = this._value.safeCast()
}

@ExperimentalCommandDescriptors
public inline fun <T, R> InterceptResult<T>.fold(
    onIntercepted: (reason: InterceptedReason) -> R,
    otherwise: (call: T) -> R,
): R {
    contract {
        callsInPlace(onIntercepted, InvocationKind.AT_MOST_ONCE)
        callsInPlace(otherwise, InvocationKind.AT_MOST_ONCE)
    }
    value?.let(otherwise)
    reason?.let(onIntercepted)
    UNREACHABLE_CLAUSE
}

@ExperimentalCommandDescriptors
public inline fun <T : R, R> InterceptResult<T>.getOrElse(onIntercepted: (reason: InterceptedReason) -> R): R {
    contract { callsInPlace(onIntercepted, InvocationKind.AT_MOST_ONCE) }
    reason?.let(onIntercepted)
    return value!!
}

/**
 * 创建一个 [InterceptedReason]
 *
 * @see InterceptedReason.create
 */
@ExperimentalCommandDescriptors
@JvmSynthetic
public inline fun InterceptedReason(message: String): InterceptedReason = InterceptedReason.create(message)

/**
 * 拦截原因
 */
@ExperimentalCommandDescriptors
public interface InterceptedReason {
    public val message: String

    public companion object {
        /**
         * 创建一个 [InterceptedReason]
         */
        public fun create(message: String): InterceptedReason = InterceptedReasonData(message)
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
@Serializable
private data class InterceptedReasonData(override val message: String) : InterceptedReason

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command.resolve

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandCallParser
import net.mamoe.mirai.console.extensions.CommandCallInterceptorProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * 指令解析和调用拦截器. 用于在指令各解析阶段拦截或转换调用.
 */
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
                    onProceed = { it }
                ) ?: acc
            }.let { InterceptResult.proceed(it) }
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
                    onProceed = { it }
                ) ?: acc
            }.let { InterceptResult.proceed(it) }
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
                    onProceed = { it }
                ) ?: acc
            }.let { InterceptResult.proceed(it) }
        }
    }
}


/**
 * [CommandCallInterceptor] 拦截结果
 */
public class InterceptResult<T> private constructor(
    @Suppress("PropertyName")
    @PublishedApi
    internal val _value: Any?,
) {
    /**
     * 当未被拦截时返回未被拦截的值, 否则返回 `null`.
     */
    public val value: T?
        get() {
            val value = this._value
            @Suppress("UNCHECKED_CAST")
            return if (value is InterceptedReason) null else value as T
        }

    /**
     * 获取拦截原因, 当未被拦截时返回 `null`.
     */
    public val reason: InterceptedReason?
        get() = this._value.safeCast()

    /**
     * 当被拦截时返回 `true`, 否则返回 `false`.
     * @since 2.15
     */
    public val isIntercepted: Boolean get() = _value is InterceptedReason

    public companion object {
        /**
         * 构造一个 [InterceptResult], 以 [value] 继续处理后续指令执行.
         */
        @JvmStatic
        @JvmName("proceed")
        public fun <T> proceed(value: T): InterceptResult<T> = InterceptResult(value)

        /**
         * 构造一个 [InterceptResult], 以 [原因][reason] 中断指令执行.
         */
        @JvmStatic
        @JvmName("intercepted")
        public fun <T> intercepted(reason: InterceptedReason): InterceptResult<T> = InterceptResult(reason)
    }
}

public inline fun <T, R> InterceptResult<T>.fold(
    onIntercepted: (reason: InterceptedReason) -> R,
    onProceed: (call: T) -> R,
): R {
    contract {
        callsInPlace(onIntercepted, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onProceed, InvocationKind.AT_MOST_ONCE)
    }
    val value = _value
    if (value is InterceptedReason) {
        return onIntercepted(value)
    } else {
        @Suppress("UNCHECKED_CAST")
        return onProceed(value as T)
    }
}

/**
 * 当 [InterceptResult] 为未被拦截时, 返回值. 当为被拦截时, 计算并返回 [onIntercepted].
 */
public inline fun <T : R, R> InterceptResult<T>.getOrElse(onIntercepted: (reason: InterceptedReason) -> R): R {
    contract { callsInPlace(onIntercepted, InvocationKind.AT_MOST_ONCE) }
    val value = _value
    if (value is InterceptedReason) {
        return onIntercepted(value)
    }
    @Suppress("UNCHECKED_CAST")
    return value as R
}

/**
 * 拦截原因
 */
@NotStableForInheritance
public interface InterceptedReason {
    public val message: String

    public companion object {
        /**
         * 创建一个 [InterceptedReason]
         */
        @JvmStatic
        public fun simple(message: String): InterceptedReason = InterceptedReasonData(message)
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
private data class InterceptedReasonData(override val message: String) : InterceptedReason

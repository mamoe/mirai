/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("unused", "NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.DeprecationLevel.ERROR
import kotlin.js.JsName


///////////////////////////////////////////////////////////////////////////
// Deprecated
///////////////////////////////////////////////////////////////////////////

/**
 * 得到包含 [this] 的 [MessageChain].
 */
@Suppress("UNCHECKED_CAST")
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun Message.asMessageChain(): MessageChain = toMessageChain()

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun SingleMessage.asMessageChain(): MessageChain = toMessageChain()

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun Collection<SingleMessage>.asMessageChain(): MessageChain = toMessageChain()

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmSynthetic
@JvmName("newChain1")
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
// @JsName("newChain")
public fun Array<out Message>.asMessageChain(): MessageChain = toMessageChain()

@JvmSynthetic
@JvmName("newChain2")
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun Array<out SingleMessage>.asMessageChain(): MessageChain = toMessageChain()

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmName("newChain")
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
// @JsName("newChain")
public fun Collection<Message>.asMessageChain(): MessageChain = toMessageChain()

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun Iterable<SingleMessage>.asMessageChain(): MessageChain = toMessageChain()

@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public inline fun MessageChain.asMessageChain(): MessageChain = this

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
// @JsName("newChain")
@JvmName("asMessageChainMessage")
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun Iterable<Message>.asMessageChain(): MessageChain = toMessageChain()

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
public fun Sequence<SingleMessage>.asMessageChain(): MessageChain = toMessageChain()

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmName("asMessageChainMessage")
@JvmSynthetic
@PlannedRemoval("2.0.0")
@Deprecated(
    "Use toMessageChain.",
    ReplaceWith("this.toMessageChain()", "net.mamoe.mirai.message.data.toMessageChain"),
    ERROR
)
// @JsName("newChain")
public fun Sequence<Message>.asMessageChain(): MessageChain = toMessageChain()


/**
 * 扁平化消息序列.
 *
 * 原 [this]:
 * ```
 * A <- MessageChain(B, C) <- D <- MessageChain(E, F, G)
 * ```
 * 结果 [Sequence]:
 * ```
 * A <- B <- C <- D <- E <- F <- G
 * ```
 */
@Deprecated("flatten is deprecated.", ReplaceWith("this.toMessageChain().asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
public inline fun Iterable<Message>.flatten(): Sequence<SingleMessage> = toMessageChain().asSequence()

// @JsName("flatten1")
@Deprecated("flatten is deprecated.", ReplaceWith("this.asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
@JvmName("flatten1")// avoid platform declare clash
@JvmSynthetic
public inline fun Iterable<SingleMessage>.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

/**
 * 扁平化消息序列.
 *
 * 原 [this]:
 * ```
 * A <- MessageChain(B, C) <- D <- MessageChain(E, F, G)
 * ```
 * 结果 [Sequence]:
 * ```
 * A <- B <- C <- D <- E <- F <- G
 * ```
 */
@Deprecated("flatten is deprecated.", ReplaceWith("this.toMessageChain().asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
public inline fun Sequence<Message>.flatten(): Sequence<SingleMessage> = this.toMessageChain().asSequence()

@Deprecated("flatten is deprecated.", ReplaceWith("this"), ERROR)
@PlannedRemoval("2.0.0")
@JsName("flatten1") // avoid platform declare clash
@JvmName("flatten1")
@JvmSynthetic
public inline fun Sequence<SingleMessage>.flatten(): Sequence<SingleMessage> = this // fast path

@Deprecated("flatten is deprecated.", ReplaceWith("this.toMessageChain().asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
public inline fun Array<out Message>.flatten(): Sequence<SingleMessage> = this.toMessageChain().asSequence()

@Deprecated("flatten is deprecated.", ReplaceWith("this.asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
public inline fun Array<out SingleMessage>.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

/**
 * 返回 [MessageChain.asSequence] 或 `sequenceOf(this as SingleMessage)`
 */
@Deprecated("flatten is deprecated.", ReplaceWith("this.toMessageChain().asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
public fun Message.flatten(): Sequence<SingleMessage> = this.toMessageChain().asSequence()

@Deprecated("flatten is deprecated.", ReplaceWith("this.asSequence()"), ERROR)
@PlannedRemoval("2.0.0")
@JvmSynthetic // make Java user happier with less methods
public inline fun MessageChain.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path
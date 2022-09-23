/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("CloseableKt_common")

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

public expect interface Closeable {
    @Throws(IOException::class)
    public fun close()
}


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
public expect inline fun <C : Closeable, R> C.use(block: (C) -> R): R

// overcome overload resolution ambiguity
public inline fun <C : Input, R> C.use(block: (C) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return this.asMiraiCloseable().use { block(this) }
}

// overcome overload resolution ambiguity
public inline fun <C : Output, R> C.use(block: (C) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return this.asMiraiCloseable().use { block(this) }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
public inline fun <C : Closeable, R> C.withUse(block: C.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use(block)
}

// `Input` does not implement `Closeable` in common
public inline fun <C : Input, R> C.withUse(block: C.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use(block)
}

// `Output` does not implement `Closeable` in common
public inline fun <C : Output, R> C.withUse(block: C.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use(block)
}

public inline fun <I : Closeable, O : Closeable, R> I.withOut(output: O, block: I.(output: O) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use { output.use { block(this, output) } }
}


public expect fun Closeable.asKtorCloseable(): io.ktor.utils.io.core.Closeable
public expect fun io.ktor.utils.io.core.Closeable.asMiraiCloseable(): Closeable
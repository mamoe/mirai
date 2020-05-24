/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.MessageDsl
import net.mamoe.mirai.event.MessageListener
import net.mamoe.mirai.event.MessageSubscribersBuilder
import net.mamoe.mirai.message.MessageEvent


/*
 * 将 internal 移出 MessageSubscribersBuilder.kt 以减小其体积
 */

@MessageDsl
internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.content(
    filter: M.(String) -> Boolean,
    onEvent: MessageListener<M, RR>
): Ret =
    subscriber(filter) { onEvent(this, it) }

internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.endsWithImpl(
    suffix: String,
    removeSuffix: Boolean = true,
    trim: Boolean = true,
    onEvent: @MessageDsl suspend M.(String) -> R
): Ret {
    return if (trim) {
        val toCheck = suffix.trim()
        content({ it.trimEnd().endsWith(toCheck) }) {
            if (removeSuffix) this.onEvent(this.message.contentToString().removeSuffix(toCheck).trim())
            else onEvent(this, this.message.contentToString().trim())
        }
    } else {
        content({ it.endsWith(suffix) }) {
            if (removeSuffix) this.onEvent(this.message.contentToString().removeSuffix(suffix))
            else onEvent(this, this.message.contentToString())
        }
    }
}

internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.startsWithImpl(
    prefix: String,
    removePrefix: Boolean = true,
    trim: Boolean = true,
    onEvent: @MessageDsl suspend M.(String) -> R
): Ret {
    return if (trim) {
        val toCheck = prefix.trim()
        content({ it.trimStart().startsWith(toCheck) }) {
            if (removePrefix) this.onEvent(this.message.contentToString().substringAfter(toCheck).trim())
            else onEvent(this, this.message.contentToString().trim())
        }
    } else content({ it.startsWith(prefix) }) {
        if (removePrefix) this.onEvent(this.message.contentToString().removePrefix(prefix))
        else onEvent(this, this.message.contentToString())
    }
}

internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.containsAllImpl(
    sub: Array<out String>,
    ignoreCase: Boolean = false,
    trim: Boolean = true
): MessageSubscribersBuilder<M, Ret, R, RR>.ListeningFilter =
    if (trim) {
        val list = sub.map { it.trim() }
        content { list.all { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) } }
    } else {
        content { sub.all { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) } }
    }

internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.containsAnyImpl(
    vararg sub: String,
    ignoreCase: Boolean = false,
    trim: Boolean = true
): MessageSubscribersBuilder<M, Ret, R, RR>.ListeningFilter =
    if (trim) {
        val list = sub.map { it.trim() }
        content { list.any { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) } }
    } else content { sub.any { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) } }

internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.caseImpl(
    equals: String,
    ignoreCase: Boolean = false,
    trim: Boolean = true
): MessageSubscribersBuilder<M, Ret, R, RR>.ListeningFilter {
    return if (trim) {
        val toCheck = equals.trim()
        content { it.trim().equals(toCheck, ignoreCase = ignoreCase) }
    } else {
        content { it.equals(equals, ignoreCase = ignoreCase) }
    }
}

internal fun <M : MessageEvent, Ret, R : RR, RR> MessageSubscribersBuilder<M, Ret, R, RR>.containsImpl(
    sub: String,
    ignoreCase: Boolean = false,
    trim: Boolean = true,
    onEvent: MessageListener<M, R>
): Ret {
    return if (trim) {
        val toCheck = sub.trim()
        content({ it.contains(toCheck, ignoreCase = ignoreCase) }) {
            onEvent(this, this.message.contentToString().trim())
        }
    } else {
        content({ it.contains(sub, ignoreCase = ignoreCase) }) {
            onEvent(this, this.message.contentToString())
        }
    }
}
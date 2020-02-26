/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.data

/**
 * 错误请求. 抛出这个异常后将会返回错误给一个请求
 */
@Suppress("unused")
open class IllegalAccessException : Exception {
    override val message: String get() = super.message!!

    constructor(message: String) : super(message, null)
    constructor(cause: Throwable) : super(cause.toString(), cause)
    constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Session失效或不存在
 */
object IllegalSessionException : IllegalAccessException("Session失效或不存在")

/**
 * Session未激活
 */
object NotVerifiedSessionException : IllegalAccessException("Session未激活")

/**
 * 指定Bot不存在
 */
object NoSuchBotException: IllegalAccessException("指定Bot不存在")

/**
 * 错误参数
 */
class IllegalParamException(message: String) : IllegalAccessException(message)
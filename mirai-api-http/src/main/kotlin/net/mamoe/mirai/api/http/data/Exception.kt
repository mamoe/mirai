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
@file:Suppress("unused", "UNUSED_PARAMETER")

package net.mamoe.mirai.utils

import net.mamoe.mirai.contact.Group

/**
 * 在获取 [Group] 对象等操作时可能出现的异常
 */
class GroupNotFoundException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

open class MiraiInternalException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
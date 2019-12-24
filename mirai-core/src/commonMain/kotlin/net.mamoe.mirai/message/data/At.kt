@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.QQ


/**
 * At 一个人. 只能发送给一个群.
 */
inline class At(val target: Long) : Message {
    constructor(target: QQ) : this(target.id)

    override fun toString(): String = "[@$target]" // TODO: 2019/11/25 使用群名称进行 at. 因为手机端只会显示这个文字

    companion object Key : Message.Key<At>
}

/**
 * At 这个成员
 */
@Suppress("NOTHING_TO_INLINE")
inline fun QQ.at(): At = At(this)
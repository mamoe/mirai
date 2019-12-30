@file:JvmMultifileClass
@file:JvmName("BotHelperKt")
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.data

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


@Suppress("ClassName")
sealed class AddFriendResult {
    abstract class DONE internal constructor() : AddFriendResult() {
        override fun toString(): String = "AddFriendResult(Done)"
    }

    /**
     * 对方拒绝添加好友
     */
    object REJECTED : AddFriendResult() {
        override fun toString(): String = "AddFriendResult(Rejected)"
    }

    /**
     * 这个人已经是好友
     */
    object ALREADY_ADDED : DONE() {
        override fun toString(): String = "AddFriendResult(AlreadyAdded)"
    }

    /**
     * 等待对方同意
     */
    object WAITING_FOR_APPROVAL : DONE() {
        override fun toString(): String = "AddFriendResult(WaitingForApproval)"
    }

    /**
     * 成功添加 (只在对方设置为允许任何人直接添加为好友时才会获得这个结果)
     */
    object ADDED : DONE() {
        override fun toString(): String = "AddFriendResult(Added)"
    }
}
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotHelperKt")
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


@MiraiExperimentalAPI
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
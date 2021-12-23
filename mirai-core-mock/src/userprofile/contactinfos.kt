/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.userprofile

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.data.UserInfo
import net.mamoe.mirai.utils.currentTimeSeconds

public interface MockUserInfoBuilder {
    public fun uin(value: Long): MockUserInfoBuilder

    public fun nick(value: String): MockUserInfoBuilder

    public fun remark(value: String): MockUserInfoBuilder

    public fun build(): UserInfo

    public companion object {
        @JvmStatic
        @JvmName("builder")
        public operator fun invoke(): MockUserInfoBuilder = ThreeInOneInfoBuilder()

        @JvmSynthetic
        public inline fun create(action: MockUserInfoBuilder.() -> Unit): UserInfo = invoke().apply(action).build()
    }
}

public interface MockFriendInfoBuilder : MockUserInfoBuilder {
    public override fun build(): FriendInfo

    override fun uin(value: Long): MockFriendInfoBuilder

    override fun nick(value: String): MockFriendInfoBuilder

    override fun remark(value: String): MockFriendInfoBuilder

    public companion object {
        @JvmStatic
        @JvmName("builder")
        public operator fun invoke(): MockFriendInfoBuilder = ThreeInOneInfoBuilder()

        @JvmSynthetic
        public inline fun create(action: MockFriendInfoBuilder.() -> Unit): FriendInfo = invoke().apply(action).build()
    }
}

public interface MockMemberInfoBuilder : MockUserInfoBuilder {
    override fun build(): MemberInfo

    public fun nameCard(value: String): MockMemberInfoBuilder

    public fun specialTitle(value: String): MockMemberInfoBuilder

    public fun anonymousId(value: String?): MockMemberInfoBuilder

    public fun joinTimestamp(value: Int): MockMemberInfoBuilder

    public fun lastSpeakTimestamp(value: Int): MockMemberInfoBuilder

    public fun isOfficialBot(value: Boolean): MockMemberInfoBuilder

    public fun permission(value: MemberPermission): MockMemberInfoBuilder

    override fun uin(value: Long): MockMemberInfoBuilder

    override fun nick(value: String): MockMemberInfoBuilder

    override fun remark(value: String): MockMemberInfoBuilder

    public companion object {
        @JvmStatic
        @JvmName("builder")
        public operator fun invoke(): MockMemberInfoBuilder = ThreeInOneInfoBuilder()

        @JvmSynthetic
        public inline fun create(action: MockMemberInfoBuilder.() -> Unit): MemberInfo = invoke().apply(action).build()
    }
}

public interface MockStrangerInfoBuilder : MockUserInfoBuilder {
    public fun fromGroup(value: Long): MockUserInfoBuilder

    override fun uin(value: Long): MockUserInfoBuilder

    override fun nick(value: String): MockUserInfoBuilder

    override fun remark(value: String): MockUserInfoBuilder

    override fun build(): StrangerInfo

    public companion object {
        @JvmStatic
        @JvmName("builder")
        public operator fun invoke(): MockStrangerInfoBuilder = ThreeInOneInfoBuilder()


        @JvmSynthetic
        public inline fun create(action: MockStrangerInfoBuilder.() -> Unit): StrangerInfo = invoke().apply(action).build()
    }
}

private class ThreeInOneInfoBuilder :
    MockUserInfoBuilder,
    MockFriendInfoBuilder,
    MockMemberInfoBuilder,
    MockStrangerInfoBuilder,

    UserInfo,
    FriendInfo,
    MemberInfo,
    StrangerInfo {

    override var nameCard: String = ""
    override var permission: MemberPermission = MemberPermission.MEMBER
    override var specialTitle: String = ""
    override var muteTimestamp: Int = 0
    override var joinTimestamp: Int = currentTimeSeconds().toInt()
    override var lastSpeakTimestamp: Int = 0
    override var isOfficialBot: Boolean = false
    override var fromGroup: Long = 0L
    override var remark: String = ""
    override var uin: Long = 0
    override var nick: String = ""
    override var anonymousId: String? = null

    override fun build(): ThreeInOneInfoBuilder = this

    override fun nameCard(value: String): ThreeInOneInfoBuilder = apply { this.nameCard = value }
    override fun specialTitle(value: String): ThreeInOneInfoBuilder = apply { this.specialTitle = value }
    override fun anonymousId(value: String?): ThreeInOneInfoBuilder = apply { this.anonymousId = value }
    override fun joinTimestamp(value: Int): ThreeInOneInfoBuilder = apply { this.joinTimestamp = value }
    override fun lastSpeakTimestamp(value: Int): ThreeInOneInfoBuilder = apply { this.lastSpeakTimestamp = value }
    override fun isOfficialBot(value: Boolean): ThreeInOneInfoBuilder = apply { this.isOfficialBot = value }
    override fun fromGroup(value: Long): ThreeInOneInfoBuilder = apply { this.fromGroup = value }
    override fun uin(value: Long): ThreeInOneInfoBuilder = apply { this.uin = value }
    override fun nick(value: String): ThreeInOneInfoBuilder = apply { this.nick = value }
    override fun remark(value: String): ThreeInOneInfoBuilder = apply { this.remark = value }
    override fun permission(value: MemberPermission): ThreeInOneInfoBuilder = apply { this.permission = value }
}

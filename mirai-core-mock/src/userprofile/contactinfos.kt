/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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

@DslMarker
private annotation class MockUserInfoDSL

@MockUserInfoDSL
public interface MockUserInfoBuilder {
    @MockUserInfoDSL
    public fun uin(value: Long): MockUserInfoBuilder

    @MockUserInfoDSL
    public fun nick(value: String): MockUserInfoBuilder

    @MockUserInfoDSL
    public fun remark(value: String): MockUserInfoBuilder

    @MockUserInfoDSL
    public fun build(): UserInfo

    public companion object {
        @JvmStatic
        @JvmName("builder")
        @MockUserInfoDSL
        public operator fun invoke(): MockUserInfoBuilder = ThreeInOneInfoBuilder()
    }
}

@MockUserInfoDSL
public interface MockFriendInfoBuilder : MockUserInfoBuilder {
    @MockUserInfoDSL
    public override fun build(): FriendInfo

    @MockUserInfoDSL
    override fun uin(value: Long): MockFriendInfoBuilder

    @MockUserInfoDSL
    override fun nick(value: String): MockFriendInfoBuilder

    @MockUserInfoDSL
    override fun remark(value: String): MockFriendInfoBuilder

    public companion object {
        @JvmStatic
        @JvmName("builder")
        @MockUserInfoDSL
        public operator fun invoke(): MockFriendInfoBuilder = ThreeInOneInfoBuilder()
    }
}

@MockUserInfoDSL
public interface MockMemberInfoBuilder : MockUserInfoBuilder {
    @MockUserInfoDSL
    override fun build(): MemberInfo

    @MockUserInfoDSL
    public fun nameCard(value: String): MockMemberInfoBuilder

    @MockUserInfoDSL
    public fun specialTitle(value: String): MockMemberInfoBuilder

    @MockUserInfoDSL
    public fun anonymousId(value: String?): MockMemberInfoBuilder

    @MockUserInfoDSL
    public fun joinTimestamp(value: Int): MockMemberInfoBuilder

    @MockUserInfoDSL
    public fun lastSpeakTimestamp(value: Int): MockMemberInfoBuilder

    @MockUserInfoDSL
    public fun isOfficialBot(value: Boolean): MockMemberInfoBuilder

    @MockUserInfoDSL
    public fun permission(value: MemberPermission): MockMemberInfoBuilder

    @MockUserInfoDSL
    override fun uin(value: Long): MockMemberInfoBuilder

    @MockUserInfoDSL
    override fun nick(value: String): MockMemberInfoBuilder

    @MockUserInfoDSL
    override fun remark(value: String): MockMemberInfoBuilder

    public companion object {
        @JvmStatic
        @JvmName("builder")
        @MockUserInfoDSL
        public operator fun invoke(): MockMemberInfoBuilder = ThreeInOneInfoBuilder()
    }
}

@MockUserInfoDSL
public interface MockStrangerInfoBuilder : MockUserInfoBuilder {
    @MockUserInfoDSL
    public fun fromGroup(value: Long): MockUserInfoBuilder

    @MockUserInfoDSL
    override fun uin(value: Long): MockUserInfoBuilder

    @MockUserInfoDSL
    override fun nick(value: String): MockUserInfoBuilder

    @MockUserInfoDSL
    override fun remark(value: String): MockUserInfoBuilder

    @MockUserInfoDSL
    override fun build(): StrangerInfo

    public companion object {
        @JvmStatic
        @JvmName("builder")
        @MockUserInfoDSL
        public operator fun invoke(): MockUserInfoBuilder = ThreeInOneInfoBuilder()
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

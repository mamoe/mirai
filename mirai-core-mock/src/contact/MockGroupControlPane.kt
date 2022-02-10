/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact

/**
 * 群设置面板, 进行的操作如果使用 [withActor] 会进行广播
 *
 * 与 [MockGroup.settings] 不同的是, 该控制面板不会进行权限校检
 */
public interface MockGroupControlPane {
    public val group: MockGroup
    public val currentActor: MockNormalMember

    public var isAllowMemberInvite: Boolean

    public var isMuteAll: Boolean

    public var isAllowMemberFileUploading: Boolean

    public var isAnonymousChatAllowed: Boolean

    public var isAllowConfessTalk: Boolean

    public var groupName: String

    public fun withActor(actor: MockNormalMember): MockGroupControlPane
}
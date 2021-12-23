/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact

/**
 * 群设置面板, 如果是由 [withActor] 得到的面板在操作的同时会进行事件广播
 *
 * 与 [MockGroup.settings] 不同的是, 该控制面板不会进行权限校检
 */
public interface MockGroupControlPane {
    public val group: MockGroup

    /**
     * 如果为 [MockGroup.controlPane] 获得的原始控制面板, 此属性为 [MockGroup.botAsMember]
     *
     * @see withActor
     */
    public val currentActor: MockNormalMember

    public var isAllowMemberInvite: Boolean

    public var isMuteAll: Boolean

    public var isAllowMemberFileUploading: Boolean

    public var isAnonymousChatAllowed: Boolean

    public var isAllowConfessTalk: Boolean

    public var groupName: String

    /**
     * 通过 [withActor] 得到的 [MockGroupControlPane] 在修改属性的同时会广播相关事件
     */
    public fun withActor(actor: MockNormalMember): MockGroupControlPane
}
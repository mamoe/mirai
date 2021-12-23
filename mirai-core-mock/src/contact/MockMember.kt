/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.mock.MockBotDSL

@JvmBlockingBridge
public interface MockMember : Member, MockContact, MockUser {
    public interface MockApi {
        public val member: MockMember
        public var nick: String
        public var remark: String
        public var permission: MemberPermission
    }

    override val group: MockGroup

    /**
     * 获取直接修改字段内容的 API, 通过该 API 修改的值都不会触发广播
     */
    public val mockApi: MockApi
}
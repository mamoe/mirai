/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.mock.MockBotDSL

@JvmBlockingBridge
public interface MockContact : Contact, MockContactOrBot {
    public interface MockApi {
        public var avatarUrl: String
    }

    /**
     * 获取直接修改字段内容的 API, 通过该 API 修改的值都不会触发广播
     */
    @MockBotDSL
    public val mockApi: MockApi

    /**
     * 修改 [avatarUrl] 的地址, 同时会广播相关事件 (如果有)
     */
    @MockBotDSL
    public fun changeAvatarUrl(newAvatar: String)
}

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.mock.utils.randomMockImage

@JvmBlockingBridge
@Suppress("unused")
public interface MockContact : Contact, MockContactOrBot {
    public var _avatarUrl: String?

    /**
     * 头像链接.
     * 直接更改本属性会广播头像更改事件(如果存在), 如果不需要广播事件直接更改[_avatarUrl].
     */
    override var avatarUrl: String
        get() {
            if (_avatarUrl == null)
                _avatarUrl = randomMockImage(bot).getUrl(bot)
            return _avatarUrl!!
        }
        set(value) {
            _avatarUrl = value
        }
}

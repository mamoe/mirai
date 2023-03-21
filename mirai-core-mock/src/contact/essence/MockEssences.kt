/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact.essence

import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.mock.MockBotDSL

public interface MockEssences : Essences {

    /**
     * 直接以 [actor] 的身份设置一条精华消息
     */
    @MockBotDSL
    public fun mockSetEssences(source: MessageSource, actor: NormalMember)
}
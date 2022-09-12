/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("MockConversions")

package net.mamoe.mirai.mock.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.OnlineAudio
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.MockBotDSL
import net.mamoe.mirai.mock.contact.*
import net.mamoe.mirai.utils.ExternalResource
import kotlin.contracts.contract


public fun Bot.mock(): MockBot {
    contract { returns() implies (this@mock is MockBot) }
    return this as MockBot
}

public fun Group.mock(): MockGroup {
    contract { returns() implies (this@mock is MockGroup) }
    return this as MockGroup
}

public fun NormalMember.mock(): MockNormalMember {
    contract { returns() implies (this@mock is MockNormalMember) }
    return this as MockNormalMember
}

public fun Contact.mock(): MockContact {
    contract { returns() implies (this@mock is MockContact) }
    return this as MockContact
}

public fun AnonymousMember.mock(): MockAnonymousMember {
    contract { returns() implies (this@mock is MockAnonymousMember) }
    return this as MockAnonymousMember
}

public fun Friend.mock(): MockFriend {
    contract { returns() implies (this@mock is MockFriend) }
    return this as MockFriend
}

public fun Member.mock(): MockMember {
    contract { returns() implies (this@mock is MockMember) }
    return this as MockMember
}

public fun OtherClient.mock(): MockOtherClient {
    contract { returns() implies (this@mock is MockOtherClient) }
    return this as MockOtherClient
}

public fun Stranger.mock(): MockStranger {
    contract { returns() implies (this@mock is MockStranger) }
    return this as MockStranger
}

/**
 * @see MockBot.uploadOnlineAudio
 */
@MockBotDSL
public suspend fun ExternalResource.mockUploadAsOnlineAudio(bot: MockBot): OnlineAudio {
    return bot.uploadOnlineAudio(this)
}


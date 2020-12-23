/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PHONE

/**
 * 其他设备. 如当 [Bot] 以 [ANDROID_PHONE] 登录时, 还可以有其他设备以 [ANDROID_PAD], iOS, PC 或其他设备登录.
 */
public interface OtherClient : Contact {
    /**
     * 此设备属于的 [Bot]
     */
    public override val bot: Bot

    /**
     * 与 [Bot.id] 相同
     */
    public override val id: Long get() = bot.id
}

/*
public enum class ClientKind {
    ANDROID_PHONE,
    ANDROID_PAD,
    ANDROID_WATCH,
    IOS_PHONE,
    IOS_PAD,
    MAC_OS,
    WINDOWS_QQ,
    WINDOWS_TIM
}*/
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.utils;

import net.mamoe.mirai.Bot;

import java.util.List;

/**
 * 获取 Bot Manager
 * Java 友好 API
 */
public class BotManager {

    public static List<Long> getManagers(long botAccount) {
        Bot bot = Bot.getInstance(botAccount);
        return getManagers(bot);
    }

    public static List<Long> getManagers(Bot bot) {
        return BotHelperKt.getBotManagers(bot);
    }

    public static boolean isManager(Bot bot, long target) {
        return getManagers(bot).contains(target);
    }

    public static boolean isManager(long botAccount, long target) {
        return getManagers(botAccount).contains(target);
    }
}


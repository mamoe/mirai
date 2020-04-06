package net.mamoe.mirai.console.utils;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.MiraiConsole;

import java.util.List;

/**
 * 获取Bot Manager
 * Java友好API
 */
public class BotManager {

    static List<Long> getManagers(long botAccount) {
        Bot bot = MiraiConsole.INSTANCE.getBotOrThrow(botAccount);
        return getManagers(bot);
    }

    static List<Long> getManagers(Bot bot){
        return BotHelperKt.getBotManagers(bot);
    }

    static boolean isManager(Bot bot, long target){
        return getManagers(bot).contains(target);
    }

    static boolean isManager(long botAccount, long target){
        return getManagers(botAccount).contains(target);
    }
}


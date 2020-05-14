package net.mamoe.mirai.console.utils;

import net.mamoe.mirai.Bot;

import java.util.List;

/**
 * 获取Bot Manager
 * Java友好API
 */
public class BotManager {

    public static List<Long> getManagers(long botAccount) {
        Bot bot = Bot.getInstance(botAccount);
        return getManagers(bot);
    }

    public static List<Long> getManagers(Bot bot){
        return BotHelperKt.getBotManagers(bot);
    }

    public static boolean isManager(Bot bot, long target){
        return getManagers(bot).contains(target);
    }

    public static boolean isManager(long botAccount, long target){
        return getManagers(botAccount).contains(target);
    }
}


package net.mamoe.mirai.utils;

import net.mamoe.mirai.MiraiServer;

public enum LoggerTextFormat {
    RED("\033[31m"),
    GREEN("\033[32m"),
    YELLOW("\033[33m"),
    BLUE("\033[34m"),
    SKY_BLUE("\033[36m"),
    RESET("\33[0m");
    private String format;

    LoggerTextFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        if(MiraiServer.getInstance().isUnix()){
            return format;
        }
        return "";
    }
}

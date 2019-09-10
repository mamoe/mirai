package net.mamoe.mirai.utils;

/**
 * @author NaturalHG
 */
public enum LoggerTextFormat {
    RESET("\33[0m"),

    BLUE("\033[0;34m"),
    BLACK("\033[0;30m"),
    DARK_GREY("\033[1;30m"),
    LIGHT_BLUE("\033[1;34m"),
    GREEN("\033[0;32m"),
    LIGHT_GTEEN("\033[1;32m"),
    CYAN("\033[0;36m"),
    LIGHT_CYAN("\033[1;36m"),
    RED("\033[0;31m"),
    LIGHT_RED("\033[1;31m"),
    PURPLE("\033[0;35m"),
    LIGHT_PURPLE("\033[1;35m"),
    BROWN("\033[0;33m"),
    YELLOW("\033[1;33m"),
    LIGHT_GRAY("\033[0;37m"),
    WHITE("\033[1;37m");

    private String format;

    LoggerTextFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        //if(MiraiServer.getInstance().isUnix()){
        return format;
        // }
        // return "";
    }
}

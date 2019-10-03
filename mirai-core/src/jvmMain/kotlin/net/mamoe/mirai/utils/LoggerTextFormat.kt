package net.mamoe.mirai.utils

/**
 * @author NaturalHG
 */
enum class LoggerTextFormat(private val format: String) {
    RESET("\u001b[0m"),

    BLUE("\u001b[0;34m"),
    BLACK("\u001b[0;30m"),
    DARK_GREY("\u001b[1;30m"),
    LIGHT_BLUE("\u001b[1;34m"),
    GREEN("\u001b[0;32m"),
    LIGHT_GTEEN("\u001b[1;32m"),
    CYAN("\u001b[0;36m"),
    LIGHT_CYAN("\u001b[1;36m"),
    RED("\u001b[0;31m"),
    LIGHT_RED("\u001b[1;31m"),
    PURPLE("\u001b[0;35m"),
    LIGHT_PURPLE("\u001b[1;35m"),
    BROWN("\u001b[0;33m"),
    YELLOW("\u001b[1;33m"),
    LIGHT_GRAY("\u001b[0;37m"),
    WHITE("\u001b[1;37m");

    override fun toString(): String {
        //if(MiraiServer.getInstance().isUnix()){
        return format
        // }
        // return "";
    }
}

package net.mamoe.mirai.clikt.core

public sealed interface CommandResult {
    public object Success : CommandResult

    /**
     * An error occurred, with error message and nullable cause
     * @param message message to describe error for debug
     * @param userMessage message to describe error for user, default is the same as [message]
     */
    public class Error(message: String?, public val userMessage: String? = message, cause: Exception? = null) :
        CommandResult, IllegalStateException(message, cause)
}
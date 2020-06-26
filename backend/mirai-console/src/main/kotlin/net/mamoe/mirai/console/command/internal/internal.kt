/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.internal

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import java.util.concurrent.locks.ReentrantLock


internal infix fun Array<String>.matchesBeginning(list: List<Any>): Boolean {
    this.forEachIndexed { index, any ->
        if (list[index] != any) return false
    }
    return true
}

internal object InternalCommandManager {
    const val COMMAND_PREFIX = "/"

    @JvmField
    internal val registeredCommands: MutableList<Command> = mutableListOf()

    /**
     * 全部注册的指令
     * /mute -> MuteCommand
     * /jinyan -> MuteCommand
     */
    @JvmField
    internal val requiredPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    /**
     * Command name of commands that are prefix optional
     * mute -> MuteCommand
     */
    @JvmField
    internal val optionalPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val modifyLock = ReentrantLock()


    /**
     * 从原始的 command 中解析出 Command 对象
     */
    internal fun matchCommand(rawCommand: String): Command? {
        if (rawCommand.startsWith(COMMAND_PREFIX)) {
            return requiredPrefixCommandMap[rawCommand.substringAfter(
                COMMAND_PREFIX
            )]
        }
        return optionalPrefixCommandMap[rawCommand]
    }
}

internal infix fun <T> Array<out T>.intersects(other: Array<out T>): Boolean {
    val max = this.size.coerceAtMost(other.size)
    for (i in 0 until max) {
        if (this[i] == other[i]) return true
    }
    return false
}



internal fun String.fuzzyCompare(target: String): Double {
    var step = 0
    if (this == target) {
        return 1.0
    }
    if (target.length > this.length) {
        return 0.0
    }
    for (i in this.indices) {
        if (target.length == i) {
            step--
        } else {
            if (this[i] != target[i]) {
                break
            }
            step++
        }
    }

    if (step == this.length - 1) {
        return 1.0
    }
    return step.toDouble() / this.length
}

/**
 * 模糊搜索一个List中index最接近target的东西
 */
internal inline fun <T : Any> Collection<T>.fuzzySearch(
    target: String,
    index: (T) -> String
): T? {
    if (this.isEmpty()) {
        return null
    }
    var potential: T? = null
    var rate = 0.0
    this.forEach {
        val thisIndex = index(it)
        if (thisIndex == target) {
            return it
        }
        with(thisIndex.fuzzyCompare(target)) {
            if (this > rate) {
                rate = this
                potential = it
            }
        }
    }
    return potential
}

/**
 * 模糊搜索一个List中index最接近target的东西
 * 并且确保target是唯一的
 * 如搜索index为XXXXYY list中同时存在XXXXYYY XXXXYYYY 将返回null
 */
internal inline fun <T : Any> Collection<T>.fuzzySearchOnly(
    target: String,
    index: (T) -> String
): T? {
    if (this.isEmpty()) {
        return null
    }
    var potential: T? = null
    var rate = 0.0
    var collide = 0
    this.forEach {
        with(index(it).fuzzyCompare(target)) {
            if (this > rate) {
                rate = this
                potential = it
            }
            if (this == 1.0) {
                collide++
            }
            if (collide > 1) {
                return null//collide
            }
        }
    }
    return potential
}


internal fun Group.fuzzySearchMember(nameCardTarget: String): Member? {
    return this.members.fuzzySearchOnly(nameCardTarget) {
        it.nameCard
    }
}


//// internal

@JvmSynthetic
internal inline fun <reified T> List<T>.dropToTypedArray(n: Int): Array<T> = Array(size - n) { this[n + it] }

@JvmSynthetic
@Throws(CommandExecutionException::class)
internal suspend inline fun CommandSender.executeCommandInternal(
    messages: Any,
    commandName: String
): Command? {
    val command = InternalCommandManager.matchCommand(
        commandName
    ) ?: return null

    if (!command.testPermission(this)) {
        throw CommandExecutionException(
            command,
            commandName,
            CommandPermissionDeniedException(command)
        )
    }

    kotlin.runCatching {
        command.onCommand(this, messages.flattenCommandComponents().dropToTypedArray(1))
    }.fold(
        onSuccess = {
            return command
        },
        onFailure = {
            throw CommandExecutionException(command, commandName, it)
        }
    )
}

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.utils.isManager
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner

/**
 * 指令权限
 *
 * @see AnonymousCommandPermission
 */
abstract class CommandPermission {
    /**
     * 判断 [this] 是否拥有这个指令的权限
     */
    abstract fun CommandSender.hasPermission(): Boolean


    /**
     * 满足两个权限其中一个即可使用指令
     */ // no extension for Java
    infix fun or(another: CommandPermission): CommandPermission = OrCommandPermission(this, another)

    /**
     * 同时拥有两个权限才能使用指令
     */ // no extension for Java
    infix fun and(another: CommandPermission): CommandPermission = AndCommandPermission(this, another)


    /**
     * 任何人都可以使用这个指令
     */
    object Any : CommandPermission() {
        override fun CommandSender.hasPermission(): Boolean = true
    }

    /**
     * 任何人都不能使用这个指令. 指令只能通过代码在 [CommandManager] 使用
     */
    object None : CommandPermission() {
        override fun CommandSender.hasPermission(): Boolean = false
    }

    /**
     * 管理员或群主可以使用这个指令
     */
    class Operator(
        /**
         * 指定只有来自某个 [Bot] 的管理员或群主才可以使用这个指令
         */
        vararg val fromBot: Long
    ) : CommandPermission() {
        constructor(vararg fromBot: Bot) : this(*fromBot.map { it.id }.toLongArray())

        override fun CommandSender.hasPermission(): Boolean {
            return this is GroupContactCommandSender && this.bot.id in fromBot && this.realSender.isOperator()
        }

        /**
         * 来自任何 [Bot] 的任何一个管理员或群主都可以使用这个指令
         */
        companion object Any : CommandPermission() {
            override fun CommandSender.hasPermission(): Boolean {
                return this is GroupContactCommandSender && this.realSender.isOperator()
            }
        }
    }

    /**
     * 群主可以使用这个指令
     */
    class GroupOwner(
        /**
         * 指定只有来自某个 [Bot] 的群主才可以使用这个指令
         */
        vararg val fromBot: Long
    ) : CommandPermission() {
        constructor(vararg fromBot: Bot) : this(*fromBot.map { it.id }.toLongArray())

        override fun CommandSender.hasPermission(): Boolean {
            return this is GroupContactCommandSender && this.bot.id in fromBot && this.realSender.isOwner()
        }

        /**
         * 来自任何 [Bot] 的任何一个群主都可以使用这个指令
         */
        companion object Any : CommandPermission() {
            override fun CommandSender.hasPermission(): Boolean {
                return this is GroupContactCommandSender && this.realSender.isOwner()
            }
        }
    }

    /**
     * 管理员 (不包含群主) 可以使用这个指令
     */
    class Administrator(
        /**
         * 指定只有来自某个 [Bot] 的管理员 (不包含群主) 才可以使用这个指令
         */
        vararg val fromBot: Long
    ) : CommandPermission() {
        constructor(vararg fromBot: Bot) : this(*fromBot.map { it.id }.toLongArray())

        override fun CommandSender.hasPermission(): Boolean {
            return this is GroupContactCommandSender && this.bot.id in fromBot && this.realSender.isAdministrator()
        }

        /**
         * 来自任何 [Bot] 的任何一个管理员 (不包含群主) 都可以使用这个指令
         */
        companion object Any : CommandPermission() {
            override fun CommandSender.hasPermission(): Boolean {
                return this is GroupContactCommandSender && this.realSender.isAdministrator()
            }
        }
    }

    /**
     * console 管理员可以使用这个指令
     */
    class Manager(
        /**
         * 指定只有来自某个 [Bot] 的管理员或群主才可以使用这个指令
         */
        vararg val fromBot: Long
    ) : CommandPermission() {
        constructor(vararg fromBot: Bot) : this(*fromBot.map { it.id }.toLongArray())

        override fun CommandSender.hasPermission(): Boolean {
            return this is GroupContactCommandSender && this.bot.id in fromBot && this.realSender.isManager
        }

        /**
         * 任何 [Bot] 的 manager 都可以使用这个指令
         */
        companion object Any : CommandPermission() {
            override fun CommandSender.hasPermission(): Boolean {
                return this is GroupContactCommandSender && this.realSender.isManager
            }
        }
    }

    /**
     * 仅控制台能使用和这个指令
     */
    object Console : CommandPermission() {
        override fun CommandSender.hasPermission(): Boolean = false
    }

    companion object {
        @JvmStatic
        val Default: CommandPermission = Manager or Console
    }
}

/**
 * 使用 [lambda][block] 快速构造 [CommandPermission]
 */
@JvmSynthetic
@Suppress("FunctionName")
inline fun AnonymousCommandPermission(crossinline block: CommandSender.() -> Boolean): CommandPermission {
    return object : CommandPermission() {
        override fun CommandSender.hasPermission(): Boolean = block()
    }
}

inline fun CommandSender.hasPermission(permission: CommandPermission): Boolean =
    permission.run { this@hasPermission.hasPermission() }


inline fun CommandPermission.hasPermission(sender: CommandSender): Boolean = this.run { sender.hasPermission() }


internal class OrCommandPermission(
    private val first: CommandPermission,
    private val second: CommandPermission
) : CommandPermission() {
    override fun CommandSender.hasPermission(): Boolean {
        return this.hasPermission(first) || this.hasPermission(second)
    }
}

internal class AndCommandPermission(
    private val first: CommandPermission,
    private val second: CommandPermission
) : CommandPermission() {
    override fun CommandSender.hasPermission(): Boolean {
        return this.hasPermission(first) || this.hasPermission(second)
    }
}
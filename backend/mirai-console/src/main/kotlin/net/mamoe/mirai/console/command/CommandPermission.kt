/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.internal.command.AndCommandPermissionImpl
import net.mamoe.mirai.console.internal.command.OrCommandPermissionImpl
import net.mamoe.mirai.console.util.BotManager.INSTANCE.isManager
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner

/**
 * 指令权限.
 *
 * 在 [CommandManager.executeCommand] 时将会检查权限.
 *
 * @see Command.permission 从指令获取权限
 */
public fun interface CommandPermission {
    /**
     * 判断 [this] 是否拥有这个指令的权限
     *
     * @see CommandSender.hasPermission
     * @see CommandPermission.testPermission
     */
    public fun CommandSender.hasPermission(): Boolean


    /**
     * 满足两个权限其中一个即可使用指令
     */ // no extension for Java
    public infix fun or(another: CommandPermission): CommandPermission = OrCommandPermissionImpl(this, another)

    /**
     * 同时拥有两个权限才能使用指令
     */ // no extension for Java
    public infix fun and(another: CommandPermission): CommandPermission = AndCommandPermissionImpl(this, another)


    /**
     * 任何人都可以使用这个指令
     */
    public object Any : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean = true
    }

    /**
     * 任何人都不能使用这个指令. 指令只能通过调用 [Command.onCommand] 执行.
     */
    public object None : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean = false
    }

    /**
     * 来自任何 [Bot] 的任何一个管理员或群主都可以使用这个指令
     */
    public object Operator : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean {
            return this is MemberCommandSender && this.user.isOperator()
        }
    }

    /**
     * 来自任何 [Bot] 的任何一个群主都可以使用这个指令
     */
    public object GroupOwner : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean {
            return this is MemberCommandSender && this.user.isOwner()
        }
    }

    /**
     * 管理员 (不包含群主) 可以使用这个指令
     */
    public object Administrator : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean {
            return this is MemberCommandSender && this.user.isAdministrator()
        }
    }

    /**
     * 任何 [Bot] 的 manager 都可以使用这个指令
     */
    public object Manager : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean {
            return this is MemberCommandSender && this.user.isManager
        }
    }

    /**
     * 仅控制台能使用和这个指令
     */
    public object Console : CommandPermission {
        public override fun CommandSender.hasPermission(): Boolean = this is ConsoleCommandSender
    }

    /**
     * 默认权限.
     *
     * @return [Manager] or [Console]
     */
    public object Default : CommandPermission by (Manager or Console)
}

/**
 * 判断 [this] 是否拥有权限 [permission]
 *
 * @see CommandSender.hasPermission
 * @see CommandPermission.testPermission
 * @see CommandPermission.hasPermission
 */
public inline fun CommandSender.hasPermission(permission: CommandPermission): Boolean =
    permission.run { this@hasPermission.hasPermission() }


/**
 * 判断 [sender] 是否拥有权限 [this]
 *
 * @see CommandSender.hasPermission
 * @see CommandPermission.testPermission
 * @see CommandPermission.hasPermission
 */
public inline fun CommandPermission.testPermission(sender: CommandSender): Boolean = this.run { sender.hasPermission() }

/**
 * 判断 [sender] 是否拥有权限 [Command.permission]
 *
 * @see CommandSender.hasPermission
 * @see CommandPermission.testPermission
 * @see CommandPermission.hasPermission
 */
public inline fun Command.testPermission(sender: CommandSender): Boolean = sender.hasPermission(this.permission)
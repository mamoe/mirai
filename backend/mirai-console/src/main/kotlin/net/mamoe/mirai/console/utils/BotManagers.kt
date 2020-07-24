/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:JvmName("BotManagers")

package net.mamoe.mirai.console.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.setting.*
import net.mamoe.mirai.console.setting.SettingStorage.Companion.load
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.utils.minutesToMillis
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 判断此用户是否为 console 管理员
 */
public val User.isManager: Boolean get() = this.id in this.bot.managers

public fun Bot.removeManager(id: Long): Boolean {
    return ManagersConfig[this].remove(id)
}

public val Bot.managers: List<Long>
    get() = ManagersConfig[this].toList()

internal fun Bot.addManager(id: Long): Boolean {
    return ManagersConfig[this].add(id)
}


internal object ManagersConfig : Setting by (ConsoleBuiltInSettingStorage.load(ConsoleBuiltInSettingHolder)) {
    private val managers: MutableMap<Long, MutableSet<Long>> by value()

    internal operator fun get(bot: Bot): MutableSet<Long> = managers.getOrPut(bot.id, ::mutableSetOf)
}


internal fun CoroutineContext.overrideWithSupervisorJob(): CoroutineContext = this + SupervisorJob(this[Job])
internal fun CoroutineScope.childScope(context: CoroutineContext = EmptyCoroutineContext): CoroutineScope =
    CoroutineScope(this.coroutineContext.overrideWithSupervisorJob() + context)

internal object ConsoleBuiltInSettingHolder : AutoSaveSettingHolder,
    CoroutineScope by MiraiConsole.childScope() {
    override val autoSaveIntervalMillis: LongRange
        get() = 30.minutesToMillis..60.minutesToMillis
    override val name: String get() = "ConsoleBuiltIns"
}

internal object ConsoleBuiltInSettingStorage :
    SettingStorage by MiraiConsoleImplementationBridge.settingStorageForJarPluginLoader
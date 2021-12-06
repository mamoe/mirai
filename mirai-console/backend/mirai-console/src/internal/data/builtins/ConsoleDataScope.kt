/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.data.builtins

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.utils.minutesToMillis


internal object ConsoleDataScope : CoroutineScope by MiraiConsole.childScope("ConsoleDataScope") {
    private val data: List<PluginData> = mutableListOf()
    private val configs: MutableList<PluginConfig> = mutableListOf(AutoLoginConfig)

    fun addAndReloadConfig(config: PluginConfig) {
        configs.add(config)
        ConsoleBuiltInPluginConfigStorage.load(ConsoleBuiltInPluginConfigHolder, config)
    }

    fun reloadAll() {
        data.forEach { dt ->
            ConsoleBuiltInPluginDataStorage.load(ConsoleBuiltInPluginDataHolder, dt)
        }
        configs.forEach { config ->
            ConsoleBuiltInPluginConfigStorage.load(ConsoleBuiltInPluginConfigHolder, config)
        }
    }
}

internal object ConsoleBuiltInPluginDataHolder : AutoSavePluginDataHolder,
    CoroutineScope by ConsoleDataScope.childScope("ConsoleBuiltInPluginDataHolder") {
    override val autoSaveIntervalMillis: LongRange = 1.minutesToMillis..10.minutesToMillis
    override val dataHolderName: String get() = "Console"
}

internal object ConsoleBuiltInPluginConfigHolder : AutoSavePluginDataHolder,
    CoroutineScope by ConsoleDataScope.childScope("ConsoleBuiltInPluginConfigHolder") {
    override val autoSaveIntervalMillis: LongRange = 1.minutesToMillis..10.minutesToMillis
    override val dataHolderName: String get() = "Console"
}

internal object ConsoleBuiltInPluginDataStorage :
    PluginDataStorage by MiraiConsoleImplementationBridge.dataStorageForBuiltIns

internal object ConsoleBuiltInPluginConfigStorage :
    PluginDataStorage by MiraiConsoleImplementationBridge.configStorageForBuiltIns
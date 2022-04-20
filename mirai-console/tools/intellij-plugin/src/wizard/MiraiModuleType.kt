/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.intellij.wizard

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import net.mamoe.mirai.console.intellij.assets.Icons

class MiraiModuleType : ModuleType<MiraiModuleBuilder>("MIRAI_CONSOLE_PLUGIN_MODULE") {
    override fun createModuleBuilder() = MiraiModuleBuilder()
    override fun getIcon() = Icons.MainIcon
    override fun getNodeIcon(isOpened: Boolean) = Icons.MainIcon
    override fun getName() = NAME
    override fun getDescription() =
        "Modules used for developing plugins for <b>Mirai Console</b>"

    companion object {
        private const val ID = "MIRAI_CONSOLE_PLUGIN_MODULE"
        const val NAME = "Mirai"

        val instance: MiraiModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as MiraiModuleType
    }
}
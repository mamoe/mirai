/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import net.mamoe.mirai.console.data.java.JAutoSavePluginConfig

/**
 * 一个插件的配置数据, 用于和用户交互.
 *
 * 用户可通过 mirai-console 前端 (如在 Android 中可视化实现) 修改这些配置, 修改会自动写入这个对象中.
 *
 * **提示**:
 * 插件内部的数据应用 [PluginData] 存储, 而不能使用 [PluginConfig].
 *
 * ## 实现
 * 对使用者来说, [PluginConfig] 与 [PluginData] 实现几乎相同. 目前仅需在 [PluginData] 使用的基础上添加接口实现即可.
 *
 * ### Kotlin
 * 在 [PluginData] 的示例基础上, 修改对象定义
 * ```
 * // 原
 * object MyPluginData : AutoSavePluginData()
 * // 修改为
 * object MyPluginConfig : AutoSavePluginConfig()
 * ```
 * 即可将一个 [PluginData] 变更为 [PluginConfig].
 *
 * ### Java
 * 见 [JAutoSavePluginConfig]
 *
 * @see PluginData
 */
public interface PluginConfig : PluginData {
    /**
     * 警告: [PluginConfig] 的实现处于实验性阶段.
     *
     * 自主实现 [PluginConfig] 将得不到兼容性保障. 请仅考虑使用 [AutoSavePluginConfig]
     */
}
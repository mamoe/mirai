/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.data

/**
 * 一个插件的配置数据, 用于和用户交互.
 *
 * 用户可通过 mirai-console 前端修改这些配置, 修改会自动写入这个对象中.
 */
@ExperimentalPluginConfig
public interface PluginConfig : PluginData
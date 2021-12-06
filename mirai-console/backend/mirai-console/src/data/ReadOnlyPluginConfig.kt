/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.data

/**
 * 只读的 [PluginConfig]. 插件只能读取其值, 但值可能在后台被前端 (用户) 修改.
 *
 * @see PluginConfig
 * @see AutoSavePluginData
 * @since 1.1
 */
public open class ReadOnlyPluginConfig public constructor(saveName: String) : ReadOnlyPluginData(saveName), PluginConfig
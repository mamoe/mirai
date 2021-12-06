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

import kotlinx.coroutines.Job

/**
 * 链接自动保存的 [PluginConfig].
 *
 * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
 *
 * 若 [AutoSavePluginDataHolder.coroutineContext] 含有 [Job], 则 [AutoSavePluginData] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
 *
 * @see PluginConfig
 * @see AutoSavePluginData
 */
public open class AutoSavePluginConfig public constructor(saveName: String) : AutoSavePluginData(saveName), PluginConfig
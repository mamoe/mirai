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

import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.error

/**
 * 只读的 [PluginData]. 插件只能读取其值, 但值可能在后台被前端 (用户) 修改.
 *
 * @see PluginData
 * @see AutoSavePluginData
 * @since 1.1
 */
public open class ReadOnlyPluginData private constructor(
    // KEEP THIS PRIMARY CONSTRUCTOR FOR FUTURE USE: WE'LL SUPPORT SERIALIZERS_MODULE FOR POLYMORPHISM
    @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?,
) : AbstractPluginData() {
    public final override val saveName: String
        get() = _saveName

    private lateinit var _saveName: String

    public constructor(saveName: String) : this(null) {
        _saveName = saveName
    }

    @ConsoleExperimentalApi
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
    }

    @ConsoleExperimentalApi
    public final override fun onValueChanged(value: Value<*>) {
        debuggingLogger1.error { "onValueChanged: $value" }
    }
}
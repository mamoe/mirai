/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting.internal

import net.mamoe.mirai.console.setting.IntValue
import net.mamoe.mirai.console.setting.Setting


//// region Setting.value primitives impl CODEGEN ////

// TODO: 2020/6/21 CODEGEN

internal fun Setting.valueImpl(default: Int): IntValue = object : IntValueImpl(default) {
    override fun onChanged() = this@valueImpl.onValueChanged(this)
}

//// endregion Setting.value primitives impl CODEGEN ////

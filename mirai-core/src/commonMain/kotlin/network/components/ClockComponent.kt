/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.utils.Clock
import net.mamoe.mirai.utils.lateinitMutableProperty

internal open class ClockHolder {
    open val local: Clock get() = Clock.SystemDefault
    open var server: Clock by lateinitMutableProperty { local }

    companion object : ComponentKey<ClockHolder> {
        val QQAndroidBot.clock get() = components[ClockHolder]
    }
}
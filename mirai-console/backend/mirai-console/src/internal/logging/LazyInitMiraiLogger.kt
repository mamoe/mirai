/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.logging

import me.him188.kotlin.dynamic.delegation.dynamicDelegation
import net.mamoe.mirai.utils.MiraiLogger


internal fun lazyInitMiraiLogger(block: () -> MiraiLogger): MiraiLogger {
    return LazyInitMiraiLogger(lazy(block))
}

internal fun lazyInitMiraiLogger(block: Lazy<MiraiLogger>): MiraiLogger {
    return LazyInitMiraiLogger(block)
}

internal class LazyInitMiraiLogger(
    private val theLazy: Lazy<MiraiLogger>,
) : MiraiLogger by (dynamicDelegation(LazyInitMiraiLogger::logger)) {
    private inline val logger: MiraiLogger get() = theLazy.value
}

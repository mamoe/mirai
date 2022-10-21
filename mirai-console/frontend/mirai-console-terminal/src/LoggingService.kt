/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.terminal

import net.mamoe.mirai.utils.TestOnly

// Used by https://github.com/iTXTech/soyuz, change with care.
internal sealed class LoggingService {
    @TestOnly
    internal lateinit var switchLogFileNow: () -> Unit

    internal abstract fun pushLine(line: String)
}

internal class LoggingServiceNoop : LoggingService() {
    override fun pushLine(line: String) {
    }

    init {
        @OptIn(TestOnly::class)
        switchLogFileNow = {}
    }
}


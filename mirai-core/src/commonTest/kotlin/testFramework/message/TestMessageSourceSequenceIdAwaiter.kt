/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.message

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.internal.message.source.MessageSourceSequenceIdAwaiter
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToGroupImpl

internal class TestMessageSourceSequenceIdAwaiter : MessageSourceSequenceIdAwaiter() {
    override fun getSequenceIdAsync(
        sourceToGroupImpl: OnlineMessageSourceToGroupImpl,
        coroutineScope: CoroutineScope
    ): Deferred<IntArray?> {
        return CompletableDeferred(value = null) // assuming server didn't provide response, just to be simpler
    }
}
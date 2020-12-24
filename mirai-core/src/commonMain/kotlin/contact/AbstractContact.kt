/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.coroutines.CoroutineContext

internal abstract class AbstractContact(
    bot: Bot,
    coroutineContext: CoroutineContext,
) : Contact {
    final override val coroutineContext: CoroutineContext = coroutineContext + SupervisorJob(coroutineContext[Job])
    final override val bot: QQAndroidBot by bot.cast<QQAndroidBot>().unsafeWeakRef()
}
/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import kotlin.jvm.JvmOverloads

/**
 * 当 [Bot] 被迫下线时抛出, 作为 [Job.cancel] 的 `cause`
 */
public class ForceOfflineException
@JvmOverloads constructor(
    public override val message: String? = null,
    public override val cause: Throwable? = null
) : CancellationException(message)
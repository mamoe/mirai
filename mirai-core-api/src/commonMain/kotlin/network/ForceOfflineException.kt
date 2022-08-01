/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import kotlin.jvm.JvmOverloads

/**
 * 当 [Bot] 被迫下线时抛出, 作为 [Job.cancel] 的 `cause`
 */
@Deprecated("Not used anymore since 2.7", level = DeprecationLevel.HIDDEN) // deprecated since 2.7, error since 2.8
@DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.8", hiddenSince = "2.10")
public class ForceOfflineException
@JvmOverloads constructor(
    public override val message: String? = null,
    public override val cause: Throwable? = null
) : CancellationException(message)
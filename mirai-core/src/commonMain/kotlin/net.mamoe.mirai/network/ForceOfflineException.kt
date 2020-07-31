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
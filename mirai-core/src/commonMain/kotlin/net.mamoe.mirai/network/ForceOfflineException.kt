package net.mamoe.mirai.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot

/**
 * 当 [Bot] 被迫下线时抛出, 作为 [Job.cancel] 的 `cause`
 */
class ForceOfflineException(override val message: String?) : CancellationException(message)
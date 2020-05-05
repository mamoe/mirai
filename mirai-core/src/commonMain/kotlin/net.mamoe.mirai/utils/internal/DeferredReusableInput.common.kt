package net.mamoe.mirai.utils.internal

import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.MiraiExperimentalAPI

internal expect class DeferredReusableInput(input: Any, extraArg: Any?) : ReusableInput {
    val initialized: Boolean

    @OptIn(MiraiExperimentalAPI::class)
    suspend fun init(strategy: FileCacheStrategy)
}
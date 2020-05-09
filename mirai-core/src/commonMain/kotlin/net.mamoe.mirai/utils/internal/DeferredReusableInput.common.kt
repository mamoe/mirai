package net.mamoe.mirai.utils.internal

import net.mamoe.mirai.utils.FileCacheStrategy

internal expect class DeferredReusableInput(input: Any, extraArg: Any?) : ReusableInput {
    val initialized: Boolean


    suspend fun init(strategy: FileCacheStrategy)
}
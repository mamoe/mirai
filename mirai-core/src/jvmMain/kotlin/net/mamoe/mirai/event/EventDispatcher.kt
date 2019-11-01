package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val EventDispatcher: CoroutineDispatcher get() = Dispatchers.Default
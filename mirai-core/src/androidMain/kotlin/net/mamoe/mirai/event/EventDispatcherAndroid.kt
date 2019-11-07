package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal actual val EventDispatcher: CoroutineDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
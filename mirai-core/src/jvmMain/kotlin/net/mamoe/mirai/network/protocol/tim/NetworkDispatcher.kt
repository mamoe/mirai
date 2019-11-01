package net.mamoe.mirai.network.protocol.tim

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * 独立的 4 thread 调度器
 */
actual val NetworkDispatcher: CoroutineDispatcher
    get() = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
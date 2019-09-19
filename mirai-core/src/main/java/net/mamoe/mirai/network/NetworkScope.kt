package net.mamoe.mirai.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

object NetworkScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = lazy {
            ThreadPoolExecutor(
                    1,
                    4,
                    8000, TimeUnit.MILLISECONDS,
                    SynchronousQueue()
            ).asCoroutineDispatcher()
        }.value//todo improve

}
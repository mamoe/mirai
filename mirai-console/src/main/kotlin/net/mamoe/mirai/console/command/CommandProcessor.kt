package net.mamoe.mirai.console.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object CommandProcessor {
    val commandDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    suspend fun runCommand(){
        Dispatchers.IO
    }
}


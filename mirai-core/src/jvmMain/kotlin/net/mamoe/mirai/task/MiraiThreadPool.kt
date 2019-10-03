package net.mamoe.mirai.task

import net.mamoe.mirai.Mirai

import java.io.Closeable
import java.util.concurrent.ScheduledThreadPoolExecutor

/**
 * @author NaturalHG
 */
class MiraiThreadPool internal constructor()/*super(0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );*/ : ScheduledThreadPoolExecutor(10), Closeable {


    override fun close() {
        this.shutdown()
        if (!this.isShutdown) {
            this.shutdownNow()
        }
    }

    companion object {
        val instance = MiraiThreadPool()

        @JvmStatic
        fun main(args: Array<String>) {
            println(Mirai.VERSION)
        }
    }

}

package net.mamoe.mirai.task;

import java.io.Closeable;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author NaturalHG
 */
public final class MiraiThreadPool extends ScheduledThreadPoolExecutor implements Closeable {
    private static MiraiThreadPool instance = new MiraiThreadPool();

    public static MiraiThreadPool getInstance() {
        return instance;
    }

    MiraiThreadPool() {
        super(10);

        /*super(0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );*/
    }


    @Override
    public void close(){
        this.shutdown();
        if(!this.isShutdown()){
            this.shutdownNow();
        }
    }

}

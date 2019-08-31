package net.mamoe.mirai.task;

import java.io.Closeable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MiraiThreadPool extends ThreadPoolExecutor implements Closeable {

    protected MiraiThreadPool(){
        super(0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );
    }


    @Override
    public void close(){
        this.shutdown();
        if(!this.isShutdown()){
            this.shutdownNow();
        }
    }

}

package net.mamoe.mirai.task;

import java.util.concurrent.*;

public class MiraiTaskPool {

    ExecutorService service;

    protected MiraiTaskPool(){
        this.service = Executors.newCachedThreadPool();
    }

    public <D> Future<D> submit(Callable<D> callable, MiralTaskExceptionHandler handler) {
        return this.service.submit(() -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                handler.onHandle(e);
                return null;
            }
        });
    }

    public <D> Future<D> submit(Callable<D> callable) {
        return this.submit(callable, Throwable::printStackTrace);
    }

}

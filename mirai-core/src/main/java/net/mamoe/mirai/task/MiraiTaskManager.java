package net.mamoe.mirai.task;


import net.mamoe.mirai.event.MiraiEventHook;
import net.mamoe.mirai.event.events.server.ServerDisabledEvent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author NaturalHG
 */
public final class MiraiTaskManager {

    private static MiraiTaskManager instance = new MiraiTaskManager();

    public static MiraiTaskManager getInstance() {
        return MiraiTaskManager.instance;
    }

    private MiraiThreadPool pool;

    private MiraiTaskManager() {
        this.pool = new MiraiThreadPool();

        MiraiEventHook
                .onEvent(ServerDisabledEvent.class)
                .handler(a -> this.pool.close())
                .mount();

    }

    /**
     * 基础Future处理
     */

    public void execute(Runnable runnable) {
        this.execute(runnable, MiraiTaskExceptionHandler.printing());
    }

    public void execute(Runnable runnable, MiraiTaskExceptionHandler handler) {
        this.pool.execute(() ->
        {
            try {
                runnable.run();
            } catch (Exception e) {
                handler.onHandle(e);
            }
        });
    }


    public <D> Future<D> submit(Callable<D> callable) {
        return this.submit(callable, MiraiTaskExceptionHandler.printing());
    }

    public <D> Future<D> submit(Callable<D> callable, MiraiTaskExceptionHandler handler) {
        return this.pool.submit(() -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                handler.onHandle(e);
                return null;
            }
        });
    }

    /**
     * 异步任务
     */
    public <D> void ansycTask(Callable<D> callable, Consumer<D> callback) {
        this.ansycTask(callable, callback, MiraiTaskExceptionHandler.printing());
    }

    public <D> void ansycTask(Callable<D> callable, Consumer<D> callback, MiraiTaskExceptionHandler handler) {
        this.pool.execute(() -> {
            try {
                callback.accept(callable.call());
            } catch (Throwable e) {
                handler.onHandle(e);
            }
        });
    }

    /**
     * 定时任务
     */

    public void repeatingTask(Runnable runnable, long intervalMillis) {
        this.repeatingTask(runnable, intervalMillis, MiraiTaskExceptionHandler.printing());
    }

    public void repeatingTask(Runnable runnable, long intervalMillis, MiraiTaskExceptionHandler handler) {
        this.repeatingTask(runnable, intervalMillis, a -> true, handler);
    }

    public void repeatingTask(Runnable runnable, long intervalMillis, int times) {
        this.repeatingTask(runnable, intervalMillis, times, MiraiTaskExceptionHandler.printing());
    }

    public void repeatingTask(Runnable runnable, long intervalMillis, int times, MiraiTaskExceptionHandler handler) {
        AtomicInteger integer = new AtomicInteger(times - 1);
        this.repeatingTask(
                runnable, intervalMillis, a -> integer.getAndDecrement() > 0, handler
        );
    }


    public <D extends Runnable> void repeatingTask(D runnable, long intervalMillis, Predicate<D> shouldContinue, MiraiTaskExceptionHandler handler) {
        new Thread(() -> {
            do {
                this.pool.execute(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        handler.onHandle(e);
                    }
                });
                try {
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (shouldContinue.test(runnable));
        }).start();
    }

    public void deletingTask(Runnable runnable, long intervalMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.pool.execute(runnable);
        }).start();
    }

}

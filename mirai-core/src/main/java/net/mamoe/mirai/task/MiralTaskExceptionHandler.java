package net.mamoe.mirai.task;

@FunctionalInterface
public interface MiralTaskExceptionHandler {
    void onHandle(Throwable e);
}

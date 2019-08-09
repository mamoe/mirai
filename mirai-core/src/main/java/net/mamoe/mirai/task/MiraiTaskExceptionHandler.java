package net.mamoe.mirai.task;

@FunctionalInterface
public interface MiraiTaskExceptionHandler {
    void onHandle(Throwable e);

    static MiraiTaskExceptionHandler byDefault(){
        return Throwable::printStackTrace;
    }
}

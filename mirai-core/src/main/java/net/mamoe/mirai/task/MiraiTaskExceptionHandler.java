package net.mamoe.mirai.task;

/**
 * @author NaturalHG
 */
@FunctionalInterface
public interface MiraiTaskExceptionHandler {
    void onHandle(Throwable e);

    static MiraiTaskExceptionHandler printing() {
        return Throwable::printStackTrace;
    }

    static MiraiTaskExceptionHandler ignoring() {
        return a -> {
        };
    }
}

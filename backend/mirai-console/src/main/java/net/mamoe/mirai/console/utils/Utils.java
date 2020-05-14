package net.mamoe.mirai.console.utils;

import org.jetbrains.annotations.Range;

import java.util.concurrent.Callable;

public final class Utils {

    /**
     * 执行N次 callable
     * 成功一次就会结束
     * 否则就会throw
     */
    public static <T> T tryNTimes(@Range(from = 1, to = Integer.MAX_VALUE) int n,
                                  Callable<T> callable) throws Exception {
        Exception last = null;

        while (n-- > 0) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (last == null) {
                    last = e;
                } else {
                    try {
                        last.addSuppressed(e);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        throw last;
    }
}

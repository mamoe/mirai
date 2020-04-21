package net.mamoe.mirai.console.utils;

import java.util.concurrent.Callable;

public class Utils {

    /**
     * 执行N次 callable
     * 成功一次就会结束
     * 否则就会throw
     */
    public static <T> T tryNTimes(
            /*@Range(from=1, to=Integer.MAX_VALUE)*/
            int n,
            Callable<T> callable
    ) throws Exception {
        if (n < 0) {
            throw new IllegalArgumentException("Must be executed at least once.");
        }
        Exception last = null;

        while (n-- > 0) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (last == null) {
                    last = e;
                } else {
                    last.addSuppressed(e);
                }
            }
        }

        if (last == null) {
            throw new Exception("unknown error");
        }

        throw last;
    }
}

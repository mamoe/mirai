package net.mamoe.mirai.console.utils;

import java.util.concurrent.Callable;

public class Utils {

    /**
     * 执行N次 callable
     * 成功一次就会结束
     * 否则就会throw
     */
    public static <T> T tryNTimes(
            int n,
            Callable<T> callable
    ) throws Exception {

        T result = null;
        Exception last = null;

        while(n-- > 0){
            try{
                result = callable.call();
                break;
            }catch(Exception e){last=e;}
        }

        if(result != null){
            return result;
        }

        if(last == null){
            last = new Exception("unknown error");
        }

        throw last;
    }
}

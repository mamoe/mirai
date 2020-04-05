package net.mamoe.mirai.console.utils

/**
 * 执行N次 builder
 * 成功一次就会结束
 * 否则就会throw
 */
inline fun <T> tryNTimes(n:Int = 2, builder: () -> T):T {
    var lastException: Exception? = null

    repeat(n){
        try {
            return builder.invoke()
        } catch (e: Exception) {
            lastException = e
        }
    }

    throw lastException!!
}




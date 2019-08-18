package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * used to replace old logger
 */
object MiraiLogger{
    fun info(o: Any) {
        this.print(o.toString())
    }

    fun catching(e:Throwable){
        e.printStackTrace()
        /*
        this.print(e.message)
        this.print(e.localizedMessage)
        this.print(e.cause.toString())*/
    }

    private fun print(value:String?){
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
        System.out.println(LoggerTextFormat.BLUE.toString() + "[Mirai] $s : $value")
    }
}
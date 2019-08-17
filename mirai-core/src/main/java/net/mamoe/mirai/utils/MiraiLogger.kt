package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

object MiraiLogger{
    fun info(o: Any) {
        this.print(o.toString())
    }

    fun catching(e:Throwable){
        this.print(e.message)
        this.print(e.localizedMessage)
        this.print(e.cause.toString())
    }

    private fun print(value:String?){
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
        System.out.println(LoggerTextFormat.BLUE.toString() + "[Mirai] $s : $value")
    }
}
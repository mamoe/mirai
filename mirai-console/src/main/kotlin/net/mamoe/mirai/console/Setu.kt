package net.mamoe.mirai.console

import net.mamoe.mirai.console.plugins.Config
import net.mamoe.mirai.console.plugins.loadAsConfig
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import java.io.File

object Data {
    val section = (File(System.getProperty("user.dir") + "/setu.yml")).loadAsConfig()
    val R18 = section.getList
}

fun main() {


}
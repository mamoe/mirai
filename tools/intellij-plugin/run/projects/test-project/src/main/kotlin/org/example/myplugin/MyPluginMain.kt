package org.example.myplugin

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

val T = "scas" + "pp" // 编译期常量

object MyPluginMain : KotlinPlugin(
    JvmPluginDescription(
        T,
        "0.1.0",
    ) {
        name(".")
        id("")
    }
) {
    fun test() {

    }
}

object MyPluginMain2 : KotlinPlugin(
    JvmPluginDescription(
        "",
        "0.1.0",
    ) {
        name(".")
        id("")
    }
) {
    fun test() {

    }
}
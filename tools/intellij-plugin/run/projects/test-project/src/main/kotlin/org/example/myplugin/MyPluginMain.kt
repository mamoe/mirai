package org.example.myplugin

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object MyPluginMain : KotlinPlugin(
    JvmPluginDescription(
        "net.mamoe.main",
        "0.1.0",
    ) {
        name(".")
        id("")
    }
) {
    fun test() {

    }
}
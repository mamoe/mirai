package org.example.myplugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
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

object DataTest : AutoSavePluginConfig() {
    val p by value<HasDefaultValue>()
    val pp by value<NoDefaultValue>()
}

data class HasDefaultValue(
    val x: Int = 0,
)

data class NoDefaultValue(
    val y: Int,
)
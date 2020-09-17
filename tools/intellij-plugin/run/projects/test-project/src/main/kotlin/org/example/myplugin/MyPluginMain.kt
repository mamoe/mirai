package org.example.myplugin

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object MyPluginMain : KotlinPlugin(
    JvmPluginDescription(
        "org.example.example-plugin",
        "0.1.0"
    )
) {
    fun test() {

    }
}

class PM : KotlinPlugin(

)
package org.example.myplugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

val T = "org.example" // 编译期常量

object MyPluginMain : KotlinPlugin(
    JvmPluginDescription(
        T,
        "0.1.0",
    ) {
        name(".")
    }
) {
    override fun onEnable() {
        super.onEnable()
        PermissionService.INSTANCE.register(permissionId("dvs"), "ok")
    }

    fun test() {

    }
}

object DataTest : AutoSavePluginConfig("data") {
    val p by value<HasDefaultValue>()
    val pp by value<NoDefaultValue>()
}

@Serializable
data class HasDefaultValue(
    val x: Int = 0,
)

data class NoDefaultValue(
    val y: Int,
)

val y = "傻逼 yellow"
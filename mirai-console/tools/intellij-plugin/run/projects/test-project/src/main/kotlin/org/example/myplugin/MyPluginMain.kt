package org.example.myplugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.SemVersion

const val T = "org.example" // 编译期常量

object MyPluginMain : KotlinPlugin(
    JvmPluginDescription(
        T,
        "1.0-M4",
    ) {
        name(".")
    }
) {
    override fun onEnable() {
        super.onEnable()
        PermissionService.INSTANCE.register(permissionId("dvs"), "ok")
        PermissionService.INSTANCE.register(permissionId("perm with space"), "error")
        PermissionId("Namespace with space", "Name with space")
        SemVersion.parseRangeRequirement("1.0")
        SemVersion.parseRangeRequirement("<br/>")
        SemVersion.parseRangeRequirement("SB YELLOW")
        SemVersion.parseRangeRequirement("1.0.0 || 2.0.0 || ")
        SemVersion.parseRangeRequirement("1.0.0 || 2.0.0")
        SemVersion.parseRangeRequirement("1.0.0 || 2.0.0 && 3.0.0")
        SemVersion.parseRangeRequirement("{}")
        SemVersion.parseRangeRequirement("||")
        SemVersion.parseRangeRequirement(">= 114.514 || = 1919.810 || (1.1, 1.2)")
        SemVersion.parseRangeRequirement("0.0.0 || {90.48}")
        SemVersion.parseRangeRequirement("{114514.1919810}")
        SemVersion.parseRangeRequirement("}")
    }

    fun test() {

    }
}


val x = "弱智黄色"


object MyData : AutoSavePluginData("") {
    val value by value("")
    val value2 by value<Map<String, String>>()
}
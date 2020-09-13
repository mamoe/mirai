@file:Suppress("unused")

package org.example.myplugin

import com.google.auto.service.AutoService
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.PluginDataExtensions.mapKeys
import net.mamoe.mirai.console.data.PluginDataExtensions.withEmptyDefault
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.scopeWith
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.info

@AutoService(JvmPlugin::class)
object MyPluginMain : KotlinPlugin(
    JvmPluginDescription(
        "org.example.example-plugin",
        "0.1.0"
    )
) {

    val PERMISSION_EXECUTE_1 = PermissionService.INSTANCE.register(
        permissionId("execute1"),
        "注册权限的示例"
    )


    override fun onEnable() {
        MySetting.reload() // 从数据库自动读取配置实例
        MyPluginData.reload()

        logger.info { "Hi: ${MySetting.name}" } // 输出一条日志.
        logger.info("Hi: ${MySetting.name}") // 输出一条日志. 与上面一条相同, 但更推荐上面一条.
        logger.verbose("Hi: ${MySetting.name}") // 多种日志级别可选

        // 请不要使用 println, System.out.println 等标准输出方式. 请总是使用 logger.


        MySetting.count++ // 对 Setting 的改动会自动在合适的时间保存

        MySimpleCommand.register() // 注册指令
    }

    override fun onDisable() {
        MySimpleCommand.unregister() // 取消注册指令
    }
}

// 定义插件数据
// 插件
object MyPluginData : AutoSavePluginData() {
    var list: MutableList<String> by value(mutableListOf("a", "b")) // mutableListOf("a", "b") 是初始值, 可以省略
    var long: Long by value(0L) // 允许 var
    var int by value(0) // 可以使用类型推断, 但更推荐使用 `var long: Long by value(0)` 这种定义方式.


    // 带默认值的非空 map.
    // notnullMap[1] 的返回值总是非 null 的 MutableMap<Int, String>
    var notnullMap
            by value<MutableMap<Int, MutableMap<Int, String>>>().withEmptyDefault()

    // 可将 MutableMap<Long, Long> 映射到 MutableMap<Bot, Long>.
    val botToLongMap: MutableMap<Bot, Long> by value<MutableMap<Long, Long>>().mapKeys(Bot::getInstance, Bot::id)
}

// 定义一个配置. 所有属性都会被追踪修改, 并自动保存.
// 配置是插件与用户交互的接口, 但不能用来保存插件的数据.
object MySetting : AutoSavePluginConfig() {
    val name by value("test")

    var count by value(0)

    val nested by value<MyNestedData>() // 嵌套类型是支持的
}

@Serializable
data class MyNestedData(
    val list: List<String> = listOf(),
)

// 简单指令
object MySimpleCommand : SimpleCommand(
    MyPluginMain, "foo",
    description = "示例指令"
) {
    // 会自动创建一个 ID 为 "org.example.example-plugin:command.foo" 的权限.


    // 通过 /foo 调用, 参数自动解析
    @Handler
    suspend fun CommandSender.handle(int: Int, str: String) { // 函数名随意, 但参数需要按顺序放置.

        if (this.hasPermission(MyPluginMain.PERMISSION_EXECUTE_1)) {
            sendMessage("你有 ${MyPluginMain.PERMISSION_EXECUTE_1.id} 权限.")
        } else {
            sendMessage(
                """
                你没有 ${MyPluginMain.PERMISSION_EXECUTE_1.id} 权限.
                可以在控制台使用 /permission 管理权限.
            """.trimIndent()
            )
        }

        sendMessage("/foo 的第一个参数是 $int, 第二个是 $str")
    }
}

// 复合指令
object MyCompositeCommand : CompositeCommand(
    MyPluginMain, "manage",
    description = "示例指令",
    // prefixOptional = true // 还有更多参数可填, 此处忽略
) {
    // 会自动创建一个 ID 为 "org.example.example-plugin:command.manage" 的权限.

    //
    // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
    // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
    // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
    // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
    @SubCommand
    suspend fun CommandSender.mute(target: Member, duration: Int) { // 通过 /manage mute <target> <duration> 调用
        sendMessage("/manage mute 被调用了, 参数为: $target, $duration")

        val result = kotlin.runCatching {
            target.mute(duration).toString()
        }.getOrElse {
            it.stackTraceToString()
        } // 失败时返回堆栈信息


        // 表示对 this 和 ConsoleCommandSender 一起操作
        this.scopeWith(ConsoleCommandSender) {
            sendMessage("结果: $result") // 同时发送给 this@CommandSender 和 ConsoleCommandSender
        }
    }

    @SubCommand
    suspend fun CommandSender.list() { // 执行 "/manage list" 时调用这个函数
        sendMessage("/manage list 被调用了")
    }

    // 支持 Image 类型, 需在聊天中执行此指令.
    @SubCommand
    suspend fun CommandSender.test(image: Image) { // 执行 "/manage test <一张图片>" 时调用这个函数
        sendMessage("/manage image 被调用了, 图片是 ${image.imageId}")
    }
}
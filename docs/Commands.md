# Mirai Console Backend - Commands

## [`Command`]

>「指令」：目前通常是 "/commandName arg1 arg2 arg3" 格式的消息。在将来可能会被扩展

```kotlin
interface Command {
    val names: Array<out String>
    val usage: String
    val description: String
    val permission: CommandPermission
    val prefixOptional: Boolean
    val owner: CommandOwner
    suspend fun CommandSender.onCommand(args: MessageChain)
}
```

每一条指令都被抽象成 [`Command`]。

### 执行指令

指令既可以在代码执行，也可以在消息环境中执行。

#### 在 [`CommandManager`] 执行指令

通过扩展：
- `suspend fun Command.execute(CommandSender, args: Message, checkPermission: Boolean=true)`
- `suspend fun Command.execute(CommandSender, args: String, checkPermission: Boolean=true)`
- `suspend fun CommandSender.executeCommand(message: Message, checkPermission: Boolean=true)`
- `suspend fun CommandSender.executeCommand(message: String, checkPermission: Boolean=true)`

### 指令参数

指令的参数允许 `SingleMessage`，即包含 `At`, `Image`, `PlainText` 等。


### [`RawCommand`]


## [`CommandManager`]
上面已经提到可以在 [`CommandManager`] 执行指令。[`CommandManager`] 持有已经注册的指令列表，源码内有详细注释，此处不过多赘述。

## [`CommandSender`]
指令发送者。

### 必要性

`Contact`

[`Plugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/Plugin.kt
[`PluginDescription`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/description/PluginDescription.kt
[`PluginLoader`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/PluginLoader.kt
[`PluginManager`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/PluginManager.kt
[`JarPluginLoader`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JarPluginLoader.kt
[`JvmPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPlugin.kt
[`JvmPluginDescription`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPluginDescription.kt
[`AbstractJvmPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/AbstractJvmPlugin.kt
[`KotlinPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/KotlinPlugin.kt
[`JavaPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPlugin.kt


[`Value`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/Value.kt
[`PluginData`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginData.kt
[`AbstractPluginData`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/AbstractPluginData.kt
[`AutoSavePluginData`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/AutoSavePluginData.kt
[`AutoSavePluginConfig`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/AutoSavePluginConfig.kt
[`PluginConfig`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginConfig.kt
[`PluginDataStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt
[`MultiFilePluginDataStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt#L116
[`MemoryPluginDataStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt#L100
[`AutoSavePluginDataHolder`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataHolder.kt#L45
[`PluginDataHolder`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataHolder.kt
[`PluginDataExtensions`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataExtensions.kt

[`MiraiConsole`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsole.kt
[`MiraiConsoleImplementation`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleImplementation.kt
<!--[MiraiConsoleFrontEnd]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleFrontEnd.kt-->

[`Command`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/Command.kt
[`AbstractCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/Command.kt#L90
[`CompositeCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/CompositeCommand.kt
[`SimpleCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/SimpleCommand.kt
[`RawCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/RawCommand.kt
[`CommandManager`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/CommandManager.kt
[`CommandSender`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/CommandSender.kt

[`BotManager`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/BotManager.kt
[`Annotations`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/Annotations.kt
[`ConsoleInput`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/ConsoleInput.kt
[`JavaPluginScheduler`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPluginScheduler.kt
[`ResourceContainer`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/ResourceContainer.kt
[`PluginFileExtensions`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/PluginFileExtensions.kt

[Kotlin]: https://www.kotlincn.net/
[Java]: https://www.java.com/zh_CN/
[JVM]: https://zh.wikipedia.org/zh-cn/Java%E8%99%9A%E6%8B%9F%E6%9C%BA
[JAR]: https://zh.wikipedia.org/zh-cn/JAR_(%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F)

[为什么不支持热加载和卸载插件？]: QA.md#为什么不支持热加载和卸载插件
[使用 AutoService]: QA.md#使用-autoservice



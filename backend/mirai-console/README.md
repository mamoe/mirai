# mirai-console backend

欢迎来到 mirai-console 后端开发文档。

## 包结构
- `net.mamoe.mirai.console.`
  - `command`：指令模块：[Command]
  - `data`：存储模块：[PluginData], [PluginConfig], [PluginDataStorage]
  - `event`：Console 实现的事件.
  - `plugin`：插件模块：[Plugin], [PluginLoader], [JvmPlugin]
  - `util`：工具类：[Annotations], [BotManager], [ConsoleInput], [JavaPluginScheduler]
  - `internal`：内部实现


## 基础

### `Plugin` 模块

Console 支持拥有强扩展性的插件加载器。内建 JVM 插件支持 ([JarPluginLoader])。

#### [插件加载器 `PluginLoader`][PluginLoader]
Console 本身是一套高扩展性的「框架」，必然拥有通用的 [插件加载器][PluginLoader]。

Console 内置 [JarPluginLoader]，支持加载使用 Kotlin、 Java，或其他 JVM 平台编程语言并打包为 ‘jar’ 的插件 (详见下文 `JvmPlugin`)。

扩展的 [插件加载器][PluginLoader] 可以由一个特别的 [JVM 插件][JvmPlugin] 提供。在启动时, Console 首先加载那些提供扩展 [插件加载器][PluginLoader] 的插件. 并允许它们 [注册扩展加载器]。

#### [`Plugin`][Plugin]
所有 Console 插件都必须实现 [`Plugin`][Plugin] 接口。  
虽然 Console 是 JVM 平台程序, 但也拥有支持其他平台的插件管理系统。

[`Plugin`][Plugin] 可在相应 [插件加载器 `PluginLoader`][PluginLoader] 的帮助下，成为任何语言实现的插件与 Console 建立联系的桥梁。



#### [JVM 插件][JvmPlugin]


#### 实现 Kotlin 插件
添加一个类

#### 实现 Java 插件



[Plugin]: src/main/kotlin/net/mamoe/mirai/console/plugin/Plugin.kt
[PluginDescription]: src/main/kotlin/net/mamoe/mirai/console/plugin/description.kt
[PluginLoader]: src/main/kotlin/net/mamoe/mirai/console/plugin/PluginLoader.kt
[JarPluginLoader]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JarPluginLoader.kt
[JvmPlugin]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPlugin.kt
[AbstractJvmPlugin]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/AbstractJvmPlugin.kt
[KotlinPlugin]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/KotlinPlugin.kt
[JavaPlugin]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPlugin.kt

[PluginData]: src/main/kotlin/net/mamoe/mirai/console/data/PluginData.kt
[PluginConfig]: src/main/kotlin/net/mamoe/mirai/console/data/PluginConfig.kt
[PluginDataStorage]: src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt

[MiraiConsole]: src/main/kotlin/net/mamoe/mirai/console/MiraiConsole.kt
[MiraiConsoleImplementation]: src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleImplementation.kt
<!--[MiraiConsoleFrontEnd]: src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleFrontEnd.kt-->

[Command]: src/main/kotlin/net/mamoe/mirai/console/command/Command.kt
[CompositeCommand]: src/main/kotlin/net/mamoe/mirai/console/command/CompositeCommand.kt
[SimpleCommand]: src/main/kotlin/net/mamoe/mirai/console/command/SimpleCommand.kt
[RawCommand]: src/main/kotlin/net/mamoe/mirai/console/command/RawCommand.kt
[CommandManager]: src/main/kotlin/net/mamoe/mirai/console/command/CommandManager.kt

[BotManager]: src/main/kotlin/net/mamoe/mirai/console/util/BotManager.kt
[Annotations]: src/main/kotlin/net/mamoe/mirai/console/util/Annotations.kt
[ConsoleInput]: src/main/kotlin/net/mamoe/mirai/console/util/ConsoleInput.kt
[JavaPluginScheduler]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPluginScheduler.kt

[注册扩展加载器]: src/main/kotlin/net/mamoe/mirai/console/plugin/PluginManager.kt#L49-L51
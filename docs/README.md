# MiraiConsole

欢迎来到 mirai-console 开发文档!

## 准备工作

### 开发 mirai-console 插件的准备工作
- 需使用 IDE: [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- IntelliJ 需装有 [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge) 插件 (启动 IntelliJ, 点击 [一键安装](https://plugins.jetbrains.com/embeddable/install/14816))
- 安装并配置 JDK 8

### 前置知识
要学习为 mirai-console 开发原生支持的插件, 需要:

- 掌握 Java 基础.
- 了解 Kotlin 基础语法:
  - [基本类型](https://www.kotlincn.net/docs/reference/basic-types.html)
  - [类与继承](https://www.kotlincn.net/docs/reference/classes.html)
  - [属性与字段](https://www.kotlincn.net/docs/reference/properties.html)
  - [接口](https://www.kotlincn.net/docs/reference/interfaces.html)
  - [扩展](https://www.kotlincn.net/docs/reference/extensions.html)
  - [数据类](https://www.kotlincn.net/docs/reference/data-classes.html)
  - [对象](https://www.kotlincn.net/docs/reference/object-declarations.html)
  - [密封类](https://www.kotlincn.net/docs/reference/sealed-classes.html)
  - **[Java 中调用 Kotlin](https://www.kotlincn.net/docs/reference/java-to-kotlin-interop.html)**
- 至少能使用 Java 或 Kotlin 一种一门语言解决问题
- 了解 JVM 和 Java 等同类编程语言的关系

## 目录

### 后端插件开发基础

- 包结构
  `net.mamoe.mirai.console.`
    - `command`：指令模块：[`Command`]
    - `data`：存储模块：[`PluginData`], [`PluginConfig`], [`PluginDataStorage`]
    - `event`：Console 实现的事件.
    - `plugin`：插件模块：[`Plugin`], [`PluginLoader`], [`JvmPlugin`]
    - `util`：工具类：[`Annotations`], [`BotManager`], [`ConsoleInput`], [`JavaPluginScheduler`]
    - `internal`：内部实现

- 插件 - [Plugin 模块](Plugins.md)
- 指令 - [Command 模块](Commands.md)
- 存储 - [PluginData 模块](PluginData.md)
- 权限 - [Permission 模块](Permissions.md)

### 后端插件开发进阶

- 扩展 - [Extension 模块和扩展点](Extensions.md)
- 扩展 - [实现 PluginLoader](PluginLoader.md)
- 扩展 - [实现 PermissionService](PermissionService.md)

### 实现前端
- [FrontEnd](FrontEnd.md)

[`Plugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/Plugin.kt
[`Annotations`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/Annotations.kt
[`PluginData`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginData.kt
[`JavaPluginScheduler`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPluginScheduler.kt
[`JvmPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPlugin.kt
[`PluginConfig`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginConfig.kt
[`PluginLoader`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/PluginLoader.kt
[`ConsoleInput`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/ConsoleInput.kt
[`PluginDataStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt
[`BotManager`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/BotManager.kt
[`Command`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/Command.kt

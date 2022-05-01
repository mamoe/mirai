# Mirai Console Backend - Plugins

[`Plugin`]: ../../backend/mirai-console/src/plugin/Plugin.kt

[`PluginDescription`]: ../../backend/mirai-console/src/plugin/description/PluginDescription.kt

[`PluginLoader`]: ../../backend/mirai-console/src/plugin/loader/PluginLoader.kt

[`PluginManager`]: ../../backend/mirai-console/src/plugin/PluginManager.kt

[`JvmPluginLoader`]: ../../backend/mirai-console/src/plugin/jvm/JvmPluginLoader.kt

[`JvmPlugin`]: ../../backend/mirai-console/src/plugin/jvm/JvmPlugin.kt

[`JvmPluginDescription`]: ../../backend/mirai-console/src/plugin/jvm/JvmPluginDescription.kt

[`AbstractJvmPlugin`]: ../../backend/mirai-console/src/plugin/jvm/AbstractJvmPlugin.kt

[`KotlinPlugin`]: ../../backend/mirai-console/src/plugin/jvm/KotlinPlugin.kt

[`JavaPlugin`]: ../../backend/mirai-console/src/plugin/jvm/JavaPlugin.kt


[`PluginData`]: ../../backend/mirai-console/src/data/PluginData.kt

[`PluginConfig`]: ../../backend/mirai-console/src/data/PluginConfig.kt

[`PluginDataStorage`]: ../../backend/mirai-console/src/data/PluginDataStorage.kt

[`ExportManager`]: ../../backend/mirai-console/src/plugin/jvm/ExportManager.kt

[`MiraiConsole`]: ../../backend/mirai-console/src/MiraiConsole.kt

[`MiraiConsoleImplementation`]: ../../backend/mirai-console/src/MiraiConsoleImplementation.kt

[`Command`]: ../../backend/mirai-console/src/command/Command.kt

[`CompositeCommand`]: ../../backend/mirai-console/src/command/CompositeCommand.kt

[`SimpleCommand`]: ../../backend/mirai-console/src/command/SimpleCommand.kt

[`RawCommand`]: ../../backend/mirai-console/src/command/RawCommand.kt

[`CommandManager`]: ../../backend/mirai-console/src/command/CommandManager.kt

[`Annotations`]: ../../backend/mirai-console/src/util/Annotations.kt

[`ConsoleInput`]: ../../backend/mirai-console/src/util/ConsoleInput.kt

[`JavaPluginScheduler`]: ../../backend/mirai-console/src/plugin/jvm/JavaPluginScheduler.kt

[`ResourceContainer`]: ../../backend/mirai-console/src/plugin/ResourceContainer.kt

[`PluginFileExtensions`]: ../../backend/mirai-console/src/plugin/PluginFileExtensions.kt

[`AutoSavePluginDataHolder`]: ../../backend/mirai-console/src/data/PluginDataHolder.kt#L45

[Kotlin]: https://www.kotlincn.net/

[Java]: https://www.java.com/zh_CN/

[JVM]: https://zh.wikipedia.org/zh-cn/Java%E8%99%9A%E6%8B%9F%E6%9C%BA

[JAR]: https://zh.wikipedia.org/zh-cn/JAR_(%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F)

[使用 AutoService]: ../QA.md#使用-autoservice

[JVMPlugin]: ./JVMPlugin.md

Mirai Console （简称 Console） 运行在 [JVM]，支持使用 [Kotlin] 或 [Java] 等 JVM
语言编写的插件。

本章节简要介绍 Console 插件架构（与平台无关的基础架构）。

## 通用的插件接口 - [`Plugin`]

所有 Console 插件都必须直接或间接地实现 [`Plugin`] 接口。

> **解释 *插件***：只要实现了 [`Plugin`] 接口的对象都可以叫做「Mirai Console 插件」，简称 「插件」。  
> 为了便捷，内含 [`Plugin`] 实现的一个 [JAR] 文件也可以被称为「插件」。

基础的 [`Plugin`] 很通用，它只拥有很少的成员：

```kotlin
interface Plugin : CommandOwner { // CommandOwner 表示该对象可以创建指令
    val isEnabled: Boolean // 当插件已开启时返回 true
    val loader: PluginLoader<*, *> // 能处理这个 Plugin 实例的 PluginLoader
}
```

[`Plugin`] 接口拥有强扩展性，以支持 Mirai Console 统一管理使用其他编程语言编写的插件
（详见进阶章节 [扩展 - PluginLoader](../Extensions.md)）。

> 除非你是在实现新种类插件，否则不要直接实现 `Plugin` 接口。

## 插件加载器 - [`PluginLoader`]

Mirai Console 支持使用多个插件加载器来加载多种类型插件。每个插件加载器都支持一种类型的插件。

Mirai Console 内置 [`JvmPluginLoader`] 以加载 JVM
平台插件（见下文），并允许这些插件注册扩展的插件加载器（见章节 [扩展](../Extensions.md))
，以支持读取其他语言编写的插件并接入
Console 插件管理系统。

## 总结

Mirai Console 提供抽象的插件及其加载器接口，支持扩展。各类插件行为由其加载器确定。插件作者需要基于特定的插件平台实现，如
Console 内置的 [JVM 平台][JVMPlugin]。

## 继续阅读

- [JVM 平台插件详情][JVMPlugin]
- [编写插件加载器](../Extensions.md)
# mirai-console backend

欢迎来到 mirai-console 后端开发文档。

## 准备工作

### 开发 mirai-console 插件的准备工作
- 需使用 IDE: [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- IntelliJ 需装有 [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge) 插件 (启动 IntelliJ, 点击 [一键安装](https://plugins.jetbrains.com/embeddable/install/14816))
- 安装并配置 JDK 8

### 前置知识
- 你需要掌握 Java 基础.
- 你需要了解 Kotlin 基础语法:
  - [基本类型](https://www.kotlincn.net/docs/reference/basic-types.html)
  - [类与继承](https://www.kotlincn.net/docs/reference/classes.html)
  - [属性与字段](https://www.kotlincn.net/docs/reference/properties.html)
  - [接口](https://www.kotlincn.net/docs/reference/interfaces.html)
  - [扩展](https://www.kotlincn.net/docs/reference/extensions.html)
  - [数据类](https://www.kotlincn.net/docs/reference/data-classes.html)
  - [对象](https://www.kotlincn.net/docs/reference/object-declarations.html)
  - [密封类](https://www.kotlincn.net/docs/reference/sealed-classes.html)
  - **[Java 中调用 Kotlin](https://www.kotlincn.net/docs/reference/java-to-kotlin-interop.html)**

## 包结构
- `net.mamoe.mirai.console.`
  - `command`：指令模块：[`Command`]
  - `data`：存储模块：[`PluginData`], [`PluginConfig`], [`PluginDataStorage`]
  - `event`：Console 实现的事件.
  - `plugin`：插件模块：[`Plugin`], [`PluginLoader`], [`JvmPlugin`]
  - `util`：工具类：[`Annotations`], [`BotManager`], [`ConsoleInput`], [`JavaPluginScheduler`]
  - `internal`：内部实现


## 基础

### `Plugin` 模块

Console 支持拥有强扩展性的插件加载器。内建 JVM 插件支持 ([`JarPluginLoader`])。

#### [插件加载器 `PluginLoader`][`PluginLoader`] 和 [插件管理器][`PluginManager`]
Console 本身是一套高扩展性的「框架」，拥有通用的 [插件加载器][`PluginLoader`]。

Console 内置 [`JarPluginLoader`]，支持加载使用 Kotlin、 Java，或其他 JVM 平台编程语言并打包为 ‘jar’ 的插件 (详见下文 `JvmPlugin`)。

扩展的 [插件加载器][`PluginLoader`] 可以由一个特别的 [JVM 插件][`JvmPlugin`] 提供。


##### 服务器启动过程中的插件加载流程

在服务器启动过程中, Console 首先加载那些提供扩展 [插件加载器][`PluginLoader`] 的插件. 并允许它们 [注册扩展加载器]。  
随后对插件按依赖顺序调用 `onLoad()`, 告知插件主类加载完毕, 相关依赖解决完毕.  
当所有插件的 `onLoad()` 都被调用后, [`PluginManager`] 按依赖顺序依次调用 `onEnable()`

如果 A 依赖 B, B 依赖 C. 那么启动时的调用顺序为:  
`C.onLoad()` -> `B.onLoad()` -> `A.onLoad()` -> `C.onEnable` -> `B.onEnable()` -> `A.onEnable()`

#### [`Plugin`]
所有 Console 插件都必须实现 [`Plugin`] 接口。

`Plugin` 很通用，它只拥有很少的成员：
```kotlin
interface Plugin : CommandOwner {
    val isEnabled: Boolean
    val loader: PluginLoader<*, *> // 能处理这个 Plugin 的 PluginLoader
}
```

[`Plugin`] 可在相应 [插件加载器 `PluginLoader`][`PluginLoader`] 的帮助下，成为任何语言实现的插件与 Console 建立联系的桥梁。


#### [JVM 插件][`JvmPlugin`]

##### [`JvmPlugin`]

```kotlin
interface JvmPlugin : Plugin, CoroutineScope, PluginFileExtensions, ResourceContainer, AutoSavePluginDataHolder {
    val logger: MiraiLogger
    val description: JvmPluginDescription
    val loader: JarPluginLoader
    fun <T : PluginData> loadPluginData(clazz: Class<T>): T
    fun <T : PluginConfig> loadPluginConfig(clazz: Class<T>): T
    fun onLoad()
    fun onEnable()
    fun onDisable()
}
```

##### `plugin.yml`

JVM 插件, 通常需要打包为 `jar` 后才能被加载.

**注意**:
- 插件自身的版本要求遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 合格的版本例如: `1.0.0`, `1.0`, `1.0-M1`, `1.0-pre-1`
- 插件依赖的版本遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 同时支持 [Apache Ivy 风格表示方法](http://ant.apache.org/ivy/history/latest-milestone/settings/version-matchers.html).


#### 实现 Kotlin 插件
在任意包内创建一个 `object` 单例, 继承 [`KotlinPlugin`].

#### 实现 Java 插件



[`Plugin`]: src/main/kotlin/net/mamoe/mirai/console/plugin/Plugin.kt
[`PluginDescription`]: src/main/kotlin/net/mamoe/mirai/console/plugin/description/PluginDescription.kt
[`PluginLoader`]: src/main/kotlin/net/mamoe/mirai/console/plugin/PluginLoader.kt
[`PluginManager`]: src/main/kotlin/net/mamoe/mirai/console/plugin/PluginManager.kt
[`JarPluginLoader`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JarPluginLoader.kt
[`JvmPlugin`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPlugin.kt
[`JvmPluginDescription`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPluginDescription.kt
[`AbstractJvmPlugin`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/AbstractJvmPlugin.kt
[`KotlinPlugin`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/KotlinPlugin.kt
[`JavaPlugin`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPlugin.kt


[`PluginData`]: src/main/kotlin/net/mamoe/mirai/console/data/PluginData.kt
[`PluginConfig`]: src/main/kotlin/net/mamoe/mirai/console/data/PluginConfig.kt
[`PluginDataStorage`]: src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt

[`MiraiConsole`]: src/main/kotlin/net/mamoe/mirai/console/MiraiConsole.kt
[`MiraiConsoleImplementation`]: src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleImplementation.kt
<!--[MiraiConsoleFrontEnd]: src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleFrontEnd.kt-->

[`Command`]: src/main/kotlin/net/mamoe/mirai/console/command/Command.kt
[`CompositeCommand`]: src/main/kotlin/net/mamoe/mirai/console/command/CompositeCommand.kt
[`SimpleCommand`]: src/main/kotlin/net/mamoe/mirai/console/command/SimpleCommand.kt
[`RawCommand`]: src/main/kotlin/net/mamoe/mirai/console/command/RawCommand.kt
[`CommandManager`]: src/main/kotlin/net/mamoe/mirai/console/command/CommandManager.kt

[`BotManager`]: src/main/kotlin/net/mamoe/mirai/console/util/BotManager.kt
[`Annotations`]: src/main/kotlin/net/mamoe/mirai/console/util/Annotations.kt
[`ConsoleInput`]: src/main/kotlin/net/mamoe/mirai/console/util/ConsoleInput.kt
[`JavaPluginScheduler`]: src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPluginScheduler.kt

[注册扩展加载器]: src/main/kotlin/net/mamoe/mirai/console/plugin/PluginManager.kt#L49-L51
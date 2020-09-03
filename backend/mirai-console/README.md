# mirai-console backend

欢迎来到 mirai-console 后端开发文档。

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
[`ResourceContainer`]: src/main/kotlin/net/mamoe/mirai/console/plugin/ResourceContainer.kt


## 基础

### `Plugin` 模块

Console 支持拥有强扩展性的插件加载器。内建 JVM 插件支持 ([`JarPluginLoader`])。

#### [插件加载器 `PluginLoader`][`PluginLoader`] 和 [插件管理器][`PluginManager`]
Console 本身是一套高扩展性的「框架」，拥有通用的 [插件加载器][`PluginLoader`]。

Console 内置 [`JarPluginLoader`]，支持加载使用 Kotlin、 Java，或其他 JVM 平台编程语言并打包为 ‘jar’ 的插件 (详见下文 `JvmPlugin`)。

扩展的 [插件加载器][`PluginLoader`] 可以由一个特别的 [JVM 插件][`JvmPlugin`] 提供。


##### 服务器启动过程中的插件加载流程

在服务器启动过程中, Console 首先加载那些提供扩展 [插件加载器][`PluginLoader`] 的插件。  
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
    fun onLoad() {}
    fun onEnable() {}
    fun onDisable() {}
}
```

##### 提供插件信息

JVM 插件, 通常需要打包为 `jar` 后才能被加载. Console 使用类似 Java ServiceLoader 的方式加载插件.

- 方法 A. (推荐) 自动创建 service 文件 (使用 Google auto-service)  
  在 `build.gradle.kts` 添加:
  ```kotlin
  plugins {
    kotlin("kapt")
  }
  dependencies {
    val autoService = "1.0-rc7"
    kapt("com.google.auto.service", "auto-service", autoService)
    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)
  }
  ```
  *对于 `build.gradle` 用户, 请自行按照 Groovy DSL 语法翻译*

- 方法 B. 手动创建 service 文件  
  在 `jar` 内 `META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin` 文件内存放插件主类全名.


**注意**:
- 插件自身的版本要求遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 合格的版本例如: `1.0.0`, `1.0`, `1.0-M1`, `1.0-pre-1`
- 插件依赖的版本遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 同时支持 [Apache Ivy 风格表示方法](http://ant.apache.org/ivy/history/latest-milestone/settings/version-matchers.html).


#### 实现 Kotlin 插件

一个 Kotlin 插件的主类通常需:
- 继承 [`KotlinPlugin`]
- 访问权限为 `public` 或默认 (不指定)

```kotlin
@AutoService(JvmPlugin::class) // 让 Console 知道这个 object 是一个插件主类.
object SchedulePlugin : KotlinPlugin(
    SimpleJvmPluginDescription( // 插件的描述, name 和 version 是必须的
        name = "Schedule",
        version = "1.0.0",
        // author, description, ...
    )
) {
    // ...
}
```

#### 实现 Java 插件

一个 Java 插件的主类通常需:
- 继承 [`KotlinPlugin`]
- 访问权限为 `public` 或默认 (不指定)

(推荐) 静态初始化:
```java
@AutoService(JvmPlugin.class)
public final class JExample extends JavaPlugin {
    public static final JExample INSTANCE = new JExample(); // 可以像 Kotlin 一样静态初始化单例
    private JExample() {
        super(new SimpleJvmPluginDescription(
            "JExample", // name
            "1.0.0" // version
        ));
    }
}
```

由 Console 初始化:
```java
@AutoService(JvmPlugin.class)
public final class JExample extends JavaPlugin {
    private static final JExample instance;
    public static JExample getInstance() {
        return instance;
    }
    public JExample() { // 此时必须 public
        super(new SimpleJvmPluginDescription(
            "JExample", // name
            "1.0.0" // version
        ));
        instance = this;
    }
}
```

#### 获取资源文件 [`ResourceContainer`]

[`JvmPlugin`] 实现接口 [`ResourceContainer`], 可在 `jar` 包内搜索资源文件.

提供三个获取方法:
```kotlin
interface ResourceContainer {
    fun getResourceAsStream(path: String): InputStream?
    fun getResource(path: String): String?
    fun getResource(path: String, charset: Charset): String?
}
```

### [`PluginData`] 模块

[`PluginData`]

... 待续

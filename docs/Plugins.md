# Mirai Console Backend - Plugins

[`Plugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/Plugin.kt
[`PluginDescription`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/description/PluginDescription.kt
[`PluginLoader`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/loader/PluginLoader.kt
[`PluginManager`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/PluginManager.kt
[`JvmPluginLoader`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPluginLoader.kt
[`JvmPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPlugin.kt
[`JvmPluginDescription`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPluginDescription.kt
[`AbstractJvmPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/AbstractJvmPlugin.kt
[`KotlinPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/KotlinPlugin.kt
[`JavaPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPlugin.kt


[`PluginData`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginData.kt
[`PluginConfig`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginConfig.kt
[`PluginDataStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt

[`MiraiConsole`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsole.kt
[`MiraiConsoleImplementation`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleImplementation.kt
<!--[MiraiConsoleFrontEnd]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleFrontEnd.kt-->

[`Command`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/Command.kt
[`CompositeCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/CompositeCommand.kt
[`SimpleCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/SimpleCommand.kt
[`RawCommand`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/RawCommand.kt
[`CommandManager`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/CommandManager.kt

[`Annotations`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/Annotations.kt
[`ConsoleInput`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/ConsoleInput.kt
[`JavaPluginScheduler`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPluginScheduler.kt
[`ResourceContainer`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/ResourceContainer.kt
[`PluginFileExtensions`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/PluginFileExtensions.kt
[`AutoSavePluginDataHolder`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataHolder.kt#L45

[Kotlin]: https://www.kotlincn.net/
[Java]: https://www.java.com/zh_CN/
[JVM]: https://zh.wikipedia.org/zh-cn/Java%E8%99%9A%E6%8B%9F%E6%9C%BA
[JAR]: https://zh.wikipedia.org/zh-cn/JAR_(%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F)

[为什么不支持热加载和卸载插件？]: QA.md#为什么不支持热加载和卸载插件
[使用 AutoService]: QA.md#使用-autoservice

Mirai Console 运行在 [JVM]，支持使用 [Kotlin] 或 [Java] 语言编写的插件。

## 通用的插件接口 - [`Plugin`]

所有 Console 插件都必须实现 [`Plugin`] 接口。

> **解释 *插件***：只要实现了 [`Plugin`] 接口的类和对象都可以叫做「Mirai Console 插件」，简称 「插件」。  
> 为了便捷，内含 [`Plugin`] 实现的一个 [JAR] 文件也可以被称为「插件」。

基础的 [`Plugin`] 很通用，它只拥有很少的成员：

```kotlin
interface Plugin : CommandOwner { // CommandOwner 是空的 interface
    val isEnabled: Boolean
    val loader: PluginLoader<*, *> // 能处理这个 Plugin 的 PluginLoader
}
```

[`Plugin`] 接口拥有强扩展性，以支持 Mirai Console 统一管理使用其他编程语言编写的插件 （详见进阶章节 [扩展 - PluginLoader](Extensions.md)）。

## JVM 平台插件接口 - [`JvmPlugin`]

所有的 JVM 插件都必须实现 [`JvmPlugin`]（否则不会被 [`JvmPluginLoader`] 加载）。  
Mirai Console 提供一些基础的实现，即 [`AbstractJvmPlugin`]，并将 [`JvmPlugin`] 分为 [`KotlinPlugin`] 和 [`JavaPlugin`]。

### 主类和描述

**Kotlin 使用者的插件主类应继承 [`KotlinPlugin`]。**  
**其他 JVM 语言（如 Java）使用者的插件主类应继承 [`JavaPlugin`]。**

#### 描述
插件描述需要在主类构造器传递给 `super`。因此插件不需要 `plugin.yml`, `plugin.xml` 等配置文件来指示信息。

Mirai Console 使用类似 `ServiceLoader` 的机制加载插件。  
在 Kotlin，可（[使用 AutoService]）自动配置 service 信息。  
在 Kotlin 或其他语言，可手动创建 service 文件： 在 `jar` 内 `META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin` 文件内存放插件主类全名（以纯文本 UTF-8 存储，文件内容只包含一行插件主类全名）.


有关插件版本号的限制：
- 插件自身的版本要求遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 合格的版本例如: `1.0.0`, `1.0`, `1.0-M1`, `1.0-pre-1`
- 插件依赖的版本遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 同时支持 [Apache Ivy 风格表示方法](http://ant.apache.org/ivy/history/latest-milestone/settings/version-matchers.html).

有关描述的详细信息可在开发时查看源码内文档。

#### 实现 Kotlin 插件主类

一个 Kotlin 插件的主类通常需:
- 继承 [`KotlinPlugin`]
- 访问权限为 `public` 或默认 (不指定)

```kotlin
object SchedulePlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.my-schedule-plugin",
        version = "1.0.0",
    ) {
        name("Schedule")
        
        // author("...")
        // dependsOn("...")
    }
) {
    // ...
}
```

#### 实现 Java 插件

一个 Java 插件的主类通常需:
- 继承 [`JavaPlugin`]
- 访问权限为 `public`

(推荐) 静态初始化:
```java
public final class JExample extends JavaPlugin {
    public static final JExample INSTANCE = new JExample(); // 可以像 Kotlin 一样静态初始化单例
    private JExample() {
        super(new JvmPluginDescriptionBuilder(
            "org.example.test-plugin", // name
            "1.0.0" // version
        )
        // .author("...")
        // .info("...")
        .build()
        );
    }
}
```

由 Console 初始化（仅在某些静态初始化不可用的情况下使用）:
```java
public final class JExample extends JavaPlugin {
    private static final JExample instance;
    public static JExample getInstance() {
        return instance;
    }
    public JExample() { // 此时必须 public
        super(new JvmPluginDescriptionBuilder(
            "org.example.test-plugin", // id
            "1.0.0" // version
        )
        // .author("...")
        // .info("...")
        .build()
        );
        instance = this;
    }
}
```

### 依赖管理

一个插件被允许依赖于另一个插件。可在 `SimpleJvmPluginDescription` 构造时提供信息。

若插件拥有依赖，则会首先加载其依赖。但任何一个插件的 `onEnable()` 都会在所有插件的 `onLoad()` 都调用成功后再调用。

多个插件的加载是*顺序的*，意味着若一个插件的 `onLoad()` 等回调处理缓慢，后续插件的加载也会被延后，即使它们可能没有依赖关系。  
因此请尽量让 `onLoad()`，`onEnable()`，`onDisable()`快速返回。

### 插件生命周期

Mirai Console 不提供热加载和热卸载功能，所有插件只能在服务器启动前加载，在服务器结束时卸载。（[为什么不支持热加载和卸载插件？]）

插件仅可以通过如下三个回调知晓自身的加载情况。

较小概率情况：
- 如果 `onLoad()` 被调用，`onEnable()` 不一定会调用。因为可能在调用后续插件的 `onLoad()` 或 `onEnable()` 时可能会出错而导致服务器被关闭。
- 如果 `onLoad()` 或 `onEnable()` 调用时抛出异常，`onDisable()` 不会被调用。（注意：这是仍处于争议状态的行为，后续可能有修改）
- 如果 `onEnable()` 被成功调用，`onDisable()` 一定会调用，无论其他插件是否发生错误。

#### 加载

[`JvmPluginLoader`] 调用插件的 `onLoad()`，在 `onLoad()` 正常返回后插件被认为成功加载。

由于 `onLoad()` 只会被初始化一次，插件可以在该方法内进行一些*一次性*的*初始化*任务，如 [注册扩展](Extensions.md#注册扩展)。

**在 `onLoad()` 时插件并未处于启用状态，此时插件不能进行监听事件，加载配置等操作。**

若在 Kotlin 使用 `object`，或在 Java 使用静态初始化方式定义插件主类, `onLoad()` 与 `init` 代码块作用几乎相同。

#### 启用

[`JvmPluginLoader`] 调用插件的 `onEnable()`，意为启用一个插件。

此时插件可以启动所有协程，事件监听，和其他任务。**但这些任务都应该拥有生命周期管理，详见 [任务生命周期管理](#任务生命周期管理)。**

#### 禁用

[`JvmPluginLoader`] 调用插件的 `onDisable()`，意为禁用一个插件。

插件的任何类和对象都不会被卸载。「禁用」仅表示停止关闭所有正在进行的任务，保存所有数据，停止处理将来的数据。

插件应正确实现「禁用」，以为用户提供完全的控制可能。

### 任务生命周期管理

#### 协程管理

[`JvmPlugin`] 实现 `CoroutineScope`，并由 Console 内部实现提供其 `coroutineContext`。

`JvmPlugin.coroutineContext` 包含元素 `CoroutineName`, `SupervisorJob`, `CoroutineExceptionHandler`。  
**所有插件启动的协程都应该受 `JvmPlugin` 作用域的管理**

如要启动一个协程，正确的做法是：
```kotlin
// object MyPluginMain : KotlinPlugin()

MyPluginMain.launch {
    // job
} 
```

#### Java 线程管理

*TODO*：Mirai Console 暂未支持自动的线程管理。请手动在 `onDisable()` 时关闭启动的线程。

### 访问数据目录和配置目录

[`JvmPlugin`] 实现接口 [`PluginFileExtensions`]。插件可通过 `resolveDataFile`，`resolveConfigFile` 等方法取得数据目录或配置目录下的文件。

可以在任何时刻使用这些方法。

详见 [`PluginFileExtensions`]。

#### 物理目录路径
用 `$root` 表示 Mirai Console 运行路径，`$name` 表示插件名，
插件数据目录一般在 `$root/data/$name`，插件配置目录一般在 `$root/config/$name`。

有关数据和配置的区别可以在 [PluginData](PluginData.md) 章节查看。

### 访问 [JAR] 包内资源文件

[`JvmPlugin`] 实现接口 [`ResourceContainer`]。插件可通过 `getResource`，`getResourceAsStream` 等取得 [JAR] 包内资源文件。

可以在任何时刻使用这些方法。

详见 [`ResourceContainer`]。

### 读取 [`PluginData`] 或 [`PluginConfig`]

> 本节基于章节 [PluginData](PluginData.md) 的内容。
> 在阅读本节前建议先阅读上述基础章节。

[`JvmPlugin`] 实现接口 [`AutoSavePluginDataHolder`]，提供:

Kotlin：
- `public fun <T : PluginData> T.reload()`
- `public fun <T : PluginConfig> T.reload()`

Java：
- `public fun reloadPluginData(PluginData)`
- `public fun reloadPluginData(PluginConfig)`

**仅可在插件 onEnable() 时及其之后才能使用这些方法。**  
**在插件 onDisable() 之后不能使用这些方法。**

#### 使用示例

```kotlin
object SchedulePlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.my-schedule-plugin",
        version = "1.0.0",
    ) {
        name("Schedule")
        
        // author("...")
        // dependsOn("...")
    }
) {
    // ...
    
    override fun onEnable() {
        MyData.reload() // 仅需此行，保证启动时更新数据，在之后自动存储数据。
    }
}

object MyData : AutoSavePluginData() {
    val value: Map<String, String> by value()
}
```

### 附录：Java 插件的多线程调度器 - [`JavaPluginScheduler`]
拥有生命周期管理的简单 Java 线程池。其中所有的任务都会在插件被关闭时自动停止。
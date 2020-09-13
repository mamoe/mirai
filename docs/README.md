# Mirai Console

欢迎来到 mirai-console 开发文档!

## 目录

- **[准备工作](#准备工作)**
- **[启动 Console](Run.md)**

### 后端插件开发基础

- 插件 - [Plugin 模块](Plugins.md)
- 指令 - [Command 模块](Commands.md)
- 存储 - [PluginData 模块](PluginData.md)
- 权限 - [Permission 模块](Permissions.md)

### 后端插件开发进阶

- 扩展 - [Extension 模块和扩展点](Extensions.md)

### 实现前端
- [FrontEnd](FrontEnd.md)

[`Plugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/Plugin.kt
[`Annotations`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/Annotations.kt
[`PluginData`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginData.kt
[`JavaPluginScheduler`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JavaPluginScheduler.kt
[`JvmPlugin`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/jvm/JvmPlugin.kt
[`PluginConfig`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginConfig.kt
[`PluginLoader`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/plugin/loader/PluginLoader.kt
[`ConsoleInput`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/util/ConsoleInput.kt
[`PluginDataStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/data/PluginDataStorage.kt
[`Command`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/Command.kt

## 准备工作
***如果跳过本节内容，你很可能遇到无法解决的问题。***

### 环境要求

*不接受降低最低版本要求的建议*

- JDK 11
- Android：Android SDK 26+ （Android 8.0)
- Kotlin: 1.4

*Mirai Console 需要的 Kotlin 版本会与 Kotlin 最新稳定版本同步。*

### 开发插件的准备工作

- 安装并配置 JDK 11

若使用 Java，或要修改 Mirai Console：

- 使用 [IntelliJ IDEA](https://www.jetbrains.com/idea/) （或 `Android Studio`）。
- IDE 需装有 [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge) 插件 (先启动你的 IDE，再点击 [一键安装](https://plugins.jetbrains.com/embeddable/install/14816))

若使用 Kotlin，无特别要求。

### 前置知识
要学习为 mirai-console 开发原生支持的插件, 需要:

- 掌握 Java 基础
- 至少粗略了解 Kotlin 基础语法（30 分钟）:
  - [基本类型](https://www.kotlincn.net/docs/reference/basic-types.html)
  - [类与继承](https://www.kotlincn.net/docs/reference/classes.html)
  - [属性与字段](https://www.kotlincn.net/docs/reference/properties.html)
  - [接口](https://www.kotlincn.net/docs/reference/interfaces.html)
  - [扩展](https://www.kotlincn.net/docs/reference/extensions.html)
  - [数据类](https://www.kotlincn.net/docs/reference/data-classes.html)
  - [对象](https://www.kotlincn.net/docs/reference/object-declarations.html)
  - [密封类](https://www.kotlincn.net/docs/reference/sealed-classes.html)
  - **[Java 中调用 Kotlin](https://www.kotlincn.net/docs/reference/java-to-kotlin-interop.html)**
- 对于 Java 使用者，请阅读 [Java 用户的使用指南](#java-用户的使用指南)，[在 Java 使用 Mirai Console 中的 Kotlin `suspend` 函数](#在-java-使用-mirai-console-中的-kotlin-suspend-函数)
- 对于 Kotlin 使用者，请熟知 [Kotlin `1.4` 版本带来的新特性](#mirai-console-使用的-kotlin-14-版本的新特性)


## 附录

### Java 用户的使用指南

- Java 中的「方法」在 Kotlin 中均被称为「函数」。
- Kotlin 默认的访问权限是 `public`。如 Kotlin `class Test` 相当于 Java 的 `public class Test {}`
- Kotlin 的函数定义 `fun test(int: Int): String` 相当于 Java 的方法定义 `public String test(int int)`

### 在 Java 使用 Mirai Console 中的 Kotlin `suspend` 函数

#### 什么是 `suspend` 函数

`suspend` 函数中文是「挂起函数」，是 Kotlin 「[协程](https://www.kotlincn.net/docs/reference/coroutines/coroutines-guide.html)」的一部分。

Kotlin 协程是语言级特性，函数的修饰符 `suspend` 会在编译阶段被处理。

对于一个挂起函数:
```kotlin
suspend fun test(): String
```

它会被编译为 `public Object test(Continuation<String> $completion)`。

这是因为 Kotlin 对所有挂起函数都有这样的内部变化，并在编译时实现了协程的一些特性。

Java 用户无法调用这样的方法，因为 `Continuation` 的实现很复杂。

Mamoe 为此开发了 Kotlin 编译器插件 [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge)，通过 `@JvmBlockingBridge` 注解，在编译期额外生成一个供 Java 使用的方法，让 Java 用户可以使用拥有源码内相同的函数签名的方法。

要获取详细信息，参考 [Kotlin Jvm Blocking Bridge 编译器插件](https://github.com/mamoe/kotlin-jvm-blocking-bridge/blob/master/README-chs.md#%E7%BC%96%E8%AF%91%E5%99%A8%E6%8F%92%E4%BB%B6)

### Mirai Console 使用的 Kotlin `1.4` 版本的新特性

在官方文档的 [语言特性与改进](https://www.kotlincn.net/docs/reference/whatsnew14.html#%E8%AF%AD%E8%A8%80%E7%89%B9%E6%80%A7%E4%B8%8E%E6%94%B9%E8%BF%9B) 基础上，Mirai Console 的一些设计基于 Kotlin 1.4 的更多新特性。

#### `object` 内的扩展函数的自动引用
对于如下定义：
```kotlin
package org.example
object Obj {
  fun String.foo()
}
```
在 Kotlin `1.3`，要调用 `foo`，必须使用：
```kotlin
Obj.run {
  "str".foo()
}
```
因为 IDE 不会自动为 `String.foo` 添加 `import`。

Kotlin `1.4` 解决了这个问题。在使用 `"str".foo` 时 Kotlin 会自动添加 `org.example.Obj.foo` 的引用。

Mirai Console 很多单例对象都设计为 `interface + companion object INSTANCE` 的接口与实现模式，需要这样的新特性。例如：
```kotlin
interface MiraiConsole {
    companion object INSTANCE : MiraiConsole by MiraiConsoleImpl // MiraiConsoleImpl 是内部实现，不公开
}
```

#### Mirai Console 演进

Mirai Console 是不断前进的框架，将来必定会发生 API 弃用和重构。  
维护者会严谨地推进每一项修改，并提供迁移周期（至少 2 个次版本）。

##### 版本规范

Mirai Console 的版本号遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/#spec-item-9) 规范。

在大版本开发过程中，Mirai Console 会以 `-M1`, `-M2` 等版本后缀发布里程碑预览版本。代表一些功能基本完成，但还不稳定。  
但这些版本里新增的 API 可能还会在下一个 Milestone 版本变化，因此请按需使用。

在大版本即将发布前，Mirai Console 会以 `-RC` 版本后缀发布最终的预览版本。  
`RC` 表示新版本 API 已经确定，离稳定版发布只差最后的一些内部优化或 bug 修复。

##### 更新兼容性

对于 `x.y.z` 版本号:
- 当 `z` 增加时，只会有 bug 修复，和必要的新函数添加（为了解决某一个问题），不会有破坏性变化。
- 当 `y` 增加时，可能有新 API 的引入，和旧 API 的弃用。但这些弃用会经过一个弃用周期后才被删除（隐藏）。向下兼容得到保证。
- 当 `x` 增加时，任何 API 都可能会有变化。无兼容性保证。

##### 弃用周期

一个计划被删除的 API，将会在下一个次版本开始经历弃用周期。

如一个 API 在 `1.1.0` 起被弃用，它首先会是 `WARNING` (使用时会得到一个编译警告）弃用级别。  
在 `1.2.0` 上升为 `ERROR`（使用时会得到一个编译错误）；  
在 `1.3.0` 上升为 `HIDDEN`（使用者无法看到这些 API)。

`HIDDEN` 的 API 仍然会保留在代码中并正常编译，以提供二进制兼容性，直到下一个主版本更新。

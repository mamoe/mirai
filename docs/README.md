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
- 对于 Kotlin 使用者，请熟知 [Kotlin `1.4` 版本带来的新特性](#mirai-console-使用的-kotlin-14-版本的新特性)
## 目录

### 后端插件开发基础

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


## 附录

### Java 用户的使用指南

- Java 中的「方法」在 Kotlin 中均被成为「函数」。
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
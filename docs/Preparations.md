# Mirai Console - Preparations

***如果跳过本节内容，你很可能遇到无法解决的问题。***

***此文档假设你是 JVM 平台的开发者。若不是，请参考[其他语言 SDK](https://github.com/mamoe/mirai#%E5%BC%80%E5%8F%91%E8%80%85)***

### JVM 环境要求

- 桌面 JVM：最低 Java 8，但推荐 Java 11
- Android：Android SDK 26+ （Android 8.0)

### 开发插件的准备工作

#### 安装 IDE 插件

推荐使用 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 或 [Android Studio](https://developer.android.com/studio)。Mirai Console 提供 IntelliJ 插件来提升开发体验。

- [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge) ([JetBrains 插件仓库](https://plugins.jetbrains.com/plugin/14816-kotlin-jvm-blocking-bridge), [一键安装](https://plugins.jetbrains.com/embeddable/install/14816))：帮助 Java 用户调用 Kotlin suspend 函数
- [Mirai Console IntelliJ](../tools/intellij-plugin/) ([JetBrains 插件仓库](https://plugins.jetbrains.com/plugin/15094-mirai-console), [一键安装](https://plugins.jetbrains.com/embeddable/install/15094))：提供错误检查等功能

## 前置知识

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
- 对于 Java 使用者，请阅读：
  - [Java 用户的使用指南](#kotlin-源码阅读帮助)
  - [在 Java 使用 Mirai Console 中的 Kotlin `suspend` 函数](#在-java-使用-mirai-console-中的-kotlin-suspend-函数)
- 对于 Kotlin 使用者，请熟知 [Kotlin `1.4` 版本带来的新特性](#mirai-console-使用的-kotlin-14-版本的新特性)


### Kotlin 源码阅读帮助

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

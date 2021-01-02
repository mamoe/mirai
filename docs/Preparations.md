# Mirai - Preparations

***此文档假设你是 JVM 平台的开发者。若不是，请参考 [其他语言 SDK](README.md#确定-sdk)***

## JVM 环境要求

- 桌面 JVM：最低 Java 8，但推荐 Java 11（要使用一键启动器，需要 11）
- Android：Android SDK 26+ （Android 8.0，Oreo)


**但注意不要使用 Oracle JDK**（[原因](https://github.com/mamoe/mirai/discussions/779)），推荐使用 OpenJDK（可以在 [Red Hat Developer](https://developers.redhat.com/products/openjdk/download) 下载）。

## 开发的准备工作

### 安装 IDE 插件

推荐使用 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 或 [Android Studio](https://developer.android.com/studio)。Mirai 提供一系列 IntelliJ 插件来提升开发体验。

- [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge) ([JetBrains 插件仓库](https://plugins.jetbrains.com/plugin/14816-kotlin-jvm-blocking-bridge), [一键安装](https://plugins.jetbrains.com/embeddable/install/14816))：**帮助 Java 用户调用 Kotlin suspend 函数**
- [Mirai Console IntelliJ](https://github.com/mamoe/mirai-console/tree/master/tools/intellij-plugin) ([JetBrains 插件仓库](https://plugins.jetbrains.com/plugin/15094-mirai-console), [一键安装](https://plugins.jetbrains.com/embeddable/install/15094))：提供错误检查等功能


*如果你不知道这俩是什么，安装就对了。*

## 前置知识

要能流畅使用 Mirai, 建议学习：

- 掌握 Java 基础
- 粗略了解 Kotlin 基础语法（15 分钟）:
  - [基本类型](https://www.kotlincn.net/docs/reference/basic-types.html)
  - [类与继承](https://www.kotlincn.net/docs/reference/classes.html)
  - [属性与字段](https://www.kotlincn.net/docs/reference/properties.html)
  - [接口](https://www.kotlincn.net/docs/reference/interfaces.html)
  - [扩展](https://www.kotlincn.net/docs/reference/extensions.html)
  - [数据类](https://www.kotlincn.net/docs/reference/data-classes.html)
  - [对象](https://www.kotlincn.net/docs/reference/object-declarations.html)
  - [密封类](https://www.kotlincn.net/docs/reference/sealed-classes.html)
  - **[Java 中调用 Kotlin](https://www.kotlincn.net/docs/reference/java-to-kotlin-interop.html)**
- 对于 Java 使用者，请阅读（1 分钟）：
  - [在 Java 使用 Kotlin `suspend` 函数](#在-java-使用-kotlin-suspend-函数)

### 在 Java 使用 Kotlin `suspend` 函数

`suspend` 函数中文是「挂起函数」，是 Kotlin 「[协程](https://www.kotlincn.net/docs/reference/coroutines/coroutines-guide.html)」的一部分。例如 `public suspend fun foo(): String` 被 `suspend` 修饰，它就是一个挂起函数。

对于一个挂起函数:
```kotlin
suspend fun test(): String
```

它会被 Kotlin 编译器编译为等同于 Java 的 `public Object test(Continuation<String> $completion)`。`Continuation` 类似一个回调，要实现它需要熟悉 Kotlin 协程实现原理。

Mamoe 为此开发了 Kotlin 编译器插件 [Kotlin Jvm Blocking Bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge)，通过 `@JvmBlockingBridge` 注解，在编译期额外生成一个供 Java 使用的方法，让 Java 用户可以使用拥有源码内相同的函数签名的方法。
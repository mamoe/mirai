# Mirai - Preparations

本章节介绍 Mirai 的 JVM 环境和开发准备工作。

## JVM 环境要求

- 桌面 JVM：最低 Java 8，但推荐 Java 17（要使用一键启动器，需要 11 及以上）
- Android：
    - mirai 2.15.0 起: API 等级 21 （Android 5.0，LOLLIPOP)
    - mirai 2.15.0 前: API 等级 26 （Android 8.0，O)

目前主要使用的自动启动器，[Mirai Console Loader](https://github.com/iTXTech/mirai-console-loader)
，（MCL） 默认安装 JRE 17。

**但注意不要使用 Oracle JDK**
（[原因](https://github.com/mamoe/mirai/discussions/779)），可以使用其他任何 JDK。

> 要下载 JDK：
> - 手动下载安装如 [AdoptOpenJDK](https://adoptopenjdk.net/)
> - 自动在 IntelliJ IDEA `Project Structure`(`Ctrl+Shift+Alt+S`) -> `SDKs` -> `+` -> `Download JDK` 下载安装

## 开发的准备工作

### 安装 IDE 插件

[Mirai Console IntelliJ]: /mirai-console/tools/intellij-plugin

[Mirai Console IntelliJ-JB]: https://plugins.jetbrains.com/plugin/15094-mirai-console

[Mirai Console IntelliJ-OK]: https://plugins.jetbrains.com/embeddable/install/15094

<!--[Kotlin Jvm Blocking Bridge]: https://github.com/mamoe/kotlin-jvm-blocking-bridge-->
<!--[Kotlin Jvm Blocking Bridge-JB]: https://plugins.jetbrains.com/plugin/14816-kotlin-jvm-blocking-bridge-->
<!--[Kotlin Jvm Blocking Bridge-OK]: https://plugins.jetbrains.com/embeddable/install/14816-->

推荐使用 [IntelliJ IDEA](https://www.jetbrains.com/idea/)
或 [Android Studio](https://developer.android.com/studio)。Mirai 提供 IDE
插件来提升开发体验。

|           插件名            |                     描述                     |               一键安装                |          JetBrains 插件仓库          |
|:------------------------:|:------------------------------------------:|:---------------------------------:|:--------------------------------:|
| [Mirai Console IntelliJ] | 提供 mirai-core 的错误检查和 mirai-console 的插件开发辅助 | [一键安装][Mirai Console IntelliJ-OK] | [说明页][Mirai Console IntelliJ-JB] |

<!--| [Kotlin Jvm Blocking Bridge] |         帮助 Java 用户调用 Kotlin suspend 函数         | [Kotlin Jvm Blocking Bridge-OK] | [Kotlin Jvm Blocking Bridge-JB] |-->

使用 Kotlin 建议安装 Mirai Console IntelliJ。同时请确保 Kotlin 插件是最新版本（在 `Settings -> Plugins` 启用并更新 Kotlin 到最新）。

### Kotlin

[Kotlin](https://kotl.in) 是让开发人员更快乐的一门现代编程语言，由 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 的开发公司 [JetBrains](https://www.jetbrains.com/) 维护，被 Google 推举为 Android 首选编程语言。

使用 Mirai 是一个不错的学习 Kotlin 机会，使用者有兴趣可以在 [官方中文文档](https://www.kotlincn.net/docs/reference/) 学习 Kotlin。

Java 开发者如果只希望使用 Mirai 而不学习 Kotlin，也请阅读 [Kotlin 定义对应的 Java 定义](KotlinAndJava.md)（5 分钟）。


----

> [回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

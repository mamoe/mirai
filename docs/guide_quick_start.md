# Mirai Guide - Quick Start

由于Mirai项目在快速推进中，因此内容时有变动，本文档的最后更新日期为```2020-02-20```，对应版本```0.17.0```

本文适用于对kotlin较熟悉的开发者

**若你希望一份更为基础且详细的guide**, 请参阅: [mirai-guide-getting-started](guide_getting_started.md)

**若你希望使用 Java 开发**, 请参阅: [mirai-japt](/mirai-japt/README.md)

## Build Requirements

- Kotlin 1.3.61 
- JDK 6 (required)
- JDK 11（for protocol tools, optional）
- Android SDK 29 (for Android target, optional)

## Use directly

**直接使用 Mirai(终端环境/网页面板（将来））.**  
[Mirai-Console](https://github.com/mamoe/mirai/tree/master/mirai-console) 插件支持, 在终端中启动 Mirai 并获得机器人服务  
本模块还未完善。

## Use as a library

**mirai-core 为独立设计, 可以作为库内置于任意 Java(JVM)/Android 项目中使用.**

请将 `VERSION` 替换为最新的版本(如 `0.15.0`):
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
**Mirai 目前还处于实验性阶段, 我们无法保证任何稳定性, API 也可能会随时修改.**

### Maven

Kotlin 在 Maven 上只支持 JVM 平台.

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-qqandroid-jvm</artifactId>
        <version>0.15.1</version> <!-- 替换版本为最新版本 -->
    </dependency>
</dependencies>
```

### Gradle

Mirai 只发布在 `jcenter`, 因此请确保添加 `jcenter()` 仓库：

```kotlin
repositories{
  jcenter()
}
```

若您需要使用在跨平台项目, 则要对各个目标平台添加不同的依赖，这与 kotlin 相关多平台库的依赖是类似的。  
**若您只需要使用在单一平台, 则只需要添加一项该平台的依赖.**

**注意：**
Mirai 核心由 API 模块（`mirai-core`）和协议模块组成。  
只添加 API 模块将无法正常工作。  
现在只推荐使用 QQAndroid 协议，请参照下文选择对应目标平台的依赖添加。

**jvm** (JVM 平台)

```kotlin
implementation("net.mamoe:mirai-core-qqandroid-jvm:VERSION")
```

**common** (通用平台)

```kotlin
implementation("net.mamoe:mirai-core-qqandroid-common:VERSION")
```

**android** (Android 平台)

```kotlin
implementation("net.mamoe:mirai-core-qqandroid-android:VERSION")
```

## Try

### On JVM or Android

现在体验低付出高效率的 Mirai

```kotlin
val bot = Bot(qqId, password).alsoLogin()
bot.subscribeMessages {
  "你好" reply "你好!"
  "profile" reply { sender.queryProfile() }
  contains("图片"){ File(imagePath).send() }
}
bot.subscribeAlways<MemberPermissionChangedEvent> {
  if (it.kind == BECOME_OPERATOR)
    reply("${it.member.id} 成为了管理员")
}
```

### Performance

Android 上, Mirai 运行需使用 80M 内存.  
JVM 上启动需 80M 内存, 每多一个机器人实例需要 30M 内存.


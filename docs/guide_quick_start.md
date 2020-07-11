# Mirai Guide - Quick Start

由于 mirai 项目在快速推进中，因此内容时有变动，本文档的最后更新日期为```2020/6/22```，对应版本```1.0.2```

本文适用于对 Kotlin 较熟悉的开发者,  
使用 mirai 作为第三方依赖库引用到任意一个 Kotlin, Java 或其他 JVM 平台的项目

**若你希望一份更基础的教程**, 请参阅: [mirai-guide-getting-started](guide_getting_started.md)

## 构建需求

- JDK 6 或更高

## 获取 Demo
可在 [mirai-demos](https://github.com/mamoe/mirai-demos) 中获取已经配置好依赖的示例项目.

## Quick Start

请将 `VERSION` 替换为 `mirai-core` 的最新版本号(如 `1.0.4`):
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  

### 添加依赖
可通过以下三种方法之一添加 mirai 依赖.

#### Maven

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
        <artifactId>mirai-core-qqandroid</artifactId>
        <version>0.23.0</version> <!-- 替换版本为最新版本 -->
    </dependency>
</dependencies>
```

#### Gradle (推荐)

Mirai 只发布在 `jcenter`, 因此请确保添加 `jcenter()` 仓库：

```kotlin
repositories{
  jcenter()
}
```

**注意：**
Mirai 核心由 API 模块（`mirai-core`）和协议模块组成。依赖协议模块时会自动依赖相应版本的 API 模块。
请参照下文选择目标平台的依赖添加。

如果你只用 Java / Kotlin 或其他语言开发 JVM 平台应用，只需要添加下文第一条。
如果你只开发 Android 应用，只需添加下文第三条。

**jvm** (JVM 平台源集)

```kotlin
implementation("net.mamoe:mirai-core-qqandroid:VERSION")
```

**common** (Kotlin 多平台项目的通用源集)

```kotlin
implementation("net.mamoe:mirai-core-qqandroid-common:VERSION")
```

**android** (Android 平台源集)
**注意**: 在 [KT-37152](https://youtrack.jetbrains.com/issue/KT-37152) 修复前, mirai 无法支持 Android 平台目标.
```kotlin
implementation("net.mamoe:mirai-core-qqandroid-android:VERSION")
```

#### 直接导入jar包 (不推荐)
下载已经编译好的 Jar 包, 并添加 Jar 依赖:
- [mirai-core](https://github.com/mamoe/mirai-repo/tree/master/shadow/mirai-core)
- [mirai-qqandriod](https://github.com/mamoe/mirai-repo/tree/master/shadow/mirai-core-qqandroid)


### 开始使用

```kotlin
val bot = Bot(qqId, password).alsoLogin()
bot.subscribeAlways<GroupMessageEvent> { event ->
  if (event.message.content.contains("你好")) {
     reply("你好!")
  } else if (event.message.content.contains("你好")) {
     File("C:\\image.png").sendAsImage()
  } 
}

bot.subscribeAlways<MemberPermissionChangedEvent> { event ->
  if (event.kind == BECOME_OPERATOR)
    reply("${event.member.id} 成为了管理员")
}
```

# Mirai Guide - Quick Start

由于 mirai 项目在快速推进中，因此内容时有变动，本文档的最后更新日期为```2020/3/26```，对应版本```0.30.1```

本文适用于对 Kotlin 较熟悉的开发者,  
使用 mirai 作为第三方依赖库引用任意一个 Kotlin, Java 或其他 JVM 平台的项目

**若你希望一份更基础的教程**, 请参阅: [mirai-guide-getting-started](guide_getting_started.md)

**若你希望使用 Java 开发**, 请参阅: [mirai-japt](https://github.com/mamoe/mirai-japt)

## 构建需求

- Kotlin 1.3.71 (必须)
- JDK 6 或更高 (必须)

## 获取 Demo
可在 [mirai-demos](https://github.com/mamoe/mirai-demos) 中获取已经配置好依赖的示例项目.

## Quick Start

请将 `VERSION` 替换为 `mirai-core` 的最新版本号(如 `0.23.0`):
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  

### 添加依赖

#### Maven

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
        <version>0.23.0</version> <!-- 替换版本为最新版本 -->
    </dependency>
</dependencies>
```

#### Gradle

Mirai 只发布在 `jcenter`, 因此请确保添加 `jcenter()` 仓库：

```kotlin
repositories{
  jcenter()
}
```

**注意：**
Mirai 核心由 API 模块（`mirai-core`）和协议模块组成。  
只添加 API 模块将无法正常工作。  
现在只推荐使用 QQAndroid 协议，请参照下文选择对应目标平台的依赖添加。

**jvm** (JVM 平台源集)

```kotlin
implementation("net.mamoe:mirai-core-qqandroid-jvm:VERSION")
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

### 开始使用

```kotlin
val bot = Bot(qqId, password).alsoLogin()
bot.subscribeMessages {
  "你好" reply "你好!"
  contains("图片"){ File("C:\\image.png").sendAsImage() }
}
bot.subscribeAlways<MemberPermissionChangedEvent> {
  if (it.kind == BECOME_OPERATOR)
    reply("${it.member.id} 成为了管理员")
}
```

# Mirai - Configuring Projects

本文介绍如何在一个 JVM 项目中使用 mirai。

### 选择版本

有关各类版本的区别，参考 [版本规范](Evolution.md#版本规范)

[Version]: https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg?
[Bintray Download]: https://bintray.com/him188moe/mirai/mirai-core/

| 版本类型 |             版本号              |
|:------:|:------------------------------:|
|  稳定   |             2.4.2              |
|  预览   |             2.5-M2             |
|  开发   | [![Version]][Bintray Download] |

### 配置项目

本文提供如下三种配置方法。推荐使用 Gradle 构建。

- [A. 使用 Gradle](#a-使用-gradle)
- [B. 使用 Maven](#b-使用-maven)
- [C. 下载 JAR 包](#c-下载-jar-包)


## A. 使用 Gradle

### Gradle Kotlin DSL

在 `build.gradle.kts` 添加：

```kotlin
plugins {
    kotlin("jvm") version "1.4.31" // 确保添加 Kotlin
}

dependencies {
    api("net.mamoe", "mirai-core", "2.4.1")
}
```

**注意，必须添加 Kotlin 插件才能正确获取 mirai 软件包。**

> 依赖配置完成，请选择：
> - [分离 API 和实现（可选）](#分离-api-和实现可选)
> - [回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

### Gradle Groovy DSL

在 `build.gradle` 添加

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.4.31' // 确保添加 Kotlin
}

dependencies {
    api('net.mamoe', 'mirai-core', '2.4.1')
}
```

> 依赖配置完成，请选择：
> - [分离 API 和实现（可选）](#分离-api-和实现可选)
> - [回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

### 分离 API 和实现（可选）

mirai 在开发时需要 `net.mamoe:mirai-core-api`, 在运行时需要 `net.mamoe:mirai-core`。可以在开发和编译时只依赖 `mirai-core-api`，会减轻对 IDE 的负担。
```kotlin
dependencies {
    val miraiVersion = "2.4.1"
    api("net.mamoe", "mirai-core-api", miraiVersion)     // 编译代码使用
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion) // 运行时使用
}
```


## B. 使用 Maven

在 `pom.xml` 中添加 mirai 依赖：

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-jvm</artifactId>
        <version>2.4.1</version> <!-- 替换版本为你需要的版本 -->
    </dependency>
</dependencies>
```

> 注意在 Maven，artifactId 要使用带 `-jvm` 后缀的


通常 mirai 可以直接使用。但 mirai 使用的 Kotlin 1.4 可能与你的项目使用的其他库依赖的 Kotlin 版本冲突，Maven 有时候无法正确处理这种冲突。此时请手动添加 Kotlin 标准库依赖。

```xml
<properties>
    <kotlin.version>1.4.31</kotlin.version>
</properties>
```
```xml
<dependencies>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-stdlib-jdk8</artifactId>
        <version>${kotlin.version}</version>
    </dependency>
</dependencies>
```

> 可以在 [Kotlin 官方文档](https://www.kotlincn=.net/docs/reference/using-maven.html) 获取更多有关配置 Kotlin 的信息。


> 依赖配置完成，[回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

## C. 下载 JAR 包

非常不推荐这种方法，请尽可能使用构建工具。

在 [Jcenter](https://jcenter.bintray.com/net/mamoe/mirai-core-all/) 或 [阿里云代理仓库](https://maven.aliyun.com/repository/public/net/mamoe/mirai-core-all/) 下载指定版本的 `-all.jar` 文件，即包含 `mirai-core`，`mirai-core-api`，`mirai-core-utils` 和其他依赖。

> [回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

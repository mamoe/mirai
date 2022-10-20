# Mirai - Configuring Projects

本文介绍如何在一个 JVM 项目中使用 mirai。

具体项目可参考 [mirai-hello-world](https://github.com/project-mirai/mirai-hello-world)。

### 选择版本

有关各类版本的区别，参考 [版本规范](Evolution.md#版本规范)。通常建议选择最新稳定版本。

[Maven Central Version]: https://img.shields.io/maven-central/v/net.mamoe/mirai-core-api.svg?label=Maven%20Central
[Maven Central]: https://search.maven.org/search?q=net.mamoe%20mirai
[GitHub Releases]: https://github.com/mamoe/mirai/releases/latest
[GR all]: https://github.com/mamoe/mirai/releases/

| 版本类型 |              版本号链接              |
|:------:|:-----------------------------------:|
|  稳定   |          [GitHub Releases]          |
|  预览   |     [GitHub Releases][GR all]       |
|  开发   | [UsingSnapshots](UsingSnapshots.md) |

### 配置项目

本文提供如下三种配置方法。推荐使用 Gradle 构建。

**注意，下文版本号可能过旧，请自行参照上述表格更新版本号**

- [A. 使用 Gradle](#a-使用-gradle)
- [B. 使用 Maven](#b-使用-maven)
- [C. 下载 JAR 包](#c-下载-jar-包)


## A. 使用 Gradle

### Gradle Kotlin DSL

在 `build.gradle.kts` 添加：

```kotlin
plugins {
    kotlin("jvm") version "1.5.30" // 确保添加 Kotlin
}

dependencies {
    api("net.mamoe", "mirai-core", "2.9.1")
}
```

**注意，必须添加 Kotlin 插件才能正确获取 mirai 软件包。**

> 依赖配置完成，请选择：
> - [分离 API 和实现（可选）](#分离-api-和实现可选)
> - [回到 Mirai 文档索引](README.md#使用-mirai)

### Gradle Groovy DSL

在 `build.gradle` 添加

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.5.30' // 确保添加 Kotlin
}

dependencies {
    implementation 'net.mamoe:mirai-core:2.9.1'
}
```

> 依赖配置完成，请选择：
> - [分离 API 和实现（可选）](#分离-api-和实现可选)
> - [回到 Mirai 文档索引](README.md#使用-mirai)

### 分离 API 和实现（可选）

Mirai 在开发时需要 `net.mamoe:mirai-core-api`, 在运行时需要 `net.mamoe:mirai-core`。可以在开发和编译时只依赖 `mirai-core-api`，会减轻对 IDE 的负担。  
在 2.8.0 起 Mirai 提供 `mirai-bom` 用于自动协调 Mirai 不同组件的版本信息，这是引用 Mirai 平台的首选方式。
使用 `mirai-bom` 也会对 Dependabot 等自动化依赖管理程序更加友好。
```kotlin
dependencies {
    api(platform("net.mamoe:mirai-bom:2.9.1"))
    api("net.mamoe:mirai-core-api")     // 编译代码使用
    runtimeOnly("net.mamoe:mirai-core") // 运行时使用
}
```
也可以继续使用如下传统方式，但务必保证 `mirai-core-api` 和 `mirai-core` 的版本号相一致，以避免潜在的异常。  
尤其注意 Dependabot 等依赖管理程序可能会导致模块版本不同。
```kotlin
dependencies {
    val miraiVersion = "2.9.1"
    api("net.mamoe", "mirai-core-api", miraiVersion)     // 编译代码使用
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion) // 运行时使用
}
```

## B. 使用 Maven

> 推荐使用 gradle, 使用 maven 您可能会遇到各种奇怪的依赖错乱问题

在 `pom.xml` 中添加 mirai 依赖：

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-jvm</artifactId>
        <version>2.9.1</version> 
    </dependency>
</dependencies>
```

> 注意在 Maven，artifactId 要使用带 `-jvm` 后缀的


通常 mirai 可以直接使用。但 mirai 使用的 Kotlin 1.5 可能与你的项目使用的其他库依赖的 Kotlin 版本冲突，Maven 有时候无法正确处理这种冲突。此时请手动添加 Kotlin 标准库依赖。

```xml
<properties>
    <kotlin.version>1.5.10</kotlin.version>
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

> 可以在 [Kotlin 官方文档](https://www.kotlincn.net/docs/reference/using-maven.html) 获取更多有关配置 Kotlin 的信息。


> 依赖配置完成，[回到 Mirai 文档索引](README.md#使用-mirai)

## C. 下载 JAR 包

非常不推荐这种方法，请尽可能使用构建工具。

在 [Maven Central](https://repo.maven.apache.org/maven2/net/mamoe/mirai-core-all/) 或 [阿里云代理仓库](https://maven.aliyun.com/repository/central/net/mamoe/mirai-core-all/) 下载指定版本的 `-all.jar` 文件，即包含 `mirai-core`，`mirai-core-api`，`mirai-core-utils` 和其他依赖。

> [回到 Mirai 文档索引](README.md#使用-mirai)

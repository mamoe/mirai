# Mirai - Configuring Projects

本文介绍如何在一个项目中使用 mirai。

mirai 使用纯 Kotlin 开发，兼容 JVM 平台语言如 Java，最低要求 `JDK 1.8`，`Kotlin 1.4`。

### 选择版本

有关各类版本的区别，参考 [版本规范](Evolution.md#版本规范)

[Version]: https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg?
[Bintray Download]: https://bintray.com/him188moe/mirai/mirai-core/

| 版本类型 |             版本号              |
|:------:|:------------------------------:|
|  稳定   |             1.3.3              |
|  预览   |            2.0-M2-2            |
|  开发   | [![Version]][Bintray Download] |

**即使 2.0 还没有稳定，也建议使用 2.0 预览版本，因 1.x 版本将不会收到任何更新。**

### 配置项目

如果你熟悉 Gradle，只需要添加 `jcenter` 仓库和依赖 `net.mamoe:mirai-core:VERSION` 即可而不需要继续阅读。下文将详细解释其他方法。

本文提供如下三种配置方法，但推荐使用 Gradle 构建。

- [A. 使用 Gradle](#a-使用-gradle)
- [B. 使用 Maven](#b-使用-maven)
- [D. 下载 JAR 包](#c-下载-jar-包)


## A. 使用 Gradle

### Gradle Kotlin DSL

在 `build.gradle.kts` 添加：

```kotlin
plugins {
    kotlin("jvm") version "1.4.21"
}

repositories {
    jcenter()
}

dependencies {
    api("net.mamoe", "mirai-core", "1.3.3") // 替换为你需要的版本号
}
```

注意，必须添加 Kotlin 插件才能正确获取 mirai 软件包。

### Gradle Groovy DSL

在 `build.gradle` 添加

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.4.21'
}

repositories {
    jcenter()
}

dependencies {
    api('net.mamoe', 'mirai-core', '1.3.3') // 替换为你需要的版本号
}
```


## B. 使用 Maven

在 `pom.xml` 中：

### 1. 添加 jcenter 仓库
```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

### 2. 添加 mirai 依赖

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-jvm</artifactId>
        <version>1.3.3</version> <!-- 替换版本为你需要的版本 -->
    </dependency>
</dependencies>
```

### 3. 添加 Kotlin 依赖

通常 mirai 可以直接使用。但 mirai 使用的 Kotlin 1.4 可能与你的项目使用的其他库依赖的 Kotlin 版本冲突，Maven 无法正确处理这种冲突。此时请手动添加 Kotlin 标准库依赖。

```xml
<properties>
    <kotlin.version>1.4.20</kotlin.version>
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

## C. 下载 JAR 包

非常不推荐这种方法，请尽可能使用构建工具。

在 [Jcenter](https://jcenter.bintray.com/net/mamoe/mirai-core-all/) 或 [阿里云代理仓库](https://maven.aliyun.com/repository/public/net/mamoe/mirai-core-all/) 下载指定版本的 `-all.jar` 文件，即包含 `mirai-core`，`mirai-core-api`，`mirai-core-utils` 和其他依赖。

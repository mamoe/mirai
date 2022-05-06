# Mirai - Using Snapshots

每个 commit 在构建成功后都会发布一个开发测试版本到 mirai 仓库。如有需要，可添加仓库并使用。开发测试版本非常不稳定，仅用于测试某 commit 对一个问题的修复情况，而不建议在生产或开发环境使用。

每个开发测试版本只保留一个月。

- [在 Maven 使用](#在-maven-使用)
- [在 Gradle 使用](#在-gradle-使用)

## 在 Maven 使用

### 1. 添加 Maven 仓库

```xml
<repositories>
    <repository>
        <id>miraisnapshots</id>
        <name>mirai snapshots</name>
        <url>https://repo.mirai.mamoe.net/snapshots</url>
    </repository>
</repositories>
```

### 2. 修改依赖版本

1. 选择需要测试的 commit, 找到其 revision id (即 SHA), 取前 8 位, 如 `3cb39c4`.
2. 在该 commit 所属分支的 `buildSrc/src/main/kotlin/Versions.kt` 确认 mirai 主版本号如 `2.8.0-M1`.
3. 得到开发测试版本号 `2.8.0-M1-dev-3cb39c4`.

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-jvm</artifactId>
        <version>2.8.0-M1-dev-3cb39c4</version>
    </dependency>
</dependencies>
```

## 在 Gradle 使用


### 1. 添加 Maven 仓库

build.gradle(.kts)
```
repositories {
   maven("https://repo.mirai.mamoe.net/snapshots") 
}
```

### 2. 修改依赖版本

1. 选择需要测试的 commit, 找到其 revision id (即 SHA), 取前 8 位, 如 `3cb39c4`.
2. 在该 commit 所属分支的 `buildSrc/src/main/kotlin/Versions.kt` 确认 mirai 主版本号如 `2.8.0-M1`.
3. 得到开发测试版本号 `2.8.0-M1-dev-3cb39c4`.

build.gradle(.kts)
```
dependencies {
    implementation("net.mamoe:mirai-core:2.8.0-M1-dev-3cb39c4")
}
```

## 使用测试版本 Mirai Console Gradle 插件

settings.gradle(.kts)
```
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mirai.mamoe.net/snapshots")
    }
}
```

plugin.gradle(.kts)
```
plugins {
    // ...
    id("net.mamoe.mirai-console") version "2.8.0-M1-dev-3cb39c4"
}
```
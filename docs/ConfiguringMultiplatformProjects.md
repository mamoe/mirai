# Mirai - Configuring Projects

本文介绍如何在一个 Kotlin 多平台项目中使用 mirai。

### 选择版本

可参考 [ConfiguringProjects](ConfiguringProjects.md#选择版本) 选择合适的版本。

## 支持的编译目标平台

mirai 上传到 Maven Central 的预编译模块列表如下表所示。在表中列举的平台即为你的项目可以使用的平台。
如果你使用了一个不受支持的平台，在构建项目时将会得到来自 Gradle 的依赖解决错误。

mirai 曾在 2.13.0 ~ 2.15.0-RC（不包含）支持编译到 macOS、Window、Linux 平台。自 2.15.0-RC 已完全删除对这些平台的支持。

| 发布平台名称  | 描述               |
|---------|------------------|
| jvm     | JVM              |
| android | Android (Dalvik) |

## 添加依赖

仅需为 `commonMain` 添加依赖，Kotlin 会自动为其他源集配置依赖。

Kotlin 编译器版本必须至少为 1.7.0，Gradle 版本建议使用高于 7.3。

额外添加 `net.mamoe:mirai-core-utils` 是为了临时解决 [#2275](https://github.com/mamoe/mirai/issues/2275)。

```kotlin
plugins {
    kotlin("multiplatform") version "1.7.20"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("net.mamoe:mirai-core:2.13.0")
                implementation("net.mamoe:mirai-core-utils:2.13.0")
            }
        }
    }
}
```

## 解决问题

如果你在使用多平台项目时遇到问题，那应该是正常的。Kotlin 多平台项目在 1.7 仍然是一个测试版功能。欢迎在 issues 提交多平台相关问题。

> 依赖配置完成，
> - [回到 Mirai 文档索引](README.md#使用-mirai)
# Mirai Console - Configuring Projects

配置 Mirai Console 项目。

## 模块说明

console 由后端和前端一起工作. 使用时必须选择一个前端.

- `mirai-console`: Mirai Console 后端。

- `mirai-console-terminal`: 终端前端，适用于 JVM。
- [`MiraiAndroid`](https://github.com/mzdluo123/MiraiAndroid): Android 应用前端。

**注意：`mirai-console` 1.0-RC 发布之前, 前端请使用 `mirai-console-pure` 而不是 `mirai-console-terminal`**

## 选择版本

有关各类版本的区别，参考 [版本规范](Appendix.md#版本规范)

[Version]: https://api.bintray.com/packages/him188moe/mirai/mirai-console/images/download.svg?
[Bintray Download]: https://bintray.com/him188moe/mirai/mirai-console/

| 版本类型 |    版本号     |
|:------:|:------------:|
|  稳定   |      -       |
|  预览   |    1.0-M4    |
|  开发   | [![Version]][Bintray Download] |

## 配置项目

### 使用模板项目

Mirai 鼓励插件开发者将自己的作品开源，并为此提供了模板项目。

注意，模板项目依赖的 Mirai Console 不一定是最新的。请检查

1. 访问 [mirai-console-plugin-template](https://github.com/project-mirai/mirai-console-plugin-template)
2. 点击绿色按钮 "Use this template"，创建项目
3. 克隆项目，检查并修改生成的属性

### 使用 Gradle 插件配置项目

`VERSION` 可在

若使用 `build.gradle.kts`:
```kotlin
plugins {
    id("net.mamoe.mirai-console") version "VERSION"
}
```

若使用 `build.gradle`:
```groovy
plugins {
    id 'net.mamoe.mirai-console' version 'VERSION'
}
```

完成。Mirai Console Gradle 插件会为你配置依赖等所有编译环境。

### 手动配置项目

添加依赖：
`build.gradle.kts`：
```kotlin
repositories {
  jcenter()
}

dependencies {
  compileOnly("net.mamoe:mirai-core:$CORE_VERSION") // mirai-core 的 API
  compileOnly("net.mamoe:mirai-console:$CONSOLE_VERSION") // 后端
  
  testImplementation("net.mamoe:mirai-console-terminal:$CONSOLE_VERSION") // 前端, 用于启动测试
  testImplementation("net.mamoe:mirai-console-terminal:$CONSOLE_VERSION") // 前端, 用于启动测试
}
```

之后还需要配置 Kotlin `jvm-default` 编译参数，Kotlin 和 Java 的编译目标等。  
在打包插件时必须将依赖一并打包进插件 JAR，且排除 `mirai-core`，`mirai-console` 和它们的间接依赖，否则插件不会被加载。
# Mirai Console - Configuring Projects

配置 Mirai Console 项目。

## 模块说明

console 由后端和前端一起工作. 使用时必须选择一个前端.

- `mirai-console`: Mirai Console 后端。

- `mirai-console-terminal`: 终端前端，适用于 JVM。
- [`MiraiAndroid`](https://github.com/mzdluo123/MiraiAndroid): Android 应用前端。
- [`mirai-compose`](https://github.com/sonder-joker/mirai-compose): 跨平台桌面图形前端。

## 选择版本

`mirai-console` 与 `mirai-core` 同步版本发布。版本号见 [mirai](https://github.com/mamoe/mirai/blob/dev/docs/ConfiguringProjects.md#%E9%80%89%E6%8B%A9%E7%89%88%E6%9C%AC)。

## 配置项目

请选择以下三种方法之一。

### 使用模板项目

Mirai 鼓励插件开发者将自己的作品开源，并为此提供了模板项目。

注意，模板项目依赖的 Mirai Console 不一定是最新的。请检查

1. 访问 [mirai-console-plugin-template](https://github.com/project-mirai/mirai-console-plugin-template)
2. 点击绿色按钮 "Use this template"，创建项目
3. 克隆项目，检查并修改生成的属性

### 使用 Gradle 插件配置项目

`VERSION`: [选择版本](#选择版本)

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

可以在 [README](../tools/gradle-plugin/README.md#mirai-console-gradle-plugin) 获取详细的 Gradle 插件使用方法，**如配置 mirai-core 版本**。

> 现在你已经配置好了项目，返回 [开发文档索引](README.md#mirai-console)

### 手动配置项目

添加依赖：
`build.gradle.kts`：
```kotlin
dependencies {
  compileOnly("net.mamoe:mirai-core:$CORE_VERSION") // mirai-core 的 API
  compileOnly("net.mamoe:mirai-console:$CONSOLE_VERSION") // 后端
  
  testImplementation("net.mamoe:mirai-console-terminal:$CONSOLE_VERSION") // 前端, 用于启动测试
}
```

注意，在打包插件时必须将依赖一并打包进插件 JAR，且排除 `mirai-core`，`mirai-console` 和[它们的间接依赖](https://mvnrepository.com/artifact/net.mamoe/mirai-core-jvm/2.4.0)，否则可能导致兼容性问题。

> 现在你已经配置好了项目，返回 [开发文档索引](README.md#mirai-console)


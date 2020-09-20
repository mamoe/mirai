# Mirai Console Gradle Plugin

Mirai Console Gradle 插件。

## 使用

参考 [ConfiguringProjects](../../docs/ConfiguringProjects.md#gradle)[

## 功能

- 为 `main` 源集配置 `mirai-core`，`mirai-console` 依赖
- 为 `test` 源集配置 `mirai-core-qqandroid`, `mirai-console-terminal` 的依赖 (用于启动测试)
- 添加 mirai 依赖仓库链接
- 配置插件 JAR 打包构建任务 `buildPlugin` (带依赖)


### `buildPlugin`

用于打包插件和依赖为可以放入 Mirai Console `plugins` 目录加载的插件 JAR。

#### 执行 `buildPlugin`
```shell script
$ gradlew buildPlugin
```

打包结果存放在 `build/mirai/` 目录下。

## 配置

若要修改 Mirai Console Gradle 插件的默认配置，在 `build.gradle.kts` 或 `build.gradle` 内，使用 `mirai`：
```kotlin
mirai { // this: MiraiConsoleExtension
    // 配置
}
```

DSL 详见 [MiraiConsoleExtension](src/main/kotlin/net/mamoe/mirai/console/gradle/MiraiConsoleExtension.kt)。

#### 排除依赖

如果要在打包 JAR（`buildPlugin`）时排除一些依赖，请使用如下配置：

```kotlin
mirai {
    excludeDependency("com.google.code.gson", "gson")
}
```

# Mirai Console Gradle Plugin

Mirai Console Gradle 插件。

## 使用

参考 [ConfiguringProjects](../../docs/ConfiguringProjects.md)

## 功能

- 为 `main` 源集配置 `mirai-core`，`mirai-console` 依赖
- 为 `test` 源集配置 `mirai-core-qqandroid`, `mirai-console-terminal` 的依赖 （用于启动测试）
- 配置 Kotlin 编译目标为 Java 1.8
- 配置 Kotlin 编译器 jvm-default 设置为 `all`, 即为所有接口中的默认实现生成 Java 1.8 起支持的 `default` 方法
- 配置 Java 编译目标为 Java 1.8
- 配置 Java 编译编码为 UTF-8
- 配置插件 JAR 打包构建任务 `buildPlugin`（带依赖, 成品 JAR 可以被 Mirai Console 加载）

支持 Kotlin 多平台项目（Multiplatform Projects）。每个 JVM 或 Android 目标平台都会被如上配置，对应打包任务带有编译目标的名称，如 `buildPluginJvm`

### `buildPlugin`

用于打包插件和依赖为可以放入 Mirai Console `plugins` 目录加载的插件 JAR。

#### 执行 `buildPlugin`
```shell script
./gradlew buildPlugin
```

打包结果存放在 `build/mirai/` 目录下。

## 配置

若要修改 Mirai Console Gradle 插件的默认配置，在 `build.gradle.kts` 或 `build.gradle` 内，使用 `mirai`：
```kotlin
mirai { // this: MiraiConsoleExtension
    // 配置
}
```

DSL 详见 [MiraiConsoleExtension](src/MiraiConsoleExtension.kt)。

#### 排除依赖

如果要在打包 JAR（`buildPlugin`）时排除一些依赖，请使用如下配置：

```kotlin
mirai {
    excludeDependency("com.google.code.gson", "gson")
}
```

插件一般不需要手动排除依赖。Mirai Console 已经包含的依赖都会自动在打包过程中被排除。
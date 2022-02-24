# Mirai Console Gradle Plugin

Mirai Console Gradle 插件。

## 在构建中使用插件

参考 [ConfiguringProjects](../../docs/ConfiguringProjects.md#b使用-gradle-插件配置项目)。

## 功能

- 为 `main` 源集配置 `mirai-core-api`，`mirai-console` 依赖
- 为 `test` 源集配置 `mirai-core`, `mirai-console-terminal` 的依赖 （用于启动测试）
- 配置 Kotlin 编译目标为 1.8
- 配置 Kotlin 编译器 jvm-default 设置为 `all`, 即为所有接口中的默认实现生成 Java 1.8 起支持的 `default` 方法
- 配置 Java 编译目标为 1.8
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
    
}
```

DSL 详见 [MiraiConsoleExtension](src/MiraiConsoleExtension.kt)。

### 打包依赖

Mirai Console Gradle 在打包 JAR（`buildPlugin`） 时不会携带任何外部依赖, 
而是会保存一份依赖列表，在加载插件时下载，
如果您使用了不可在 `Maven Central` 搜索到的依赖, 请使用以下配置告知 mirai-console-gradle

```groovy
dependencies {
    implementation "org.example:test:1.0.0"

    // 无需版本号
    shadowLink "org.example:test"
    // build.gradle.kts
    "shadowLink"("org.example:test")
}
```

### `publishPlugin`

配置好 Bintray 参数，使用 `./gradlew publishPlugin` 可自动发布并上传插件到 Bintray。

如果仓库是公开的，上传的插件在未来可以被 [mirai-console-loader](https://github.com/iTXTech/mirai-console-loader) 自动识别并展示在社区插件列表中。

```kotlin
mirai {
    publishing {
        repo = "mirai"
        packageName = "chat-command"
    }
}
```

*2021/3/21 更新:* 由于 Bintray JCenter 即将关闭，随着论坛的发展，mirai 正在策划插件中心服务。待插件中心完成后将会提供更好的插件分发平台。

#### 排除依赖 (过时)

如果要在打包 JAR（`buildPluginLegacy`）时排除一些依赖，请使用如下配置：

```kotlin
mirai {
    excludeDependency("com.google.code.gson", "gson")
}
```

**插件一般不需要手动排除依赖**。Mirai Console 已经包含的依赖都会自动在打包过程中被排除。
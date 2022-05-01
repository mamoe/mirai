# Mirai Console Gradle Plugin

Mirai Console Gradle 插件。

## 在构建中使用插件

参考 [ConfiguringProjects](../../docs/ConfiguringProjects.md#b使用-gradle-插件配置项目)
。

## 功能

- 为 `main` 源集配置 `mirai-core-api`，`mirai-console` 依赖
- 为 `test` 源集配置 `mirai-core`, `mirai-console-terminal` 的依赖 （用于启动测试）
- 配置 Kotlin 编译目标为 1.8
- 配置 Kotlin 编译器 `jvm-default` 设置为 `all`, 即为所有接口中的默认实现生成 Java 1.8
  起支持的 `default` 方法
- 配置 Java 编译目标为 1.8
- 配置 Java 编译编码为 UTF-8
- 配置插件 JAR 打包构建任务 `buildPlugin`（带依赖, 成品 JAR 可以被 Mirai Console 加载）

支持 Kotlin 多平台项目（Multiplatform Projects）。对于 MPP，每个 JVM 或 Android
目标平台都会被如上配置，对应打包任务带有编译目标的名称，如 `buildPluginJvm`。

### 任务 `buildPlugin`

用于打包插件和依赖为可以放入 Mirai Console `plugins` 目录加载的插件 JAR。

### 任务 `buildPluginLegacy`

用于打包插件和依赖为可以放入 Mirai Console `plugins` 目录加载的插件 JAR。

#### 执行 `buildPlugin`

```shell script
./gradlew buildPlugin
```

打包结果存放在 `build/mirai/` 目录下。

## 配置

若要修改 Mirai Console Gradle 插件的默认配置，在 `build.gradle.kts` 或 `build.gradle`
内，使用 `mirai`：

```kotlin
mirai { // this: MiraiConsoleExtension
    // 修改配置，如：
    jvmTarget = JavaVersion.VERSION_1_8
}
```

有关所有可修改的配置，参见 [MiraiConsoleExtension](src/main/kotlin/MiraiConsoleExtension.kt)
。

### 修改 Java 编译目标

Mirai Console Gradle 会覆盖 Java 编译目标为 `1.8`. 若要修改该值, 请通过:

```kotlin
mirai { // this: MiraiConsoleExtension
    jvmTarget = JavaVersion.VERSION_16
}
```

### 打包依赖

使用任务 `buildPlugin` 即可打包插件 JAR。打包结果输出在 `build/mirai/`。

自 2.11，Mirai Console Gradle 在打包 JAR（`buildPlugin`） 时默认不会携带外部依赖，
而是会保存一份依赖列表，在加载插件时下载。如果您使用了不可在默认仓库搜索到的依赖, 请以如下配置将依赖打包进入 JAR。

```kotlin
dependencies {
    implementation("org.example:test:1.0.0") // 正常地添加一个普通依赖，用于编译
    "shadowLink"("org.example:test") // 告知 mirai-console 在打包插件时包含此依赖；无需包含版本号
}
```

特别的, 如果使用了子项目，Mirai Console Gradle 默认也会打包进 JAR，通常这也是期望的行为。

如果您希望 Mirai Console Gradle 像处理一般依赖一样处理 Gradle 子项目（不打包），请使用以下配置：

```kotlin
dependencies {
    implementation(project(":nested")) // 正常地添加一个子项目依赖，用于编译
    "asNormalDep"(project(":nested")) // 告知 mirai-console 在打包插件时将此子项目依赖作为普通依赖处理，即不打包
}
```

> 要获取有关插件依赖的更多信息，可参考[插件文档](../../docs/plugin/JVMPlugin.md)。

#### 如何确定是否需要打包依赖

如果插件依赖的都是可在 Maven Central 找到的构建，则无需打包。

[//]: # (### `publishPlugin`)

[//]: # ()

[//]: # (配置好 Bintray 参数，使用 `./gradlew publishPlugin` 可自动发布并上传插件到 Bintray。)

[//]: # ()

[//]: # (如果仓库是公开的，上传的插件在未来可以被 [mirai-console-loader]&#40;https://github.com/iTXTech/mirai-console-loader&#41;)

[//]: # (自动识别并展示在社区插件列表中。)

[//]: # ()

[//]: # (```kotlin)

[//]: # (mirai {)

[//]: # (    publishing {)

[//]: # (        repo = "mirai")

[//]: # (        packageName = "chat-command")

[//]: # (    })

[//]: # (})

[//]: # (```)

[//]: # ()

[//]: # (*2021/3/21 更新:* 由于 Bintray JCenter 即将关闭，随着论坛的发展，mirai)

[//]: # (正在策划插件中心服务。待插件中心完成后将会提供更好的插件分发平台。)

#### 排除依赖 (过时)

在 2.11 以前，如果要在打包 JAR（`buildPluginLegacy`）时排除一些依赖，请使用如下配置：

```kotlin
mirai {
    excludeDependency("com.google.code.gson", "gson")
}
```

**插件一般不需要手动排除依赖**。Mirai Console 已经包含的依赖都会自动在打包过程中被排除。
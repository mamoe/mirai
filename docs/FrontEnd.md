# Mirai Console Frontend

Mirai Console 前端开发文档。

[`MiraiConsole`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsole.kt

## 实现前端

### 添加编译器设置

在 `build.gradle` 或 `build.gradle.kts` 添加：
```kotlin
kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("net.mamoe.mirai.console.ConsoleFrontEndImplementation")
    }
}
```

此后就可以使用 `net.mamoe.mirai.console.ConsoleFrontEndImplementation` 标记的所有 API。


### 实现 Mirai Console

[`MiraiConsole`] 是后端的公开对象，由 [MiraiConsoleImplementationBridge](../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/internal/MiraiConsoleImplementationBridge.kt) 代理，与前端链接。

前端需要实现 [MiraiConsoleImplementation.kt](../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleImplementation.kt)。

由于实现前端需要一定的技术能力，相信实现者都能理解源码内注释。

### 启动 Mirai Console

通过 `public fun MiraiConsoleImplementation.start()`。

[MiraiConsoleImplementation.kt: Line 161](../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/MiraiConsoleImplementation.kt#L161)
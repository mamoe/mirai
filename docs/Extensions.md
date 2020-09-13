# Mirai Console Backend - Extensions

Mirai Console 拥有灵活的 Extensions API，支持扩展 Console 的一些服务。

Extensions 属于插件开发的进阶内容。

[`Extension`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/extension/Extension.kt
[`ExtensionPoint`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/extension/ExtensionPoint.kt
[`PluginComponentStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/extension/PluginComponentStorage.kt
[`ComponentStorage`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/extension/ComponentStorage.kt

## [扩展][`Extension`]

### [组件容器][`ComponentStorage`]

容纳插件注册的 [扩展][`Extension`]。

### 注册扩展

插件仅能在 [`onLoad`](Plugins.md#加载) 阶段注册扩展。

示例：

```kotlin
object MyPlugin : KotlinPlugin( /* ... */ ) {
    fun PluginComponentStorage.onLoad() {
        contributePermissionService { /* ... */ }
        contributePluginLoader { /* ... */ }
        contribute(ExtensionPoint) { /* ... */ }
    }
}

```

### 可用扩展

查看 [extensions](../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/extensions/)。每个文件对应一个扩展。
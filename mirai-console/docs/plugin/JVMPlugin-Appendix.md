# Mirai Console Backend - JVM Plugins - Appendix

本页包含一些 JVM 插件的附录。

## 依赖管理

### API 导出管理

插件可能被其他插件依赖，插件可以将一些内部实现保护起来，避免其他插件调用。

要启动这个特性， 只需要在资源目录创建名为 `export-rules.txt` 的规则文件，便可以控制插件的类的公开规则。

如果正在使用 `Gradle` 项目, 该规则文件一般需要放置于 `src/main/resources` 下。

示例：

```text

# #开头的行全部识别为注释

# exports, 允许其他插件直接使用某个类

# 导出了一个internal包的一个类
#
exports org.example.miraiconsole.myplugin.internal.OpenInternal

# 导出了整个 api 包
#
exports org.example.miraiconsole.myplugin.api

# 保护 org.example.miraiconsole.myplugin.api2.Internal, 不允许其他插件直接使用
#
protects org.example.miraiconsole.myplugin.api2.Internal

# 保护整个包
#
# 别名: protect-package
protects org.example.miraiconsole.myplugin.internal

# 此规则不会生效, 因为在此条规则之前,
# org.example.miraiconsole.myplugin.internal 已经被加入到保护域中
exports org.example.miraiconsole.myplugin.internal.NotOpenInternal


# export-plugin, 允许其他插件使用除了已经被保护的全部类
# 使用此规则会同时让此规则后的所有规则全部失效
# 别名: export-all, export-system
# export-plugin


# 将整个插件放入保护域中
# 除了此规则之前显式 export 的类, 其他插件将不允许直接使用被保护的插件的任何类
# 别名: protect-all, protect-system
protect-plugin

```

插件也可以通过 Service 来自定义导出控制

Example:

```kotlin
@AutoService(ExportManager::class)
object MyExportManager : ExportManager {
    override fun isExported(className: String): Boolean {
        println("  <== $className")
        return true
    }
}
```

## 数据操作

### 读取 [`PluginData`] 或 [`PluginConfig`]

> 本节基于章节 [PluginData](../PluginData.md) 的内容。
> 在阅读本节前建议先阅读上述基础章节。也可以先跳过本节。

[`JvmPlugin`] 实现接口 [`AutoSavePluginDataHolder`]，提供:

Kotlin：

- `public fun <T : PluginData> T.reload()`
- `public fun <T : PluginConfig> T.reload()`

Java：

- `public fun reloadPluginData(PluginData)`
- `public fun reloadPluginData(PluginConfig)`

**仅可在插件 onEnable() 时及其之后才能使用这些方法。**  
**在插件 onDisable() 之后不能使用这些方法。**

#### 使用示例

```kotlin
object SchedulePlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.my-schedule-plugin",
        version = "1.0.0",
    ) {
        name("Schedule")

        // author("...")
        // dependsOn("...")
    }
) {
    // ...

    override fun onEnable() {
        MyData.reload() // 仅需此行，保证启动时更新数据，在之后自动存储数据。
    }
}

object MyData : AutoSavePluginData() {
    val value: Map<String, String> by value()
}
```

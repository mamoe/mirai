# mirai-console-intellij

[IntelliJ](https://www.jetbrains.com/idea/) 平台的 Mirai 和 Mirai Console 开发辅助插件，支持 IntelliJ IDEA Community/Ultimate，Android Studio。

主要提供一些编辑中的错误诊断。

## mirai-core 诊断

### `Message`

#### [UsingStringPlusMessageInspection](src/diagnostics/UsingStringPlusMessageInspection.kt#L33)

- 检查并报错 `String + Message` 的使用

> `String + Message` 实际上是 `String + Message.toString()`，`Message` 会被转为 `String` 再与 `String` 相加。

```kotlin
    val str: String = ""
    val plain: PlainText = PlainText("")
    str + plain
//  ^^^
//  使用 String + Message 会导致 Message 被转换为 String 再相加 
```

提供修复 [ConvertToPlainTextFix](src/diagnostics/fix/ConvertToPlainTextFix.kt#L26)

```kotlin
// before
str + plain
// after
PlainText(str) + plain
```

## mirai-console 诊断

### `Plugin`

#### [PluginMainServiceNotConfiguredInspection](src/diagnostics/PluginMainServiceNotConfiguredInspection.kt#L38)`PluginMainServiceNotConfiguredInspe

检查插件主类服务（即 `META-INF/services`）配置。在未正确配置时报错并提供自动修复 [ConfigurePluginMainServiceFix](src/diagnostics/fix/ConfigurePluginMainServiceFix.kt#L26):

```kotlin
object MyPluginMain : KotlinPlugin()
//     ^^^^^^^^^^^^
//     插件主类服务未配置
```

自动修复会在 `META-INF/services` 创建一个 `net.mamoe.mirai.console.plugin.jvm.JvmPlugin` 文件。

### `PluginDescription`

- ILLEGAL_PLUGIN_DESCRIPTION: 检查插件 ID, 名称等
- ILLEGAL_VERSION_REQUIREMENT: 检查插件依赖版本号

### `PluginData`

检查 `PluginData.value` 的泛型，

- NOT_CONSTRUCTABLE_TYPE: 在该类型无法被反射构造时报错
- UNSERIALIZABLE_TYPE: 在该类型无法被序列化时报错
- READ_ONLY_VALUE_CANNOT_BE_VAR: 检查 `ReadOnlyPluginData` 中的 `var` 并提供修复

> 通常能被反射构造的类型需要有一个公开的所有参数都可选的构造器。在 Java 则需一个公开无参构造器。

### `Command`

- ILLEGAL_COMMAND_NAME: 检查指令名称
- ILLEGAL_COMMAND_REGISTER_USE: 检查一些错误的 `CommandManager.registerCommand` 使用
- RESTRICTED_CONSOLE_COMMAND_OWNER: 检查错误的 `ConsoleCommandOwner` 使用
- ILLEGAL_COMMAND_DECLARATION_RECEIVER: 检查指令定义的接收者参数 (只允许 `CommandSender` 类型)

### `Permission`

- ILLEGAL_PERMISSION_NAME: 检查权限名称
- ILLEGAL_PERMISSION_NAMESPACE: 检查权限命名空间
- ILLEGAL_PERMISSION_REGISTER_USE: 检查一些错误的 `CommandManager.registerCommand` 使用

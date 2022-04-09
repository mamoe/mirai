# Mirai Console Backend - Commands


[`Plugin`]: ../backend/mirai-console/src/plugin/Plugin.kt
[`PluginDescription`]: ../backend/mirai-console/src/plugin/description/PluginDescription.kt
[`PluginLoader`]: ../backend/mirai-console/src/plugin/loader/PluginLoader.kt
[`PluginManager`]: ../backend/mirai-console/src/plugin/PluginManager.kt
[`JvmPluginLoader`]: ../backend/mirai-console/src/plugin/jvm/JvmPluginLoader.kt
[`JvmPlugin`]: ../backend/mirai-console/src/plugin/jvm/JvmPlugin.kt
[`JvmPluginDescription`]: ../backend/mirai-console/src/plugin/jvm/JvmPluginDescription.kt
[`AbstractJvmPlugin`]: ../backend/mirai-console/src/plugin/jvm/AbstractJvmPlugin.kt
[`KotlinPlugin`]: ../backend/mirai-console/src/plugin/jvm/KotlinPlugin.kt
[`JavaPlugin`]: ../backend/mirai-console/src/plugin/jvm/JavaPlugin.kt


[`Value`]: ../backend/mirai-console/src/data/Value.kt
[`PluginData`]: ../backend/mirai-console/src/data/PluginData.kt
[`AbstractPluginData`]: ../backend/mirai-console/src/data/AbstractPluginData.kt
[`AutoSavePluginData`]: ../backend/mirai-console/src/data/AutoSavePluginData.kt
[`AutoSavePluginConfig`]: ../backend/mirai-console/src/data/AutoSavePluginConfig.kt
[`PluginConfig`]: ../backend/mirai-console/src/data/PluginConfig.kt
[`PluginDataStorage`]: ../backend/mirai-console/src/data/PluginDataStorage.kt
[`MultiFilePluginDataStorage`]: ../backend/mirai-console/src/data/PluginDataStorage.kt#L116
[`MemoryPluginDataStorage`]: ../backend/mirai-console/src/data/PluginDataStorage.kt#L100
[`AutoSavePluginDataHolder`]: ../backend/mirai-console/src/data/PluginDataHolder.kt#L45
[`PluginDataHolder`]: ../backend/mirai-console/src/data/PluginDataHolder.kt
[`PluginDataExtensions`]: ../backend/mirai-console/src/data/PluginDataExtensions.kt

[`MiraiConsole`]: ../backend/mirai-console/src/MiraiConsole.kt
[`MiraiConsoleImplementation`]: ../backend/mirai-console/src/MiraiConsoleImplementation.kt
<!--[MiraiConsoleFrontEnd]: ../backend/mirai-console/src/MiraiConsoleFrontEnd.kt-->

[`Command`]: ../backend/mirai-console/src/command/Command.kt
[`Register`]: ../backend/mirai-console/src/command/CommandManager.kt#L77
[`AbstractCommand`]: ../backend/mirai-console/src/command/Command.kt#L90
[`CompositeCommand`]: ../backend/mirai-console/src/command/CompositeCommand.kt
[`SimpleCommand`]: ../backend/mirai-console/src/command/SimpleCommand.kt
[`RawCommand`]: ../backend/mirai-console/src/command/RawCommand.kt
[`CommandManager`]: ../backend/mirai-console/src/command/CommandManager.kt
[`CommandSender`]: ../backend/mirai-console/src/command/CommandSender.kt
[`CommandValueArgumentParser`]: ../backend/mirai-console/src/command/descriptor/CommandValueArgumentParser.kt
[`CommandArgumentContext`]: ../backend/mirai-console/src/command/descriptor/CommandArgumentContext.kt
[`CommandArgumentContext.BuiltIns`]: ../backend/mirai-console/src/command/descriptor/CommandArgumentContext.kt#L66

[`MessageScope`]: ../backend/mirai-console/src/util/MessageScope.kt

## [`Command`]

>「指令」：目前通常是 "/commandName arg1 arg2 arg3" 格式的消息。在将来可能会被扩展

```kotlin
interface Command {
    val names: Array<out String>
    val usage: String
    val description: String
    val permission: CommandPermission
    val prefixOptional: Boolean
    val owner: CommandOwner
    suspend fun CommandSender.onCommand(args: MessageChain)
}
```

每一条指令都被抽象成 [`Command`]。

### 执行指令

指令既可以在代码执行，也可以在消息环境中执行。

#### 在 [`CommandManager`] 执行指令

通过扩展：
- `suspend fun Command.execute(CommandSender, args: Message, checkPermission: Boolean=true): CommandExecutionResult`
- `suspend fun Command.execute(CommandSender, args: String, checkPermission: Boolean=true): CommandExecutionResult`
- `suspend fun CommandSender.executeCommand(message: Message, checkPermission: Boolean=true): CommandExecutionResult`
- `suspend fun CommandSender.executeCommand(message: String, checkPermission: Boolean=true): CommandExecutionResult`

### 指令语法解析
一条消息可以被解析为指令，如果它满足:

`<指令前缀><任一指令名> <指令参数列表>`

指令参数由空格分隔。参数类型可能为 `MessageContent` 类型，或 `String`（被包装为 `PlainText`)

指令前缀可能是可选的。可以在配置文件配置。（计划支持中）

### [`RawCommand`]
无参数解析, 接收原生参数的指令。
```kotlin
abstract override suspend fun CommandSender.onCommand(args: MessageChain)
```

例如 `/test 123 [图片]`，在处理时 `onCommand` 接收的 `args` 为包含 2 个元素的 `MessageChain`。第一个元素为 `PlainText("123")`，第二个元素为 `Image` 类型。

### [`Register`]
需要把指令注册到 `CommandManager` 以在 Mirai Console 生效
```kotlin
CommandManager.registerCommand(command)
```

## 参数智能解析
> 本节可能较难理解。但这不会影响你阅读下面的示例。

Mirai Console 为了简化处理指令时的解析过程，设计了参数智能解析。

### [`CommandValueArgumentParser`]
```kotlin
interface CommandArgumentParser<out T : Any> {
    fun parse(raw: String, sender: CommandSender): T
    fun parse(raw: MessageContent, sender: CommandSender): T = parse(raw.content, sender)
}
```

用于解析一个参数到一个数据类型。

### [`CommandArgumentContext`]

是 `Class` 到 [`CommandValueArgumentParser`] 的映射。作用是为某一个类型分配解析器。

#### [内建 `CommandArgumentContext`][`CommandArgumentContext.BuiltIns`]
支持原生数据类型，`Contact` 及其子类，`Bot`。

#### 构建 [`CommandArgumentContext`]
查看源码内注释：[CommandArgumentContext.kt: Line 146](../backend/mirai-console/src/command/descriptor/CommandArgumentContext.kt#L146-L183)

### 支持参数解析的 [`Command`] 实现
Mirai Console 内建 [`SimpleCommand`] 与 [`CompositeCommand`] 拥有 [`CommandArgumentContext`]，在处理参数时会首先解析参数再传递给插件的实现。

### [`SimpleCommand`]
简单指令。

此时示例一定比理论有意义。

Kotlin 示例：
```kotlin
object MySimpleCommand : SimpleCommand(
    MyPluginMain, "tell", "私聊",
    description = "Tell somebody privately"
) {
    @Handler // 标记这是指令处理器  // 函数名随意 
    suspend fun CommandSender.handle(target: User, message: String) { // 这两个参数会被作为指令参数要求
        target.sendMessage(message)
    }
}
```

Java 示例：
```java
public class MySimpleCommand extends SimpleCommand {

    public MySimpleCommand(JvmPlugin plugin) {
        super(plugin, "tell", new String[]{"私聊"}, "Tell somebody privately", plugin.getParentPermission(), CommandArgumentContext.EMPTY);
    }

    @Handler // 标记这是指令处理器，方法名随意
    public void handle(CommandSender sender, User target, String message) { // 后两个参数会被作为指令参数要求
        target.sendMessage(message);
    }

}
```

指令 `/tell 123456 Hello` 的解析流程:
1. 被分割为 `/`, `"tell"`, `"123456"`, `"Hello"`
2. `MySimpleCommand` 被匹配到，根据 `/` 和 `"test"`。`"123456"`, `"Hello"` 被作为指令的原生参数。
3. 由于 `MySimpleCommand` 定义的 `handle` 需要两个参数, `User` 和 `String`，`"123456"` 需要转换成 `User`，`"Hello"` 需要转换成 `String`。
4. Console 在 [内建 `CommandArgumentContext`][`CommandArgumentContext.BuiltIns`] 寻找适合于 `User` 的 [`CommandValueArgumentParser`]
5. `"123456"` 被传入这个 [`CommandValueArgumentParser`]，得到 `User`
6. `"Hello"` 也会按照 4~5 的步骤转换为 `String` 类型的参数
7. 解析完成的参数被传入 `handle`

### [`CompositeCommand`]

[`CompositeCommand`] 的参数解析与 [`SimpleCommand`] 一样，只是多了「子指令」概念。

示例：  

Kotlin 示例：
```kotlin
@OptIn(ConsoleExperimentalAPI::class)
object MyCompositeCommand : CompositeCommand(
    MyPluginMain, "manage", // "manage" 是主指令名
    description = "示例指令", permission = MyCustomPermission,
    // prefixOptional = true // 还有更多参数可填, 此处忽略
) {

    // [参数智能解析]
    //
    // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
    // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
    // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
    // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
    // 时调用这个函数
    @SubCommand // 表示这是一个子指令，使用函数名作为子指令名称
    suspend fun CommandSender.mute(target: Member, duration: Int) { // 通过 /manage mute <target> <duration> 调用
        sendMessage("/manage mute 被调用了, 参数为: $target, $duration")

        val result = kotlin.runCatching {
            target.mute(duration).toString()
        }.getOrElse {
            it.stackTraceToString()
        } // 失败时返回堆栈信息

        sendMessage("结果: $result")
    }
    
    @SubCommand
    suspend fun ConsoleCommandSender.foo() {
        // 使用 ConsoleCommandSender 作为接收者，表示指令只能由控制台执行。
        // 当用户尝试在聊天环境执行时将会收到错误提示。
    }

    @SubCommand("list", "查看列表") // 可以设置多个子指令名。此时函数名会被忽略。
    suspend fun CommandSender.ignoredFunctionName() { // 执行 "/manage list" 时调用这个函数
        sendMessage("/manage list 被调用了")
    }

    // 支持 Image 类型, 需在聊天中执行此指令.
    @SubCommand
    suspend fun UserCommandSender.test(image: Image) { // 执行 "/manage test <一张图片>" 时调用这个函数
        // 由于 Image 类型消息只可能在聊天环境，可以直接使用 UserCommandSender。
        
        sendMessage("/manage image 被调用了, 图片是 ${image.imageId}")
    }
}
```

Java 示例：
```java
public class MyCompositeCommand extends CompositeCommand {

    public MyCompositeCommand(JvmPlugin plugin) {
        // "manage" 是主指令名, 还有更多参数可填, 此处忽略
        super(plugin, "manage", new String[]{}, "示例指令", plugin.getParentPermission(), CommandArgumentContext.EMPTY);
    }

    // [参数智能解析]
    //
    // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
    // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
    // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
    // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
    // 时调用这个函数
    @SubCommand // 表示这是一个子指令，使用函数名作为子指令名称
    public void mute(CommandSender sender, Member target, int duration) { // 通过 /manage mute <target> <duration> 调用
        sender.sendMessage("/manage mute 被调用了, 参数为: " + target.toString() + ", " + duration);

        String result;
        try {
            target.mute(duration);
            result = "成功";
        } catch (Exception e) {
            result = "失败，" + e.getMessage();
        }

        sender.sendMessage("结果: " + result);
    }

    @SubCommand
    public void foo(ConsoleCommandSender sender) {
        // 使用 ConsoleCommandSender 作为接收者，表示指令只能由控制台执行。
        // 当用户尝试在聊天环境执行时将会收到错误提示。
    }

    @SubCommand(value = {"list", "查看列表"}) // 可以设置多个子指令名。此时函数名会被忽略。
    public void ignoredFunctionName(CommandSender sender) { // 执行 "/manage list" 时调用这个函数
        sender.sendMessage("/manage list 被调用了");
    }

    // 支持 Image 类型, 需在聊天中执行此指令.
    @SubCommand
    public void test(CommandSender sender, Image image) { // 执行 "/manage test <一张图片>" 时调用这个函数
        // 由于 Image 类型消息只可能在聊天环境，可以直接使用 UserCommandSender。

        sender.sendMessage("/manage image 被调用了, 图片是 " + image.getImageId());
    }
}
```

### 文本参数的转义

不同的参数默认用空格分隔。有时用户希望在文字参数中包含空格本身，参数解析器可以接受三种表示方法。

以上文中定义的 `MySimpleCommand` 为例：

#### 英文双引号

表示将其中内容作为一个参数，可以包括空格。

例如：用户输入 `/tell 123456 "Hello world!"` ，`message` 会收到 `Hello world!`。

注意：双引号仅在参数的首尾部生效。例如，用户输入 `/tell 123456 He"llo world!"`，`message` 只会得到 `He"llo`。

#### 转义符

即英文反斜杠 `\`。表示忽略之后一个字符的特殊含义，仅看作字符本身。

例如：

- 用户输入 `/tell 123456 Hello\ world!`，`message` 得到 `Hello world!`；
- 用户输入 `/tell 123456 \"Hello world!\"`，`message` 得到 `"Hello`。

#### 暂停解析标志

即连续两个英文短横线 `--`。表示从此处开始，到**这段文字内容**结束为止，都作为一个完整参数。

例如：

- 用户输入 `/tell 123456 -- Hello:::test\12""3`，`message` 得到 `Hello:::test\12""3`（`:` 表示空格）；
- 用户输入 `/tell 123456 -- Hello @全体成员 test1 test2`，那么暂停解析的作用范围到 `@` 为止，之后的 `test1` 和 `test2` 是不同的参数。
- 用户输入 `/tell 123456 \-- Hello` 或 `/tell 123456 "--" Hello`，这不是暂停解析标志，`message` 得到 `--` 本身。

注意：

`--` 的前后都应与其他参数有间隔，否则不认为这是暂停解析标志。

例如，用户输入 `/tell 123456--Hello world!`，`123456--Hello` 会被试图转换为 `User` 并出错。即使转换成功，`message` 也只会得到 `world!`。

### 非文本参数的转义

有时可能需要只用一个参数来接受各种消息内容，例如用户可以在 `/tell 123456` 后接图片、表情等，它们都是 `message` 的一部分。

对于这种定义方式，Mirai Console 的支持尚待实现，目前可以使用 [`RawCommand`] 替代。  

### 选择 [`RawCommand`], [`SimpleCommand`] 或 [`CompositeCommand`]

若需要不限长度的，自由的参数列表，使用 [`RawCommand`]。

若需要子指令，使用 [`CompositeCommand`]。否则使用 [`SimpleCommand`]。

## [`CommandManager`]
上面已经提到可以在 [`CommandManager`] 执行指令。[`CommandManager`] 持有已经注册的指令列表，源码内有详细注释，此处不过多赘述。

## [`CommandSender`]
指令发送者。

### 必要性

指令可能在聊天环境执行，也可能在控制台执行。因此需要一个通用的接口表示这样的执行者。

### 类型
```text
                CoroutineScope
                       ↑
                       |
                 CommandSender <---------+---------------+-------------------------------+
                       ↑                 |               |                               |
                       |                 |               |                               |
                       |     UserCommandSender   GroupAwareCommandSender     CommandSenderOnMessage
                       |                 ↑               ↑                               ↑
                       |                 |               |                               |
              AbstractCommandSender      |               |                               |
                       ↑                 |               |                               |
                       | sealed          |               |                               |
         +-------------+-------------+   |               |                               |
         |                           |   |               |                               |
         |                           |   |               |                               |      }
ConsoleCommandSender    AbstractUserCommandSender        |                               |      } 一级子类
                                     ↑                   |                               |      }
                                     | sealed            |                               |
                                     |                   |                               |
              +----------------------+                   |                               |
              |                      |                   |                               |
              |                      +------+------------+---------------+               |
              |                             |                            |               |
              |                             |                            |               |      }
      FriendCommandSender          MemberCommandSender           TempCommandSender       |      } 二级子类
              ↑                             ↑                            ↑               |      }
              |                             |                            |               |
              |                             |                            |               |      }
 FriendCommandSenderOnMessage  MemberCommandSenderOnMessage  TempCommandSenderOnMessage  |      } 三级子类
              |                             |                            |               |      }
              |                             |                            |               |
              +-----------------------------+----------------------------+---------------+
 ```

有关类型的详细信息，请查看 [CommandSender.kt](../backend/mirai-console/src/command/CommandSender.kt#L48-L135)

### 获取

`Contact.asCommandSender()` 或 `MessageEvent.toCommandSender()`，或 `ConsoleCommandSender`

## [`MessageScope`]

表示几个消息对象的’域‘，即消息对象的集合。用于最小化将同一条消息发送给多个类型不同的目标的付出。

参考 [MessageScope](../backend/mirai-console/src/util/MessageScope.kt#L28-L99)


----

> 下一步，[PluginData](PluginData.md#mirai-console-backend---plugindata)
>
> 返回 [开发文档索引](README.md#mirai-console)


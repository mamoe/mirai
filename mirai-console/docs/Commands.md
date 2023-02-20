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


"指令" 目前通常是 "/commandName arg1 arg2 arg3" 格式的消息。在将来可能会被扩展。

指令拥有一个主要名称和任意个次要名称。使用任意名称都可以执行指令。

定义指令
------

每个指令都是一个 `Command` 类型的对象。`Command` 是一个接口，定义了基本的指令的属性：

```kotlin
interface Command {
    val names: Array<out String> // 名称
    val usage: String // 用法
    val description: String // 描述
    val permission: Permission // 权限
    val prefixOptional: Boolean // 前缀可选
    val owner: CommandOwner // 拥有者

    val overloads: List<CommandSignature> // 指令的签名列表
}
```

`AbstractCommand` 提供了对 `Command` 的基础实现。而要在插件定义指令，建议继承如下三种指令实现：

（注意：所有指令都需要注册到指令管理器才能生效，详见 [注册指令](#注册指令)）

### 原生指令

原生指令即 [`RawCommand`]，它直接处理触发指令的原消息链。

`RawCommand` 提供了两个抽象函数，在指令被执行时将会调用它们：

*Kotlin*

```kotlin
open override suspend fun CommandContext.onCommand(args: MessageChain)
open override suspend fun CommandSender.onCommand(args: MessageChain)
```

*Java*

```java
public abstract class JRawCommand {
    // ...
    public void onCommand(CommandContext context, MessageChain args) {
    }

    public void onCommand(CommandSender sender, MessageChain args) {
    }
}
```

例如在聊天环境通过消息链 `/test 123 [图片]` 触发指令（`[图片]` 表示一个图片），`onCommand` 接收的 `args`
为包含 2
个元素的 `MessageChain`。第一个元素为 `PlainText("123")`，第二个元素为 `Image`。

注意，当 `onCommand(CommandSender, MessageChain)`
和 `onCommand(CommandContext, MessageChain)` 被同时覆盖时,
只有 `onCommand(CommandContext, MessageChain)` 会生效。

`CommandContext` 是当前指令的执行环境，定义如下：

```kotlin
interface CommandContext {
    val sender: CommandSender
    val originalMessage: MessageChain
}
```

其中 `sender` 为指令执行者。它可能是控制台（`ConsoleCmomandSender`
），也可能是用户（`UserCommandSender`）等；
`originalMessage` 为触发指令的原消息链，包含元数据，也包含指令名。

若在聊天环境触发指令，`originalMessage` 将会包含 `MessageSource`。

注意，`MessageSource` 等 `MessageMetadata` 的位置是不确定的。取决于 mirai-core
的版本，它可能会存在于消息链中的任意位置。因此请不要依赖于 `originalMessage` 的元素顺序。

`args` 参数的顺序是稳定的，因为它只包含消息内容（`MessageContent`）。

#### 使用 `RawCommand`

只需要按需继承 `onCommand` 其中一个即可。如果需要使用原消息链，则继承 `CommandContext`
的，否则继承 `CommandSender` 的可以使实现更简单。

通常可以以单例形式实现指令，当然非单例模式也是支持的。

下面分别为在 Kotlin 和 Java 的示例实现：

*Kotlin*

```kotlin
object MyCommand : RawCommand(
    MyPluginMain, "name", // 使用插件主类对象作为指令拥有者；设置主指令名为 "name"
    // 可选：
    "name2", "name3", // 增加两个次要名称
    usage = "/name arg1 arg2", // 设置用法，将会在 /help 展示
    description = "这是一个测试指令", // 设置描述，将会在 /help 展示
    prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
) {
    override suspend fun CommandContext.onCommand(args: MessageChain) {
    }
}
```

*Java*

```java
public final class MyCommand extends JRawCommand {
    public static final MyCommand INSTANCE = new MyCommand();

    private MyCommand() {
        super(MyPluginMain.INSTANCE, "test"); // 使用插件主类对象作为指令拥有者；设置主指令名为 "test"
        // 可选设置如下属性
        setUsage("/test"); // 设置用法，这将会在 /help 中展示
        setDescription("这是一个测试指令"); // 设置描述，也会在 /help 中展示
        setPrefixOptional(true); // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull MessageChain args) {
        // 处理指令
    }
}
```

### 参数智能解析

Console
提供参数智能解析功能，可以阅读用户手册的 [指令参数智能解析](../../docs/ConsoleTerminal.md#指令参数智能解析)
了解这一功能。

有两种指令实现支持这个功能，它们分别是简单指令 `SimpleCommand` 和复合指令 `CompositeCommand`。

### 复合指令

复合指令即 `CompositeCommand`，支持参数智能解析。

Console 通过反射实现参数类型识别。标注 `@SubCommand` 的函数（方法）都会被看作是子指令。

一个简单的子指令定义如下：

*Kotlin*

```kotlin
object MyComposite : CompositeCommand() {
    // ...

    @SubCommand("name")
    suspend fun foo(context: CommandContext, arg: String) {
        println(arg)
    }
}
```

*Java*

```java
public final class MyComposite extends JCompositeCommand {
    // ...
    @SubCommand("name")
    public void foo(CommandContext context, String arg) {
        System.out.println(arg);
    }
}
```

Java 使用者请了解 Kotlin 的 `fun foo(context: CommandContext, arg: String)` 相当于
Java 的 `public void foo(CommandContext context, String arg)`。下面部分简单示例将只用
Kotlin 展示。

#### 子指令

用 `@SubCommand` 标注的函数就是子指令。子指令将隶属于其主指令。定义于主指令 `main` 的名称为 `child`
的子指令在执行时需要使用 `/main child`，其中 `/` 表示指令前缀（如果需要）。`/main child arg1 arg2`
中的 `arg1` 和 `arg2` 则分别表示传递给子指令的第一个和第二个参数。

子指令可以拥有多个名称，即 `@SubCommand("child1", "child2")` 可以由 `/main child1`
或 `/main child2` 执行。

#### 子指令名称

`@SubCommand` 的参数为子指令的名称，可以有多个名称，即 `@SubCommand("name1", "name2")`
。若子指令名称与函数名称相同，可以省略 `@SubCommand`
的参数。例如 `@SubCommand("foo") suspend fun foo()`
可以简写为 `@SubCommand suspend fun foo()`。

#### 子指令参数

子指令的第一个参数（在 Kotlin 也可以是接收者（`receiver`））可以是 `CommandContext`
或 `CommandSender`，分别用来获取指令执行环境或发送人。与 `RawCommand`
相同，如果需要使用原消息链，则使用 `CommandContext`，否则使用 `CommandSender` 的可以让实现更简单。

在这个参数以外的就是是子指令的值参数。
值参数将会对应消息链。例如定义于名称为 `comp` 的 `CompositeCommand` 中的子指令 `@SubCommand fun foo(context: CommandContext, arg1: String, arg2: Int)`
在由 `/comp foo str 1` 执行时，`str` 将会传递给 `arg1`；`1` 将会传递给 `arg2`。将会在下文详细解释此内容。

在 Kotlin，子指令既可以是 `suspend` 也可以不是。子指令在不是挂起函数时可以使用阻塞 IO（如 `File.readText`），因为子指令会在 IO 线程执行。

#### 定义参数

子指令函数（方法）定义的参数将按顺序成为指令的参数。如下示例中 `arg1` 将成为第一个参数，`arg2` 为第二个：

*Kotlin*

```kotlin
object MyComposite : CompositeCommand(MyPluginMain, "main") {
    // ...

    @SubCommand("name")
    suspend fun foo(context: CommandContext, arg: String, b: Boolean) {
        println(arg)
    }
}
```

*Java*

```java
public final class MyComposite extends JCompositeCommand {
    public MyComposite() {
        super(MyPluginMain.INSTANCE, "main");
        // ...
    }

    // ...
    @SubCommand("name")
    public void foo(CommandContext context, String arg, boolean b) {
        System.out.println(arg);
    }
}
```

在执行时，`/main name 1 true` 中 `1` 将会被解析为 `String` 类型的参数 `arg`、`true`
将会被解析为 `boolean` 参数的 `b`。

#### 内置智能解析

可参考 `CommandValueArgumentParser`，Console 内置支持以下类型的参数：

- `Message`
- `SingleMessage`
- `MessageContent`
- 原生数据类型
- `PlainText`
- `Image`
- `String`
- `Bot`
- `Contact`
- `User`
- `Friend`
- `Member`
- `Group`
- `PermissionId`
- `PermitteeId`
- `Enum`
- `TemporalAccessor`

#### 自定义智能解析

可在 `CmopositeCommand` 继承 `context` 属性增加自定义解析器。下面示例中为 `Boolean`
指定了自定义的解析器，子指令的 `b` 参数将会用此解析器解析。

*Kotlin*

```kotlin
object CustomBooleanParser : CommandValueArgumentParser<Boolean> {
    override fun parse(raw: String, sender: CommandSender): Boolean {
        return raw == "TRUE!"
    }
    override fun parse(
        raw: MessageContent,
        sender: CommandSender
    ): Boolean {
        // 将一个图片认为是 'true'
        if (raw is Image && raw.imageId == "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
            return true
        }
        return super.parse(raw, sender)
    }
}

object MyComposite : CompositeCommand(
    MyPluginMain, "main",
    overrideContext = buildCommandArgumentContext {
        Boolean::class with CustomBooleanParser
    }
) {
    // ...
    @SubCommand("name")
    suspend fun foo(context: CommandContext, arg: String, b: Boolean) {
        println(b)
    }
}
```

*Java*

```java
// CustomBooleanParser.java
public final class CustomBooleanParser implements CommandValueArgumentParser<Boolean> {
    @NotNull
    @Override
    public Boolean parse(@NotNull String raw, @NotNull CommandSender sender) throws CommandArgumentParserException {
        return raw.equals("TRUE!");
    }

    @NotNull
    @Override
    public Boolean parse(@NotNull MessageContent raw, @NotNull CommandSender sender) throws CommandArgumentParserException {
        // 将一个图片认为是 'true'
        if (raw instanceof Image && ((Image) raw).getImageId().equals("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg")) {
            return true;
        }
        return CommandValueArgumentParser.super.parse(raw, sender);
    }
}

// MyComposite.java
public final class MyComposite extends JCompositeCommand {
    public MyComposite() {
        super(MyPluginMain.INSTANCE, "main");
        // ...

        addArgumentContext(new CommandArgumentContextBuilder()
                .add(Boolean.TYPE, new CustomBooleanParser()) // 注册解析器
                .build());
    }

    // ...
    @SubCommand("name")
    public void foo(CommandContext context, String arg, boolean b) {
        System.out.println(b);
    }
}
```

在 `parse` 时抛出 `CommandArgumentParserException`
会被看作是正常退出，异常的内容会返回给指令调用人。在 `parse` 时抛出其他异常则会认为是插件错误。

### 简单指令

简单指令与复合指令拥有一样的智能参数解析功能。简单指令没有子指令，使用 `@Handler` 标注一个函数可以让它处理指令：

*Kotlin*

```kotlin
object MySimple : SimpleCommand(MyPluginMain, "main") {
    // ...
    @Handler
    suspend fun foo(context: CommandContext, arg: String, b: Boolean) {
        println(b)
    }
}
```

*Java*

```java
// MySimple.java
public final class MySimple extends JSimpleCommand {
    public MySimple() {
        super(MyPluginMain.INSTANCE, "main");
        // ...
    }

    // ...
    @Handler
    public void foo(CommandContext context, String arg, boolean b) {
        System.out.println(b);
    }
}
```

在执行时，`/main aaaa false` 将会调用 `foo` 函数（方法）。`aaaa` 匹配 `String` 类型的参数 `arg`
，`false` 匹配 `boolean` 类型的参数 `b`。

简单指令也可以使用自定义参数解析器，用法与复合指令一样。

*Kotlin*

```kotlin
object MySimple : SimpleCommand(
    MyPluginMain, "main",
    overrideContext = buildCommandArgumentContext {
        Boolean::class with CustomBooleanParser
    }
) {
    // ...
    @Handler
    suspend fun foo(context: CommandContext, arg: String, b: Boolean) {
        println(b)
    }
}
```

*Java*

```java
// MySimple.java
public final class MySimple extends JSimpleCommand {
    public MySimple() {
        super(MyPluginMain.INSTANCE, "main");
        // ...

        addArgumentContext(new CommandArgumentContextBuilder()
                .add(Boolean.TYPE, new CustomBooleanParser()) // 注册解析器
                .build());
    }

    // ...
    @Handler
    public void foo(CommandContext context, String arg, boolean b) {
        System.out.println(b);
    }
}
```

### 选择 [`RawCommand`], [`SimpleCommand`] 或 [`CompositeCommand`]

若需要不限长度的，自由的参数列表，使用 [`RawCommand`]。

若需要子指令，使用 [`CompositeCommand`]。否则使用 [`SimpleCommand`]。

### 自行实现指令

Console 允许插件自行实现指令（不使用上述 `RawCommand`、`SimpleCommand`
和 `CompositeCommand`）。但注意，在实现时难免会需要使用到抽象指令描述器（如 `CommandArgument`
），而这些描述器是不稳定的。因此插件自行实现指令可能会导致不兼容未来的 Console 版本。


注册指令
-------

所有指令都需要注册到指令管理器才能生效。要注册指令，在 `onEnable`
使用 `CommandManager.registerCommand(command)`。

### 查看已注册的所有指令

使用 `PluginManager.INSTANCE.getAllRegisteredCommands()`
。可以获得当前已经注册的所有 `Command` 实例列表。


执行指令
-------

指令既可以由插件执行，也可以在消息环境中由用户执行（需要 [chat-command](https://github.com/project-mirai/chat-command)
）。

### 在插件执行指令

若要通过字符串解析目标指令并执行，使用 `CommandManager.INSTANCE.executeCommand(CommandSender, Message)`
，其中 `Message` 为包含前缀（如果有必要）、指令名称、以及指令参数列表的完整消息。

若要通过字符串解析目标指令并执行，使用 `CommandManager.INSTANCE.executeCommand(CommandSender, Command, Message)`
，其中 `Message` 传递给指令的参数列表，不包含前缀或指令名称。注意，若要执行复合指令，需要包含子指令名称。

### 指令语法解析

一条消息可以被解析为指令，如果它满足：

```text
<指令前缀><任一指令名> <指令参数列表>
```

指令参数列表由空格分隔。

### 指令解析流程

> 注意：该流程可能会变化，请不要依赖这个流程。

对于

```text
@Handler suspend fun handle(context: CommandContext, target: User, message: String) 
```

指令 `/tell 123456 Hello` 的解析流程:

1. 被分割为 `/`, `"tell"`, `"123456"`, `"Hello"`
2. 根据 `/` 和 `"test"`，确定 `MySimpleCommand` 作为目标指令。`"123456"`, `"Hello"`
   作为指令的原生参数。
3. 由于 `MySimpleCommand` 定义的 `handle` 需要两个参数, 即 `User` 和 `String`
   ，`"123456"` 需要转换成 `User`，`"Hello"` 需要转换成 `String`。
4. 指令寻找合适的解析器（`CommandValueArgumentParser`）
5. `"123456"` 通过 `ExistingUserValueArgumentParser` 变为 `User` 类型的参数
6. `"Hello"` 通过 `StringValueArgumentParser` 变为 `String` 类型的参数
7. 解析完成的参数传入 `handle`

文本参数转义
-----

不同的参数默认用空格分隔。有时用户希望在文字参数中包含空格本身，参数解析器可以接受三种表示方法。

以上文中定义的 `MySimpleCommand` 为例：

### 英文双引号

表示将其中内容作为一个参数，可以包括空格。

例如：用户输入 `/tell 123456 "Hello world!"` ，`message` 会收到 `Hello world!`。

注意：双引号仅在参数的首尾部生效。例如，用户输入 `/tell 123456 He"llo world!"`，`message`
只会得到 `He"llo`。

### 转义符

即英文反斜杠 `\`。表示忽略之后一个字符的特殊含义，仅看作字符本身。

例如：

- 用户输入 `/tell 123456 Hello\ world!`，`message` 得到 `Hello world!`；
- 用户输入 `/tell 123456 \"Hello world!\"`，`message` 得到 `"Hello`。

### 暂停解析标志

即连续两个英文短横线 `--`。表示从此处开始，到**这段文字内容**结束为止，都作为一个完整参数。

例如：

- 用户输入 `/tell 123456 -- Hello:::test\12""3`，`message`
  得到 `Hello:::test\12""3`（`:` 表示空格）；
- 用户输入 `/tell 123456 -- Hello @全体成员 test1 test2`，那么暂停解析的作用范围到 `@`
  为止，之后的 `test1` 和 `test2` 是不同的参数。
- 用户输入 `/tell 123456 \-- Hello` 或 `/tell 123456 "--" Hello`
  ，这不是暂停解析标志，`message` 得到 `--` 本身。

注意：

`--` 的前后都应与其他参数有间隔，否则不认为这是暂停解析标志。

例如，用户输入 `/tell 123456--Hello world!`，`123456--Hello` 会被试图转换为 `User`
并出错。即使转换成功，`message` 也只会得到 `world!`。

### 非文本参数的转义

有时可能需要只用一个参数来接受各种消息内容，例如用户可以在 `/tell 123456` 后接图片、表情等，它们都是 `message`
的一部分。

对于这种定义方式，Mirai Console 的支持尚待实现，目前可以使用 [`RawCommand`] 替代。

## 指令发送者

指令可能在聊天环境执行，也可能在控制台执行。
指令发送者即 `CommandSender`，是执行指令时的必须品之一。

### 类型

```text
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

### 获取控制台指令发送者

`ConsoleCommandSender` 表示以控制台身份执行指令。它是一个单例对象，在 Kotlin 可以直接通过类型获得类名获得实例，在
Java 可通过 `ConsoleCommandSender.INSTANCE` 获得。

### 获取其他指令发送者

在 Kotlin 可使用扩展函数：`Contact.asCommandSender()`
或 `MessageEvent.toCommandSender()`
。

在 Java 可使用 `CommandSender.from` 和 `CommandSender.of`。



[`MessageScope`]
-----

表示几个消息对象的'域'，即消息对象的集合。用于最小化将同一条消息发送给多个类型不同的目标的付出。示例：

*Kotlin*

```kotlin
// 在一个 CompositeCommand 内
@Handler
suspend fun CommandSender.handle(target: Member) {
    val duration = Random.nextInt(1, 15)
    target.mute(duration)


    // 不使用 MessageScope, 无用的样板代码
    val thisGroup = this.getGroupOrNull()
    val message = "${this.name} 禁言 ${target.nameCardOrNick} $duration 秒"
    if (target.group != thisGroup) {
        target.group.sendMessage(message)
    }
    sendMessage(message)


    // 使用 MessageScope, 清晰逻辑
    // 表示至少发送给 `this`, 当 `this` 的真实发信对象与 `target.group` 不同时, 还额外发送给 `target.group`
    this.scopeWith(target.group) {
        sendMessage("${name} 禁言了 ${target.nameCardOrNick} $duration 秒")
    }


    // 同样地, 可以扩展用法, 同时私聊指令执行者:
    // this.scopeWith(
    //    target,
    //    target.group
    // ) { ... }
}
```

*Java*

```java
public class MyCommand extends SimpleCommand {
    @Handler
    public void handle(sender: CommandSender, target: Member) {
        int duration = Random.nextInt(1, 15);
        target.mute(duration);


        // 不使用 MessageScope
        Group thisGroup = CommandSenderKt.getGroupOrNull(sender);
        String message = "${this.name} 禁言 ${target.nameCardOrNick} $duration 秒";
        if (!target.group.equals(thisGroup)) {
            target.group.sendMessage(message);
        }
        sender.sendMessage(message);


        // 使用 MessageScope
        // 表示至少发送给 `this`, 当 `this` 的真实发信对象与 `target.group` 不同时, 还额外发送给 `target.group`
        MessageScope scope = MessageScopeKt.scopeWith(sender, target);
        scope.sendMessage("${name} 禁言了 ${target.nameCardOrNick} $duration 秒");

        // 或是只用一行：
        MessageScopeKt.scopeWith(sender, target).sendMessage("${name} 禁言了 ${target.nameCardOrNick} $duration 秒");
    }
}
```

----

> 下一步，[PluginData](PluginData.md#mirai-console-backend---plugindata)
>
> 返回 [开发文档索引](README.md#mirai-console)


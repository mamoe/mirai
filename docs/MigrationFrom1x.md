# Mirai - Migration From 1.x

本文介绍如何从 1.x 升级到 2.x。

根据你的语言选择：[Kotlin](#使用-kotlin) | [Java](#使用-java)

## 使用 Kotlin

### 如何自动完成 Kotlin 的错误替换

仅 IntelliJ IDEA 和 Android Studio 支持这个功能。

把光标放在一个错误中间（或者按 F2 自动跳转到错误），在 Windows 使用 `Alt + Enter` 快捷键，macOS 使用 `Option + Enter`，会得到弹窗如下图。

![YBP47V_Z640J_YU5WZ_JVPW.png](https://i.loli.net/2020/12/18/CiX9qApu5BnVPch.png)

第一项为仅替换这一个错误，第二项为替换项目中的所有这个错误。一般推荐选择第二项并回车即可。

**Mirai 的修改都尽可能地提供了这样的替换，请依次按如下步骤更新以下几个版本，才能使用这些替换。**  
部分无法提供自动替换的修改会在下文说明。

### `1.x` -> `2.0-M1-1`

替换依赖（可以直接全局搜索替换）:
- `net.mamoe:mirai-core` -> `net.mamoe:mirai-core-api`
- `net.mamoe:mirai-core-qqandroid` -> `net.mamoe:mirai-core`

**Kotlin**:

1. 将 `MessageChain[Image]` 等 IDE 会提示错误的调用调整为:
   - `.findIsInstance<Image>()` （`Image` 不存在时返回 `null`）
   - `.firstIsInstance<Image>()`  （`Image` 不存在时抛出异常）

   **提示**: 如果你是想获取消息的内容，可以使用 `messageChain.content` 扩展，而不需要使用 `MessageChain[PlainText]`

2. `Bot.getFriend` 等函数以前在找不到好友时会抛出异常，现在它们会返回 `null`。  
   请替换 `Bot.getFriend` 为 `Bot.getFriendOrFail`。


只要能通过编译就可以适配下一个版本。

### `2.0-M1` -> `2.0-M2`

修改都可以自动替换完成。**但请不要跳过 `2.0-M2` 这一步骤。**

### `2.0-M2` -> `2.0-RC`

1. 戳一戳事件由以前的多个事件变为了统一的单个 `NudgeEvent`。若有使用请直接参考 `NudgeEvent` 源码修改。

2. `Listener.ConcurrencyKind` 和 `Listener.EventPriority` 由嵌套类移动到顶层，请执行全局替换：
   - `Listener.ConcurrencyKind` -> `ConcurrencyKind`
   - `Listener.EventPriority` -> `EventPriority`

3. `IMirai` 低级 API 函数名现在不再带有 `_lowLevel` 前缀, 直接删除前缀即可。


其他修改都可以自动替换完成。


### `2.0-RC` -> `2.0.0`

直接把版本号更改为 `2.0.0`。

至此你已经成功升级到了 mirai 2.0。[回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

## 使用 Java

**请依次按如下步骤更新以下几个版本**

### `1.x` -> `2.0-M1`

- 消息事件包名有调整, 请根据 IDE 提示自动导入引用失效的包.
- Bot 构造方法调整, 将原 `BotFactoryKt.newBot(...)` 替换为 `BotFactory.INSTANCE.newBot(...)`
- 如果有调用 `Utils.getDefaultLogger().invoke(...)`，替换为 `MiraiLogger.create(...)`
- `Bot.getFriend` 等方法以前在找不到好友时会抛出异常，现在它们会返回 `null`。  
  请替换为 `Bot.getFriendOrFail`。

### `2.0-M1` -> `2.0-M2`

图片和语音上传的 API 有更改。

新增了资源 API，可以统一缓存文件。
```
ExternalResource.create(file);
ExternalResource.create(inputStream);
```

上传一个资源为图片或语音：
```
contact.uploadImage(resource);
contact.uploadVoice(resource);
```

或者使用工具方法直接发送一个 `File` 或 `InputStream` 为图片：
```
Contact.sendImage(contact, inputStream); // 返回 MessageReceipt
Contact.sendImage(contact, file);        // 返回 MessageReceipt

Contact.uploadImage(contact, inputStream); // 返回 Image 消息
Contact.uploadImage(contact, file);        // 返回 Image 消息
```

### `2.0-M2` -> `2.0-RC`

1. 戳一戳事件由以前的多个事件变为了统一的单个 `NudgeEvent`。若有使用请直接参考 `NudgeEvent` 源码修改。

2. `Listener.ConcurrencyKind` 和 `Listener.EventPriority` 由嵌套类移动到顶层，请执行全局替换：
   - `Listener.ConcurrencyKind` -> `ConcurrencyKind`
   - `Listener.EventPriority` -> `EventPriority`

3. `IMirai` 低级 API 方法名现在不再带有 `_lowLevel` 前缀, 直接删除前缀即可。


### `2.0-RC` -> `2.0.0`

直接把版本号更改为 `2.0.0`。

至此你已经成功升级到了 mirai 2.0。[回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)

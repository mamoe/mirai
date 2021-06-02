# mirai-core-api

mirai 核心 API 模块。本文档帮助读者了解该模块的主要架构。

> mirai 为多平台设计。支持 Android 和 JVM 双平台，拥有多个源集。
>
> - `commonMain`：平台无关的通用代码。绝大部分代码都存在与这个源集。
> - `jvmMain`：桌面 JVM 平台的特别实现。
> - `androidMain`：Android 平台的特别实现。
>
> 阅读源码通常阅读 `src/commonMain`

## 架构

| 包名               | 描述                                                |
|:------------------|:----------------------------------------------------|
| `net.mamoe.mirai` | mirai 核心 API                                      |
| `.contact`        | 联系人类型。如群 `Group`，好友 `Friend`                 |
| `.event`          | 事件框架。提供事件对象的基类以及监听事件的方法               |
| `.event.events`   | 事件的定义。包含许多事件的具体类, 如消息事件 `MessageEvent` |
| `.message`        | 消息系统                                             |
| `.message.data`   | 提供对富文本聊天消息及其元素多样性的抽象                    |
| `.message.code`   | 提供一个易于阅读的消息字符串表示方式                       |
| `.message.action` | 提供与消息有关的动作的抽象，如戳一戳                       |
| `.utils`          | 一些工具类                                            |
| `.internal`       | 内部实现                                             |
| `.internal.event` | 事件框架的实现                                        |

## `net.mamoe.mirai`

### `IMirai`
[IMirai.kt](src/commonMain/kotlin/IMirai.kt#L33)

**API 模块与协议实现模块的对接接口。**

- 单例
- 通过 `ServiceLoader` 寻找[协议实现](../mirai-core/README.md)。
- 若 `ServiceLoader` 在特定环境下不可用，外部可在 Kotlin **在调用任何 Mirai API 之前**覆盖实例：
  ```kotlin
  @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // 必要
  net.mamoe.mirai._MiraiInstance.set(net.mamoe.mirai.internal.MiraiImpl())
  ```

### `Bot`

[BotFactory]: src/commonMain/kotlin/BotFactory.kt

[Bot.kt](src/commonMain/kotlin/IMirai.kt#L29)

表示一个机器人对象（账户）。

- 通过 [BotFactory] 构造
- 是功能的入口点----大部分操作都直接或间接经过 `Bot`
- 持有联系人（好友和群）对象列表
- 可获得事件通道

## `net.mamoe.mirai.contact`

联系人系统。[docs/Contacts](../docs/Contacts.md)

## `net.mamoe.mirai.event`

事件系统。[docs/Contacts](../docs/Contacts.md)

## `net.mamoe.mirai.event.events`

事件列表。[README](src/commonMain/kotlin/event/events/EventList.md#事件)

## `net.mamoe.mirai.message`

消息系统。

### `MessageReceipt`

[MessageReceipt.kt](src/commonMain/kotlin/message/MessageReceipt.kt#L25)

在发送消息（`Contact.sendMessage`）后收到的回执。

### `MessageSerializers`

[MessageSerializers.kt](src/commonMain/kotlin/message/MessageSerializers.kt#L27)

[kotlinx.serialization](https://github.com/kotlin/kotlinx.serialization) 序列化支持。

## `net.mamoe.mirai.message.data`

对富文本聊天消息及其元素多样性的抽象。

一个消息元素最基本的接口为 [Message](src/commonMain/kotlin/message/data/Message.kt#L30).

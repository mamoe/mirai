# Mirai - Concise API

> 注:
> - 本章节展示关于 `mirai-core-api` 比较常用的 API 示例
> - 请配合 `mirai-core-api` 源码查看
> - 本章仅提供 API 粗略介绍

----------------------

# Bots

## BotFactory

`BotFactory` 用于创建一个新的 `Bot`, 详情请看 [Bots.md](Bots.md)

```kotlin
val bot = BotFactory.newBot(/*....*/)

// Java
Bot bot = BotFactory.INSTANCE.newBot(/*....*/);
Bot bot = Mirai.getInstance().getBotFactory().newBot(/*....*/);
```

# Misc utils

## Logger

Mirai 全部的日志都通过 `MiraiLogger` 输出, 查看 `MiraiLogger` 源码注释获得更多信息

## ExternalResource

`ExternalResource` 代表一个外部文件, 可用于 文件上传, 图片发送, etc.

构造 `ExternalResource` 可以通过以下方法构造

```kotlin
File("foo.txt").toExternalResource()

ExternalResource.create(new File("foo.txt"))
```

内置支持的数据类型有 `java.io.File`, `java.io.RandomAccessFile`,
`byte[]`, `java.io.InputStream`

# Contact & Message

## Send Image

### Origin send image

最原始的发送图片的方法，就是先 `uploadImage` 然后 `sendMessage`

Kotlin 可以使用自动补全得到相关方法

> `contact.uploadImage // IDEA 补全`

Java 可以使用 `contact.uploadImage(ExternalResource)` 来得到一个图片对象
(~~这也是为啥 ExternalResource 在前面~~)
也可以使用在 Kotlin 定义的扩展方法

```java
Image i = Contact.uploadImage(/*....*/);
Image i = ExternalResource.uploadAsImage(/*...*/);
```

### sendImage

`sendImage` 相当于一次性把 `uploadImage` 和 `sendMessage` 做完了,
在效率上两者没有区别

Kotlin 可以使用自动补全得到相关方法

> `contact.sendImage // IDEA 补全`

由于 `sendImage` 是 Kotlin 定义的扩展方法, 所以 Java 只能使用下述方法调用
```java
Contact.sendImage(/**/);
ExternalResource.sendAsImage(/*...*/);
```

## Send Voice

发送语音与发送图片的区别不大，都是先 `upload` 然后 `send`

> - 在 2.7.0 之前，只有群聊 (`Group`) 支持语音, 2.7.0 之后支持私聊语音
> - 每次发送新语言都重新 `upload`, 避免复用 `Voice` 对象
> - 只支持 `amr` 和 `silk` 格式

要得到一个语音对象, 需要先 `uploadVoice`

Kotlin 可以使用自动补全得到相关方法

> `contact.uploadVoice // IDEA 补全`

Java 可以使用 `contact.uploadVoice(ExternalResource)` 来得到一个语音对象
(~~这也是为啥 ExternalResource 在前面~~)
也可以使用在 Kotlin 定义的扩展方法

```java
contact.sendMessage(ExternalResource.uploadAsVoice(/*...*/));
```

## Members

`Member` 对象分为 `NormalMember`(正常的群成员) 和 `AnonymousMember`(匿名)

对 `Member` 操作时需要具体操作时应该先判断是否为 `NormalMember` 然后强转

```kotlin
if (member is NormalMember) { // kotlin smart cast
}
if (member instanceof NormalMember) {
    NormalMember nMember = (NormalMember) member;
}
```

## Recall Message

撤回信息可以通过 `MessageChain` 或者 `MessageSource` 撤回

```kotlin
subscribeAlways<MessageEvent> {// this: MessageEvent
    this.message.recall()

    this.message.source.recall()
    
    // Java
    MessageSource.recall(event.getMessage());
    MessageSource.recall(event.getMessage().getOrFail(MessageSource.Key));
}
```

# Events

常用事件

| Name                              | Desc                   |
| :----------------                 | :------------          |
| MessageEvent                      | Bot 收到一条新消息        |
| NewFriendRequestEvent             | 你有一条新的好友申请        |
| MemberJoinEvent                   | 有新群成员加入群           |
| MemberLeaveEvent                  | 群成员离开群聊            |
| BotInvitedJoinGroupRequestEvent   | Bot 收到了一个加群邀请     |
| BotJoinGroupEvent                 | Bot 加入了一个群聊         |
| MemberJoinRequestEvent            | 新的入群申请              |

### MessageEvent

当直接监听 `MessageEvent` 时，应该排除 Bot 信息同步事件 `MessageSyncEvent`

```kotlin
subscribeAlways<MessageEvent> { // this: MessageEvent
    if (this is MessageSyncEvent) return@subscribeAlways
}
```

# IMirai

`IMirai` 对象是 mirai 底层访问接口(Low Level Api),
如果不是只能使用 LowLevelApi 请避免使用 `IMirai` 相关的方法

`IMirai` 相关的方法可能会在任何时候进行修改 

因错误使用 `IMirai` 方法导致的错乱请不要当成 bug, 不会受理因此导致的错误


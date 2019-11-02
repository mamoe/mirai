# Mirai
[![HitCount](http://hits.dwyl.io/him188/mamoe/mirai.svg)](http://hits.dwyl.io/him188/mamoe/mirai) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7d0ec3ea244b424f93a6f59038a9deeb)](https://www.codacy.com/manual/Him188/mirai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mamoe/mirai&amp;utm_campaign=Badge_Grade)

一个以 **TIM PC协议(非web)** 驱动的跨平台开源 QQ 机器人服务端核心, 目前仅支持 JVM  
Mirai 在 JVM 平台采用插件模式运行，同时提供独立的跨平台核心库.  
未来会在 Native(Win32) 平台提供目前比较流行的几种机器人软件的 API 转接  
  
若您有任何意见或建议, 欢迎提交 issue.  

部分协议来自网络上开源项目  

**一切开发旨在学习，请勿用于非法用途**

## Try

现在您可以开始体验低付出高效率的 Mirai

1. Clone
2. Import as Gradle project
3. Run demo main [Demo 1 Main](mirai-demos/mirai-demo-1/src/main/java/demo/subscribe/SubscribeSamples.kt)

**转到[开发文档](#Development-Guide---Kotlin)**

## Update log

- 发送好友/群消息(10/14)
- 接受解析好友消息(10/14)
- 接收解析群消息(10/14)
  - 成员昵称(10/18)
  - 成员权限(11/2)
- 好友在线状态改变(10/14)
- Android客户端上线/下线(10/18)
- 上传并发送好友/群图片(10/21, 10/26)
- 群员权限改变(11/2)
- 发起会话(11/2)
- 个人资料(11/2)

计划中: 添加好友

## Requirements

所有平台: 
- Kotlin 1.3.50

JVM 平台:
- Java 8

#### Libraries used
Mirai 使用以下开源库:
- [kotlin-stdlib](https://github.com/JetBrains/kotlin)
- [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [kotlinx-io](https://github.com/Kotlin/kotlinx-io)
- [kotlin-reflect](https://github.com/JetBrains/kotlin)
- [pcap4j](https://github.com/kaitoy/pcap4j)
- [atomicfu](https://github.com/Kotlin/kotlinx.atomicfu)
- [ktor](https://github.com/ktorio/ktor)
- [klock](https://github.com/korlibs/klock)
- [tornadofx](https://github.com/edvin/tornadofx)
- [javafx](https://github.com/openjdk/jfx)

## Development Guide - Kotlin

平台通用开发帮助(不含协议层).   

您需要有一定 Kotlin 基础才能读懂以下内容.  
若您对本文档有建议, 请告诉我们      

目录:  
- [Introduction](#Introduction) Mirai 介绍
- [Modules](#Modules) 模块介绍
  - [mirai-core](#mirai-core) 核心模块
  - [mirai-console](#mirai-console) JVM 控制台
  - [mirai-demo](#mirai-demo) 示例和演示程序
  - [mirai-debug](#mirai-debug) 抓包工具和分析工具\
- [Logger](#Logger) 日志系统
- [Bot](#Bot) 机器人类
- [Contact](#Contact) 联系人
- [Message](#Message) 消息
  - [MessageChain](#MessageChain) `MessageChain`
  - [Types](#Types) 消息类型
  - [Operators](#Operators) `Message` 一般用法
  - [Extensions](#Extensions) `Message` 的常用扩展方法
- [Image](#Image) 图片
  - [Image JVM](#Image-JVM) JVM 平台扩展实现
- [Event](#Event) 事件
  - [Subscription](#Subscription) 事件监听(订阅) 
  - [Message Event](#Message-Event) 针对消息事件的订阅实现

### Introduction 

Mirai 目前为快速流转（Moving fast）状态, 增量版本之间可能不具有兼容性，任何功能都可能在没有警告的情况下添加、删除或者更改。

### Modules
Mirai 的模块组成

#### mirai-core
Mirai 的核心部分.

- 独立的跨平台设计, 可以被以库的形式内置在任意项目内.
- 现有 JVM 支持
- 未来计划 Android, Native 支持 

#### mirai-console
- 仅 JVM 平台
- 仅命令行
- Jar 插件支持

#### mirai-demo
Samples and demos.
目前仅有 [SubscribeSamples](mirai-demos/mirai-demo-1/src/main/java/demo/subscribe/SubscribeSamples.kt)

#### mirai-debug
抓包工具和分析工具. 不会进行稳定性维护.  

- 抓包自动解密和分析
- Hex 着色比较器
- GUI Hex 调试器(值转换)

### Logger
[Contact](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/MiraiLogger.kt)  
Mirai 维护跨平台日志系统, 针对平台的实现为 `expect class PlatformLogger`,  
一般推荐使用顶层的 `var DefaultLogger: (identity: String?) -> PlatformLogger` 通过 `DefaultLogger( ... )` 来创建日志记录器.  
每个 `Bot` 都拥有一个日志记录器, 可通过 `Bot.logger` 获取

-日志记录尚不完善, 以后可能会修改-

### Bot
[Bot](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/Bot.kt) 为机器人  
一个机器人实例只有一个账号.  
一个机器人实例由多个模块构成.  
- `BotNetworkHandler` (管理所有网络方面事务, 本文不介绍)
- `ContactSystem` (管理联系人, 维护一个 `QQ` 列表和一个 `Group` 列表)

Mirai 能同时维护多个机器人账号.

[BotHelper](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/BotHelper.kt) 中存在一些快捷方法  

### Contact
[Contact](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/Contact.kt) 为联系人.  
虽是联系人, 但它包含 `QQ` 和 `Group`.  
联系人并不是独立的, 它必须隶属于某个 `Bot`  

**共有方法**:  
- `sendMessage`(`String`|`Message`|`MessageChain`)

**共有属性**:
- id (即 QQ 号和群号)

注: 为减少出错概率, 联系人的 `id` 均使用无符号整型 `UInt`, 这是 Kotlin 1.3 的一个实验性类型  
我们建议您在开发中也使用 `UInt`, 以避免产生一些难以发现的问题

### Message
Mirai 中所有的消息均为对象化的 [Message](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/Message.kt)  
实际上, 所有的 `Message` 都是 `inline class`, 保证无性能损失的前提下又不失使用的严谨性和便捷性.  
`Message` 有大量扩展和相关函数. 本文只介绍使用较多的一部分. 其他函数您也将会在实际开发中通过注释指引了解到.    

#### MessageChain

一条消息为一个 `MessageChain` 对象.  
`MessageChain` 也是 `Message` 的一种  
`MessageChain` 实现 `MutableList` 接口.  
它有多种实现:
- `inline class MessageChainImpl` 通常的 `MutableList<Message>` 实现
- `inline class SingleMessageChain` 单个消息的不可变代表包装
- `object NullMessageChain` 空的不可变实现. 用于替代 `null` 情况  

仅 `NullMessageChain` 是公开(public)的. 在开发中无需考虑另外两个的存在, 他们将会在 Mirai 内部合适地使用.

#### Types 
现支持的消息类型:  
- `PlainText` 纯文本
- `Image` 图片 (将会有独立章节来说明图片的上传等)
- `Face` 表情 (QQ 自带表情)

计划中:  
- `At` (仅限群, 将会被 QQ 显示为蓝色的连接)
- `XML`
- `File` (文件上传)

#### Operators

| 操作表示   |  说明  |
|---| ---|
| Message + Message | 连接 `Message`, 得到 `MessageChain`  |
| Message + String | 连接 `Message` 与 `String`(`PlainText`) 为 `MessageChain` |
| Message eq String | 可读字符串如 "\[@10000\]" 判断 |
| String in Message | 内容包含判断 |

#### Extensions

| 扩展方法   |  说明  |
|---| ---|
|String.toChain():MessageChain| PlainText(this) |
|Message.toChain():MessageChain| 构造上文提到的 SingleMessageChain |
|suspend Message.sendTo(Contact)| 发送给联系人 |

### Image
考虑到协议需求和内存消耗, Mirai 的所有 API 均使用 [ExternalImage](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/ExternalImage.kt)
`ExternalImage` 包含图片长宽、大小、格式、文件数据
您只需通过扩展函数处理图片.

| 扩展函数   |  说明  |
|---| ---|
|suspend ExternalImage.sendTo(Contact)| 上传图片并以纯图片消息发送给联系人 |
|suspend ExternalImage.upload():Image | 上传图片并得到 [Image] 消息 |
|suspend Contact.sendImage(ExternalImage) | 上传图片并发送给指定联系人 |

注: 使用 `upload` 而不是 `toMessage` 作为函数名是为了强调它是一个耗时的过程.

#### Image JVM

对于 JVM 平台, Mirai 提供额外的足以应对大多数情况的扩展函数:  
[ExternalImageJvm](mirai-core/src/jvmMain/kotlin/net.mamoe.mirai/utils/ExternalImageJvm.kt)  
若有必要, 这些函数将会创建临时文件以避免使用内存缓存图片  
一下内容中, `IMAGE` 可替换为 `ExternalImage`, `BufferedImage`, `File`, `InputStream`, `URL` 或 `Input` (来自 `kotlinx.io`) 

转为 `ExternalImage`  
- `suspend IMAGE.toExternalImage():ExternalImage`

直接发送  
- `suspend IMAGE.sendTo(Contact)`
- `suspend Contact.sendImage(IMAGE)`

转为 Message  
- `suspend IMAGE.upload(Contact)`
- `suspend Contact.upload(IMAGE)`

只要语义上正确的函数, 在 Mirai 都是可行的.

### Event

#### Subscription

[查看相关监听代码](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/Subscribers.kt)  
 
您可以通过顶层 (top-level) 方法 `subscribeXXX` 对某个事件进行监听, 其中 `XXX` 可以是   
 - Always (不断监听)
 - Once (一次监听)
 - Until / While (条件监听)  

例:    
```kotlin
subscribeAlways<FriendMessageEvent>{
  //it: FriendMessageEvent
}
```

#### Message Event

对于消息事件, Mirai 还提供了更强大的 DSL 监听方式.  
[MessageSubscribersBuilder](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/MessageSubscribers.kt#L140)  
可用条件方法为:  
 - case (内容相等) 
 - contains
 - startsWith
 - endsWith
 - sentBy (特定发送者)

```kotlin
// 监听所有群和好友消息
subscribeMessages {// this: MessageSubscribersBuilder
  case("你好"){
    // this: SenderAndMessage
    // message: MessageChain
    // sender: QQ
    // it: String (来自 MessageChain.toString)
    // group: Group (如果是群消息)
    reply("你好!")// reply将发送给这个事件的主体(群消息的群, 好友消息的好友)
  }
  
  replyCase("你好"){ "你好!" } // lambda 的返回值将会作为回复消息
  
  "Hello" reply "World" // 收到 "Hello" 回复 "World"
}
```

当然, 您也可以仅监听来自群或好友的消息      
```kotlin
// 监听所有好友消息
subscribeFriendMessages {  }
//监听所有群消息
subscribeGroupMessages {  }
```

另外, 由于 Mirai 可同时维护多个机器人账号, Mirai 也提供了对单个机器人的事件的监听.  
为了限制只监听来自某个机器人账号的事件, 您只需要在 `subscribeMessages` 前添加 `bot.` 将其修改为调用扩展方法.  
例:    
```kotlin
bot.subscribeMessages {  }
```
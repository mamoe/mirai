# Mirai
[![HitCount](http://hits.dwyl.io/him188/mamoe/mirai.svg)](http://hits.dwyl.io/him188/mamoe/mirai)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7d0ec3ea244b424f93a6f59038a9deeb)](https://www.codacy.com/manual/Him188/mirai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mamoe/mirai&amp;utm_campaign=Badge_Grade)

**TIM PC 协议** 跨平台 QQ 协议支持库.

- 纯 Kotlin 实现
- JVM 平台额外提供插件模式服务端
  
若您有任何意见或建议, 请告诉我们.  

部分协议来自网络上开源项目  

**一切开发旨在学习，请勿用于非法用途**

## Update log

在 [Project](https://github.com/mamoe/mirai/projects/1) 查看已支持功能和计划  
在 [UpdateLog](https://github.com/mamoe/mirai/blob/master/UpdateLog.md) 查看版本更新记录

## Use as library
把 Mirai 作为库内置于您的项目中使用.  
Mirai 只上传在 jcenter, 因此请确保添加 `jcenter()` 仓库  
```kotlin
repositories{
  jcenter()
}
```
若您需要使用在跨平台项目, 您需要对各个目标平台添加不同的依赖.  
若您只需要使用在单一平台, 则只需要添加一项该平台的依赖.  

您需要将 `VERSION` 替换为最新的版本(如 `0.5.1`): [![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
Mirai 目前还处于实验性阶段, 建议您时刻保持最新版本.

**common**
```kotlin
implementation("net.mamoe:mirai-core-common:VERSION")
```
**jvm**
```kotlin
implementation("net.mamoe:mirai-core-jvm:VERSION")
```
**android**
```kotlin
implementation("net.mamoe:mirai-core-android:VERSION")
```

## Try

### On JVM or Android
现在您可以开始体验低付出高效率的 Mirai

```kotlin
val bot = Bot(qqId, password).alsoLogin()
bot.subscribeMessages {
  "你好" reply "你好!"
  "profile" reply { sender.queryProfile() }
  contains("图片"){ File(imagePath).send() }
}
bot.subscribeAlways<MemberPermissionChangedEvent> {
  if (it.kind == BECOME_OPERATOR)
    reply("${it.member.id} 成为了管理员")
}
```

1. Clone
2. Import as Gradle project
3. 运行 Demo 程序: [mirai-demo](#mirai-demo) 示例和演示程序

**转到[开发文档](#Development-Guide---Kotlin)**

## Contribution

我们欢迎一切形式的贡献. 若您有兴趣为 Mirai 实现 JS, iOS, Native 平台, 请联系我(`Him188@mamoe.net`).  
若在使用过程中有任何疑问, 可提交 issue 或是邮件联系. 我们希望 Mirai 变得更易用.

## Requirements

#### Run-time 
所有平台: Kotlin 1.3.61  
JVM 平台: JRE 6   
Android: SDK 15

#### Build Mirai
所有平台:  Kotlin 1.3.61  
JVM 平台: Java 11 (OpenJDK 11)  
Android: SDK 15

### Using Java 
Q: 是否能只使用 Java 而不使用 Kotlin 来调用 Mirai?  
A: 目前不能.  
   Mirai 大量使用协程, 内联, 扩展等 Kotlin 专有特性. 在 Java 调用这些 API 将会非常吃力.  
   您必须具有 Kotlin 技术才能正常使用 Mirai.  

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
- [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)

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
  - [mirai-debug](#mirai-debug) 抓包工具和分析工具
- [Logger](#Logger) 日志系统
- [Bot](#Bot) 机器人类
- [Contact](#Contact) 联系人
- [Message](#Message) 消息
  - [MessageChain](#MessageChain) `MessageChain`
  - [Types](#Types-Of-Message) 消息类型
  - [Operators](#Operators) `Message` 一般用法
  - [Extensions](#Extensions) `Message` 的常用扩展方法
- [Image](#Image) 图片
  - [Image JVM](#Image-JVM) JVM 平台扩展实现
- [Event](#Event) 事件
  - [Subscription](#Subscription) 事件监听(订阅) 
  - [Message Event](#Message-Event) 针对消息事件的订阅实现

### Introduction 

Mirai 目前为快速流转（Moving fast）状态, 增量版本之间可能不具有兼容性，任何功能都可能在没有警告的情况下添加、删除或者更改。  
Mirai 源码完全开放, 您可以参考 Mirai 的协议实现来开发其他框架, 但需注明来源并遵守开源协议要求.

### Modules
Mirai 的模块组成

#### mirai-core
Mirai 的核心部分.

- 含全部协议和 `Bot`, `Message`, `Event` 等支持.
- 独立跨平台, 可以被以库的形式内置在任意项目内
- 现有 JVM 与 AndroidLib 支持
- 未来计划 Native 支持 

#### mirai-http-api
Http API 调用支持. 这是一个单向依赖 `mirai-core` 的模块, 可作为一个附加功能使用.  
您可以使用其他语言通过 Http API 调用 Mirai.    

开发尚未完成.

#### mirai-console
- 仅 JVM 平台
- 仅命令行
- Jar 插件支持

#### mirai-demo
Samples and demos.  
监听事件示例 [SubscribeSamples](mirai-demos/mirai-demo-1/src/main/java/demo/subscribe/SubscribeSamples.kt)  
随机图片发送 [Gentleman](mirai-demos/mirai-demo-gentleman/src/main/kotlin/demo/gentleman/Main.kt)

感谢 [@Freedom](https://github.com/Freedom0925) 的 [Android App Demo](https://github.com/mamoe/mirai/blob/master/mirai-demos/mirai-demo-android/src/main/kotlin/net/mamoe/mirai/demo/MainActivity.kt)
#### mirai-debug
抓包工具和分析工具. 不会进行稳定性维护.  

- 抓包自动解密和分析
- Hex 着色比较器
- GUI Hex 调试器(值转换)

### Logger
[Logger](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/MiraiLogger.kt)  
Mirai 维护跨平台日志系统, 针对平台的实现为 `expect class PlatformLogger`,  
一般推荐使用顶层的 `var DefaultLogger: (identity: String?) -> PlatformLogger` 通过 `DefaultLogger( ... )` 来创建日志记录器.  
每个 `Bot` 都拥有一个日志记录器, 可通过 `Bot.logger` 获取

*日志记录尚不完善, 以后可能会修改*

### Bot
[Bot](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/Bot.kt) 为机器人  
一个机器人实例只有一个账号.  

Mirai 能同时维护多个机器人账号. 

[BotHelper](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/BotHelper.kt) 中存在一些快捷方法, 您可以先继续阅读本文再查看捷径.  

### Contact
[Contact](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/Contact.kt) 为联系人.  
虽是联系人, 但它包含 `QQ`, `Group`, 和 `Member`(群成员).  
联系人并不是独立的, 它必须隶属于某个 `Bot`.  

**共有成员函数**:  
- `suspend fun sendMessage`(`String`|`Message`|`MessageChain`)

**共有属性**:
- `val id: UInt` (即 QQ 号和群号)

注: 为减少出错概率, 联系人的 `id` 均使用无符号整型 `UInt`, 这是 Kotlin 1.3 的一个实验性类型  
我们建议您在开发中也使用 `UInt`, 以避免产生一些难以发现的问题

### Message
Mirai 中所有的消息均为对象化的 [Message](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/Message.kt)  
大多数 `Message` 都是 `inline class`, 因此这种模式不会带来性能损失.

#### MessageChain

一条发出去的消息或接收到的消息为一个 `MessageChain` 对象, 它实现 `Message` 接口:  
`interface MessageChain : MutableList<Message>, Message`   

一个普通的 `Message` 不能发送, 只能组成 `MessageChain` 然后发送.

它有多种实现:
- `internal inline class MessageChainImpl : MutableList<Message>, MessageChain`: 通常的实现. 非线程安全.
- `internal inline class SingleMessageChain : MessageChain`: 用于包装单个 `Message` 为 `MessageChain`. 实例化后不可修改
- `object NullMessageChain : MessageChain`: 不可变的空集合. 只应改被用于替代 `null` 的情况  

#### Types of Message 
现支持的消息类型:  
- `inline class PlainText : Message` 纯文本
- `inline class Image : Message` 图片 (将会有独立章节来说明图片的上传等)
- `inline class Face : Message` 表情 (QQ 自带表情)
- `inline class At : Message` (仅限群, 将会被 QQ 显示为蓝色的连接)

计划中:  
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

您无需记忆用法.   
在监听事件后的事件处理过程中, 您可调用扩展 `image.send()` 来发送图片. 或是调用 `image.upload()` 来上传并得到一个类型为 `Image` 的 `Message` 以便于发送组合类型的消息

#### Image JVM

对于 JVM 平台, Mirai 提供额外的足以应对大多数情况的扩展函数:  
[ExternalImageJvm](mirai-core/src/jvmMain/kotlin/net.mamoe.mirai/utils/ExternalImageJvm.kt)  
若有必要, 这些函数将会创建临时文件以避免使用内存缓存图片  
以下内容中, `IMAGE` 可替换为 `ExternalImage`, `BufferedImage`, `File`, `InputStream`, `URL` 或 `Input` (来自 `kotlinx.io`) 

转为 `ExternalImage`  
- `suspend IMAGE.toExternalImage():ExternalImage`

直接发送  
- `suspend IMAGE.sendAsImageTo(Contact)`
- `suspend Contact.sendImage(IMAGE)`

转为 Message  
- `suspend IMAGE.uploadAsImage(Contact)`
- `suspend Contact.upload(IMAGE)`

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
为了限制只监听来自某个机器人账号的事件, 您只需要在 `subscribeMessages` 前添加 `bot.` 将其修改为调用 `Bot` 下的扩展方法.  
例:    
```kotlin
bot.subscribeMessages {  }
```

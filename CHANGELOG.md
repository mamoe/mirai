# Version 1.x

## `1.2.1`  2020/8/19
- 修复在 Java 调用 `group.uploadImage` 时编译出错的问题 (#511)
- 为 `group.uploadVoice` 添加 Java 方法 (需要 [kotlin-jvm-blocking-bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge)) (#512)
- 更新 ktor 到 1.4.0

## `1.2.0`  2020/8/19

### 新特性
- 初步语音支持: `Group.uploadVoice`, 支持 silk 或 arm 格式.  
   **注意**: 现阶段语音实现仅为临时方案, 在将来 (`2.0.0`) 一定会变动. 使用时请评估可能带来的不兼容性.

- 新增将日志转换为 log4j, JDK Logger, SLF4J 等框架的方法: `LoggerAdapters` (#498 by [@Karlatemp](https://github.com/Karlatemp))
- 支持解析好友输入状态: `FriendInputStatusChangedEvent` (by [@sandtechnology](https://github.com/sandtechnology))
- 支持解析好友昵称改变事件: `FriendNickChangedEvent` (#507 by [@Karlatemp](https://github.com/Karlatemp))
- `nextEvent` 和 `nextEventOrNull` 添加 `filter`
- 将 mirai 码相关内容从 mirai-serialization 集成到 mirai-core
- `GroupMessageEvent` 现在实现接口 `GroupEvent`
- `FriendMessageEvent` 现在实现接口 `FriendEvent` (#444)

### 依赖更新
- 更新 Kotlin 版本到 [`1.4.0`](https://blog.jetbrains.com/zh-hans/kotlin/2020/08/kotlin-1-4-released-with-a-focus-on-quality-and-performance-zh/)
- 更新 kotlinx-coroutines-core 到 `1.3.9`
- 使用者也需要更新到 `1.4.0`, 至少更新编译器 (Maven 和 Gradle 插件)
- **更新 kotlinx-serialization 到 `1.0.0-RC`**: kotlinx-serialization 在此版本做了较大的不兼容改动.

### API 弃用
- `String.toMessage`: 为避免和 mirai code 产生混乱.
- `URL.toExternalImage`
- `Input.toExternalImage`

### 优化和修复
- 显式 API
  - mirai 的所有公开 API 均已经显式加上 `public` 修饰符, 遵循 [Koltin Explicit API mode](https://kotlinlang.org/docs/reference/whatsnew14.html#explicit-api-mode-for-library-authors) 规范. 如:
    ```kotlin
         data class BotOnlineEvent internal constructor(
            override val bot: Bot
        ) : BotActiveEvent, AbstractEvent()
    ```
  - *调整了一些不应该公开的 API 为 `internal`, 这些调整在绝大多数情况下不影响现有代码*
- 修复群权限判断失败的问题 (#389)
- 修复 `syncFromEvent` 文档错误 (#427)
- 新增 `BotConfiguration.loadDeviceInfoJson(String)` (#450)
- 修复成员进群后第一次发言触发改名事件的问题 (#475 by [@cxy654849388](https://github.com/cxy654849388)
- 修复 `group.quit` 未正确执行的问题 (#472, #477 by [@Mr4s](https://github.com/Mrs4s)
- 修复初始化时 syncCookie 同步问题
- 修复 Network Protocol: java.lang.IllegalStateException: returnCode = -10008 (#470)
- 修复 `Member.isMuted`
- 修复 `Method.registerEvent` 相关问题 (#495 by @sandtechnology, #499 by @Karlatemp)
- 修复 Android 手表协议无法监听撤回事件的问题 (#448)
- 改进好友消息同步过程

## `1.1.3`  2020/7/17
- 修复 ListenerHost Java 兼容性问题  (#443, #446 by [@Karlatemp](https://github.com/Karlatemp))

## `1.1.2`  2020/7/16
- 修复 JvmMethodEvents `T.registerEvents` 注册时错误判断 `@NotNull` 注解的问题 (#436)

## `1.1.1`  2020/7/11
- 修复最后一个 mirai 码之后的消息无法解析的问题 (#431 [@cxy654849388](https://github.com/cxy654849388))

## `1.1.0`  2020/7/9
- 支持 Android 手表协议 (`BotConfiguration.MiraiProtocol.ANDROID_WATCH`)
- `EventHandler` 现在支持 `Nothing` 类型.
- 修复无需同意直接进群时，在加载新群信息完成前收到消息过早处理的问题 (#370)
- 修复在某些情况下，管理员邀请群Bot加群会被误判为群成员申请加群的问题 (#402 by [@kenvix](https://github.com/kenvix))
- 修复从其他客户端加群时未同步的问题 (#404, #410)
- 修复 `ConfigPushSvc.PushReq` 解析失败的问题 (#417)
- 修复 `_lowLevelGetGroupActiveData`
- 修复 `SimpleListenerHost.coroutineScope` 潜在的 Job 被覆盖的问题

## `1.0.4` 2020/7/2
- 修复上传图片失败时内存泄露的问题 (#385)
- 修复大量图片同时上传时出错的问题 (#387)
- 修复在一些情况下 BotOfflineEvent 没有正常处理而无法继续接收消息的问题 (#376)
- 修复 Bot 在某个群 T 出某个人导致 Bot 终止的问题 (#372)
- 修复 `@PlannedRemoval` 的文档

## `1.1-EA2` 2020/7/2

- 添加 `BotConfiguration.json`, 作为序列化时使用的 Json format, 修复潜在的因 kotlinx.serialization 进行不兼容更新而导致的不兼容.

**不兼容变更**:
- Image.imageId 后缀由 `.mirai` 变为图片文件实际类型, 如 `.png`, `.jpg`. 兼容原 `.mirai` 后缀.

**修复**:
- ([1.0.4](https://github.com/mamoe/mirai/releases/tag/1.0.4) 中修复的问题)
- ([1.0.3](https://github.com/mamoe/mirai/releases/tag/1.0.3) 中修复的问题)

## `1.0.3` 2020/6/29
- 修复 friendlist.GetTroopListReqV2：java.lang.IllegalStateException: type mismatch 10 (#405)

## `1.1-EA` 2020/6/16

**主要**:
- 添加实验性 `CodableMessage` 作为支持 mirai 码的 `Message` 的接口.
- 支持 [mirai 码](docs\mirai-code-specification.md) 解析; 新模块 [`mirai-serialization`](mirai-serialization)
- 实现 `MessagePreSendEvent` 和 `MessagePostSendEvent` (#339).

**不兼容变更**:
- 重命名实验性 API `CustomMessage.Factory.serialize` 到 `CustomMessage.Factory.dump`
- 重命名实验性 API `CustomMessage.Factory.deserialize` 到 `CustomMessage.Factory.load`
- 弃用 `MessageSendEvent` (#339). 迁移计划: WARNING in 1.1.0, ERROR in 1.2.0, REMOVE in 1.3.0
- 调整 `VipFace` 的 mirai 码表示, 详见 mirai 码规范
- `Face.toString()` 现在返回表情名称, 如 "\[偷笑\]", 而不是 "\[表情\]" (#345 @goldimax)

**优化和修复**:

- 修复群头像的获取不正确的问题 (#340)
- 将 `PttMessage` 与 `Voice` 标注 `@MiraiExperimentalAPI` (missing)
- 删除 `Message.plus(another: Flow<Message>)` 的 `@ExperimentalCoroutinesApi`
- 提升发送群消息的稳定性
- 一些文档优化
- 其他内部优化
- 提升在上个版本中弃用的 API 的弃用等级


## `1.0.2` 2020/6/1
- 新增 `Bot.botInstancesSequence`
- 修复日志中的时间未更新的问题
- 修复在某些情况下，Bot登录的时候无限重连 (#361)
- 优化一些文档注释

## `1.0.1` 2020/5/25
- 新增临时会话消息发送事件: `TempMessageSendEvent` (#338)
- 新增 `Bot.isOnline` (#342)
<br />

- 修复日志重定向到文件后无换行的问题
- 修复 Bot 被邀请入群事件的解析, 添加 `BotJoinGroupEvent.Invite` (#344)
- 修复 IPv6 地址支持 (#334)
- 修复一些 KDoc (#337)
- 优化一些内部的日志的显示

## `1.0.0` 2020/5/22

- `ContactOrBot` 现在继承 `CoroutineScope`
- 在没有手动指定 `deviceInfo` 时构建 Bot 将会发出警告, 须手动选择使用 `randomDeviceInfo` 或 `fileBasedDeviceInfo` 或自定义, 详见 [BotConfiguration.kt: Lines 69-72](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/BotConfiguration.common.kt#L69-L72)
<br />

- 引入 `SimpleListenerHost` 以帮助 Java 处理事件监听
- 添加 Java 广播事件的方式: `EventKt.broadcast(Event)`
- 添加 `Bot.getInstanceOrNull`
- 改进 JVM 平台的 `PlatformLogger`, 添加 `DirectoryLogger`, `SingleFileLogger` 以提供重定向日志的快捷方式
- 统一日志格式, 使用 (正则) `^([\w-]*\s[\w:]*)\s(\w)\/(.*?):\s(.+)$`. 详见 [PlatformLogger.jvm.kt: Line 46](mirai-core/src/jvmMain/kotlin/net/mamoe/mirai/utils/PlatformLogger.jvm.kt#L46)

<br />

- 弃用 `Bot.queryUrl(Image)`, 改用 `image.queryUrl()` 扩展. (保留兼容到 1.2.0)
- 弃用 `Bot.accept*`, `Bot.reject*` 等相应入群请求等事件的方法, 改用事件的成员函数. (保留兼容到 1.2.0)

<br />

- 修复 `Bot` 实例化时 `NPE` 问题
- 修复网络状态差时 `Bot` 网络模块无法处理分包的问题
- 修复当无 Bot 在线时调用 `image.queryUrl()` 抛出的异常与 KDoc 描述不符的问题
- 修复 `BotJoinGroupEvent` 重复广播问题
- 修复邀请 Bot 进群时事件处理异常的问题 (#319)
- 修复当 `Event` 被实现为一个 Kotlin `object` 时无法正常拦截事件的问题
- 修复图片链接获取为空的问题 (#318)
- 修复成员被移除群后可能发生内存泄露的问题
- 修复异常没有正确输出到日志的问题
- 修复一些 `DefaultLogger` 的不恰当使用的问题
- 修复 `UnknownHostException` 未被正常捕获的问题

<br />

- 在 Bot 被禁言时忽略 `reply` 方式创建的监听器 (`subscribeMessages` DSL)
- 使用更宽松的方式读取 `device.json`
- 将 `Bot.selfQQ` 标注 `@MiraiExperimentalAPI`
- 提高默认心跳超时时间
- 改进多处 KDoc
- 更新 kotlinx-coroutines-core 到 1.3.7
- ... 忽略了内部变动

## `1.0-RC2-1` 2020/5/11
修复一个 `VerifyError`

## `1.0-RC2` 2020/5/11
主要内容:
- 增强网络稳定性 (#298, #317), 修复 `Bot.close` 或 Bot 离线后没有从 `Bot.botInstances` 中删除的问题 (#317)
- `subscribeMessages` 现在默认使用 `MONITOR` 优先级
- `MessageChain` 现在继承 `List<SingleMessage>`
- 新增 `messageChainOf(vararg Message)`
- 支持 Bot 头像更改事件: `BotAvatarChangedEvent` (#271)
- 支持好友头像更改事件: `FriendAvatarChangedEvent`
- 新增 `nextEventOrNull`: 挂起当前协程, 直到监听到事件的广播, 返回这个事件实例. 超时时返回 `null`
- **弃用 `Bot.subscribe.*`, `Bot.nextMessage`, `Bot.subscribe.*Messages`:  
  为了更好的协程生命周期管理, 这些函数已经被隐藏, 保留二进制兼容到 1.3.0**.  
  现有源代码不会被破坏, 但将不再筛选事件的 `Bot` 实例. 在 mirai 决定好替代的 API 前需要手动筛选. (即不影响目前单 Bot 运行的服务)
- 支持在事件监听时使用 Kotlin 函数引用:
  ```kotlin
  suspend fun onMessage(event: GroupMessageEvent): ListeningStatus {
      return ListeningStatus.LISTENING
  }
  scope.subscribe(::onMessage /*, priority=..., concurrency=... */)
  ```
- 支持反射式事件监听, 改善 Java 的事件监听体验. 示例查看 [JvmMethodEventsTest.kt: Line 22](mirai-core/src/jvmTest/kotlin/net/mamoe/mirai/event/JvmMethodEventsTest.kt#L22)
- 添加 `typealias EventPriority = Listener.EventPriority`
- 优化 `Face` 的构造器: 现在 `Face` 拥有一个参数为 `id` 的公开构造器
- 让 `ContactList` 实现接口 `Collection`
- 弃用 `QuoteReply.time` 等语意不明的扩展 (无法区分 `time` 是 `source` 的时间还是 `QuoteReply` 自身时间)

优化 & 修复:
- 删除 `FileCacheStrategy.newImageCache(URL, format: String)` 中的 `format` 参数
- 隐藏 `MessageChain` 原有 `Iterable` 相关 API (兼容现有代码)
- 修复 `Message.repeat`
- 修复 `MemberJoinEvent` 比 `MemberJoinRequestEvent` 早广播的问题 (#288)
- 修复 Bot 接受好友申请时 groupId 处理错误 (#309)
- 修复 `MessageSubscribersBuilder` 一处 KDoc 错误 (#308 @wuxianucw)
- 修复 Android 平台 `BufferedImage ClassNotDefFound` 的问题
- 优化 `MessageSource.internalId` KDoc
- 优化 重连时的计时显示 (#311 @Karlatemp)
- 优化 `Bot.getInstance` 找不到相关 `Bot` 实例时的异常信息
- 将 `MessageMetadata.contentToString` 定义为 `final`
- 忽略了 732 类型同步消息 (原启动后会大量显示)
- 忽略 'VIP 进群提示' 的群同步消息
- 让随机设备信息更随机
- 其他一些内部优化 (无公开 API 变更)

## `1.0-RC`  2020/5/6

### 事件优先级与拦截
> 特别感谢 @Karlatemp (#279)

- 支持事件拦截: `Event.intercept()`, `Event.isIntercepted`

- 支持事件优先级: `HIGHEST, HIGH, NORMAL, LOW, LOWEST` 和 `MONITOR`  
事件广播时按监听器的优先级从高到低依次调用, 在任意一个监听器 拦截事件(`Event.intercept()`) 后停止广播, 不调用后续监听器.  
最后调用 `MONITOR` 级别的监听器.

- 在 `subscribe`, `subscribeAlways`, `nextMessage`, `syncFromEvent`, `subscribeMessages` 等所有事件监听函数中添加 `priority` 参数, 默认使用 `NORMAL` 优先级.  
兼容 `1.0` 以前的 API 到 `1.2.0`, 旧版本 API 使用 `MONITOR` 级别.

### 图片缓存策略 `FileCacheStrategy`
- 新增 `FileCacheStrategy`, 可管理上传图片等操作时的缓存行为.
- 内置内存缓存 (`FileCacheStrategy.MemoryCache`) 与默认使用的临时文件 (`FileCacheStrategy.TempCache`) 缓存, 可选临时文件存放目录
- 新增 `BotConfiguration.fileCacheStrategy`, 为单个 `Bot` 指定缓存策略
- 在图片上传 (无论是否成功) 删除临时文件
- 图片上传失败时支持自动重试
- 修复部分情况下文件没有关闭的问题 (#302)
- 因新架构为懒惰处理, 弃用所有 `*.suspendToExternalImage`

### 修正 `ContactMessage` 命名歧义
(#299)

- 原有 `ContactMessage` 实际上是一个事件, 而其命名与消息 `Message` 易产生迷惑.  
  弃用 (兼容到 `1.2.0`):
  - `MessagePacket`
  - `MessagePacketBase`

  进行如下更名:
  - `ContactMessage` -> `MessageEvent`
  - `FriendMessage` -> `FriendMessageEvent`
  - `GroupMessage` -> `GroupMessageEvent`
  - `TempMessage` -> `TempMessageEvent`

  暂未决定是否提供 `UserMessageEvent` 作为 `TempMessageEvent` 和 `FriendMessageEvent` 的公共父类.

- 优化扩展函数结构, 统一放置在 `MessageEventExtensions`, 以使 `MessageEvent` 结构清晰.

### 支持平板登录方式
- 可选, 且默认作为平板身份登录, 与手机电脑不冲突.
- 可通过 `BotConfiguration.protocol` 切换协议.

### 其他

- **`MessageChain.get` 现在返回可空的 `Message`**. 可迁移到 `MessageChain.getOrFail`.
- 添加 `nextEvent`: 挂起当前协程, 直到监听到事件 `[E]` 的广播, 返回这个事件实例.
- 删除部分冗长的如 `nextMessageContainingOrNullAsync` 等函数.
- 添加 `Message.content` 扩展属性作为 `Message.contentToString()` 的捷径
- 简化图片结构, 弃用 `OnlineFriendImage`, `OnlineGroupImage`, `OfflineGroupImage`, `OfflineFriendImage` 这四个类.
- 修复关闭验证码窗口后阻塞协程的问题 (#296)
- 删除全部 `0.x.x` 版本更新时做的兼容
- 删除全部 `@SinceMirai("0.x.0")`
- 支持接收群语音消息
- 优化图片 ID 正则表达式
- 优化大量 KDoc
- 优化上传图片和长消息时的日志内容
- 允许引用回复离线的消息源 (在 `MessageChain.quote` 时消息链中的 `MessageSource` 可以为 `OfflineMessageSource`)
- 拆分 JCE 序列化到独立的库 (#300)
- 在重连时增加计时
- 简化 `MemberPermission` 比较
- 在消息事件中使用强引用 (#303)
- 修复邀请机器人进群事件无法解析的问题 (#301)

# Version 0.x

开发版本. 频繁更新, 不保证高稳定性

## `0.40.0`  2020/4/29
在 `1.0.0` 正式版发布时, 所有为旧版本做的兼容都将删除, 因此请尽快迁移.

- `Message` 不再继承 `CharSequence` (兼容到 `1.0.0`)
- 废弃 `XmlMessage` 和 `JsonMessage`. 需使用 `ServiceMessage` 并手动指定 `serviceId`
- 修复登录时概率失败的问题
- 提高事件处理稳定性
- Java 事件默认 `LOCKED`, 而不是 `CONCURRENT`
- 弃用 `PlainText.stringValue`, 以 `PlainText.content` 替代
- 将 `VipFace` 作为 `PlainText` 发送, 而不是抛出异常
- 修复 `BufferedImage.toExternalImage` 降低图片质量的问题

## `0.39.5`  2020/4/28
- 优化登录初始化, 提高稳定性 (#282)
- 支持 VIP 表情的解析: `VipFace` (不支持发送)
- 支持更多的戳一戳消息 (`PokeMessage`) 类型
- 修复 Android 平台的正则语法错误问题
- 修复 `BotInvitedJoinRequestEvent.ignore`
- 提升 `LockFreeLinkedList` 遍历性能, 即 `ContactList` 遍历性能
- 将 `LockFreeLinkedList` 标注 `@MiraiInternalAPI` 并计划于 1.0.0 修改为 `internal`

## `0.39.4`  2020/4/27
- 支持匿名消息解析 (#277)
- 修复部分情况下撤回失败的问题
- 修复部分情况下解析群名片错误的问题
- 修复解析匿名群成员错误的问题
- 修复 `LoginSolver` `Swing` 选择问题
- 添加 `NoStandardInputForCaptchaException`, 在无可用标准输入时中断登录

## `0.39.3`  2020/4/25
- 添加 `Message.isContentEmpty()` 和 `Message.isContentNotEmpty()`
- 在发送消息前检查是否为空 (#268)
- 修复重复收到一些事件的问题 (#259)
- 支持所有图片的下载链接获取 (#250)
- 修复部分情况下验证码窗口无法显示的问题 (#270)
- 构造 `ForwardMessage` 时不允许 `ForwardMessage.nodeList` 为空.

## `0.39.2`  2020/4/24
- 完善 `Message` 相关的 KDoc
- 在支持图像界面的环境下弹出验证码输入 (#257)
- 修复无法通过 id 发送图片的问题 (#262)
- 修复彩色群名解析不全的问题 (#263)

## `0.39.1`  2020/4/24
- 修复长消息发送失败的问题 (#256)
- 撤销 `Bot.instances` 更改, 添加新的 `Bot.botInstances` 以兼容以前代码
- 修复密码错误时未停止重连的问题
- 修复 `ForwardMessage` 无法从 `firstOrNull` 获取的问题

## `0.39.0`  2020/4/23
**二进制不兼容的修改:** `Bot.instances` 现在返回 `List<Bot>`, 而不是 `List<WeakRef<Bot>>` 由于他们在 JVM 签名相同, 无法做兼容.

### Contact 架构改变
原有 `Member` 继承 `QQ`, `QQ` 继承 `Contact` 架构改变.


新架构为:
- 弃用 `QQ` 命名 (二进制兼容到 1.0.0)
- 新增 `User` 继承 `Contact`, 作为 `Member` 和 `Friend` 的父类
- `Member` 继承 `User`
- `Friend` 继承 `User`

#### 迁移
由于 `Member` 不再是 `QQ` 子类, 而原本表示 '好友' 意义的 `QQ` 删除,  
需要根据实际情况替换 `QQ` 的引用为 `Friend` 或 `Group`


因修改, 新增以下 API:
- `fun Member.asFriend(): Friend`: 得到此成员作为好友的对象或抛出异常
- `fun Member.asFriendOrNull(): Friend`: 得到此成员作为好友的对象或返回 `null`
- `inline val Member.isFriend: Boolean`: 判断此成员是否为好友


同时有以下修改:
- `val User.nameCardOrNick`: 获取非空群名片 (如果是群员) 或昵称
- 弃用 `fun Member.isMuted()` 而改为属性 `val Member.isMuted`

### 图片
- 构造所有类型图片时只接受唯一一个参数 `imageId: String`.
- 所有类型图片只能获取唯一一个属性 `imageId: String` (以前可以获取长宽等数据)
- 提高发送图片的性能
- 优化 `BufferedImage.toExternalImage` 的性能
- 统一图片后缀: `{ ... }.mirai`

### 消息
- 新增合并转发及其 DSL
- 新增 `OfflineMessageSource` 构造
- 新增 `MessageSource` 修改: `MessageSource.copyAmend(block)`
- 修复 'sequence not yet available' 问题 (#)
- 修复好友消息的消息源 id 错误的问题 (#247)
- 如果群成员是好友, 则发送好友消息, 而不是临时会话消息.
- 添加 `MessageSource.internalId` 以便将来使用
- 添加 `OnlineMessageSource.toOffline`

- 添加 `ContactMessage.time`
- 添加 `ContactMessage.senderName`

#### `OfflineMessageSource` 构造
可使用 DSL 构造离线消息, 修改其发送人, 发送时间, 发送内容等. 这对于跨群转发等情况十分有用.  
[MessageSourceBuilder.kt: Line 90](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/MessageSourceBuilder.kt#L90)
DSL 总览:
```
val source: OfflineMessageSource = bot.buildMessageSource {
    bot sendTo target // 指定发送人和发送目标
    metadata(source) // 从另一个消息源复制 id, internalId, time

    messages { // 指定消息内容
        +"hi"
    }
}
```

#### 合并转发及其 DSL
合并转发: [ForwardMessage](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/ForwardMessage.kt#L80)  
DSL: [ForwardMessageBuilder](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/ForwardMessage.kt#L315)  

DSL 总览:
```kotlin
buildForwardMessage {
    123456789 named "鸽子 A" says "咕"
    100200300 named "鸽子 C" at 1582315452 says "咕咕咕" // at 设置时间
    987654321 named "鸽子 B" says "咕"
    myFriend says "咕"
    bot says { // 构造消息链, 同 `buildMessageChain`
        +"发个图片试试"
        +Image("{90CCED1C-2D64-313B-5D66-46625CAB31D7}.jpg")
    }
}
```
不支持解析别人的转发.

### 其他
- 支持 bot 名片被其他人修改时的同步
- 修复登录时遇到服务器不可用时无法继续重连的问题
- 更名 `Identified` 到 `ContactOrBot`, 去掉其 '实验性' 注解
- `Bot.instances` 现在返回 `List<Bot>`, 而不是 `List<WeakRef<Bot>>` (二进制兼容)
- 更名 `subscribingGet` 到 `syncFromEvent`, 并将其定义为稳定 API.
- 更名 `subscribingGetAsync` 到 `asyncFromEvent`, 并将其定义为稳定 API.
- 添加接受 `eventClass: KClass<Event>` 参数的事件监听 `subscribe`
- 在 `MessageSubscribersBuilder` 添加 `sentBy(User)`, `sentFrom(Group)`, `atAll`, `at` DSL
- 修复某些时候未处理 `BotOfflineEvent.Force` 的问题

## `0.38.0`  2020/4/20
- 新增自定义消息 (实验性): `CustomMessage`
- 新增 `MessageChain.contentEquals`
- 新增 `Message.isPlain`, `Message.isNotPlain`
- 新增 `MessageChain.allContent`, `MessageChain.noneContent`
- 修复 `CombinedMessage.toString` 顺序错误, 添加缓存
- 新增 `BotConfiguration.inheritCoroutineContext`
- 将 Java API `MessageChain.getOrNull` 更名为 `MessageChain.firstOrNull`
- 将 Java API `MessageChain.get` 更名为 `MessageChain.first`
- 将 Java API `MessageReceipt.recall(long)` 更名为 `MessageReceipt.recallIn(long)` 以与其他 API 保持一致
- 优化 `MessageChainBuilder` 构建逻辑

## `0.37.5`  2020/4/20
- 上传长消息和图片时允许重试, 提高稳定性
- 优化无网络时的重连逻辑
- 在 `Message` 中添加 `equals` 和 `hashCode`, 将部分类型消息定义为 `data class`
- `MessageSource.id` 现在返回非 0 序列号
- 实现已撤回判断, 同一个 `MessageSource` 只能撤回一次

## `0.37.4`  2020/4/17
- 修复 #220: 无法正常解析邀请机器人进群的富文本消息
- 修复 #236: 删除无用的 getter 方法生成
- 修复上传长消息时报错错误的问题

## `0.37.3`  2020/4/15
新增:

- 在群名修改事件(`GroupNameChangeEvent`)中支持获取操作人
- 修复 #229, 引入 `ServiceMessage` 作为 `JsonMessage`, `XmlMessage` 的父类并处理所有类型富文本消息解析
- 将所有 `RichMessage` 标注 `MiraiExperimentalAPI` 以警告将来改动

问题修复:

- 修复潜在的长消息上传失败问题
- 简化 `MessageSubscriberBuilder` DSL, 整理 `linear.kt`, `subscribers.kt`
- 修复启动时概率解析失败 ConfigPushSvc.PushReq
- 修复 #228: 登录时没有因 `LoginFailedException` 中断
- 重构登录重连控制, 确保单一进程
- 处理无网络连接问题, 在无网络时将不尝试登录而等待网络连接
- 修复 #227: Android 最新版无法编译
- 修复 #226: BotUnmuteEvent
- 修复 #225: 重复接收到群消息撤回问题
- 修复 #220: 无法正常解析邀请机器人进群的富文本消息
- 修复 #217: 解析 OnlinePush confess 状态时没有覆盖全面
- 优化遇到未知消息时的日志

## `0.37.2`  2020/4/13
- 修复 `OnlineMessageSource.Incoming.target` 类型错误
- 引入实验性 `Identified` 接口作为 `Contact` 和 `Bot` 的公共接口
- 加快图片 MD5 计算过程
- 加快图片上传过程
- 其他小优化

## `0.37.1`  2020/4/12
**从 `0.37.1` 起 JVM 平台依赖无需带 "-jvm" 模块名**  
**即原 "mirai-core-jvm" 和 "mirai-core-qqandroid-jvm" 均需去掉 "-jvm", 变为 "mirai-core" 和 "mirai-core-qqandroid"**

- 登录时尝试多个服务器, 随服务器需求切换服务器 (解决潜在的无法登录的问题) (#52)
- 优化带有 `QuoteReply` 时的消息长度估算
- 添加 `MessageChainBuilder.build`, 效果同 `asMessageChain`
- 在 `ContactMessage` 中添加 `At.isBot`
- 在 `MessageSubscribersBuilder` 中添加 `String.invoke`, `atBot` DSL

## `0.37.0`  2020/4/11
- 支持主动退群: `Group.quit`, `BotLeaveEvent.Active`
- 支持临时消息撤回
- 支持好友消息撤回
- 修复一个内存泄露问题
- 修复彩色群名片读取失败的问题
- 修复退群事件重复广播的问题 (#221)

## `0.36.1`  2020/4/10
- 修复 `botPermission`
- 删除一些无用的调试输出

## `0.36.0`  2020/4/10
- 支持临时会话: `TempMessage` (#16)
- 支持群员主动加入事件 `MemberJoinEvent.Active`
- 添加 `subscribeTempMessages` 等相关 DSL
- 添加 `FriendAddEvent`, `FriendDeleteEvent` (#216)
- 修复各种事件重复广播的问题 (#173, #212)
- 修复 `OfflineMessageSource.id`
- 修复 `Member.kick`
- 修复彩色群名片读取, 支持群名片更改事件 (#210)
- 增加超时 (#175)
- 支持合并转发消息的解析, 修复部分情况下长消息解析失败的问题
- 修复新成员加入时没有添加进成员列表的问题 (#172)

## `0.35.0`  2020/4/8
- 新增处理加好友请求: `NewFriendRequestEvent`
- 新增处理加群请求: `MemberJoinRequestEvent`
- 现在 `MessageSource.originalMessage` 也可以获取到 `MessageSource`
- 支持机器人加入了大量群时的群列表获取
- 优化 init 过程
- 添加更清晰的错误日志
- 修复撤回自己发送的消息时的权限判定
- 修复 `botAsMember.nameCard` 修改时需要管理员权限的问题
- 修复 `MessageSource.key`
- 修复其他一些小问题

## `0.34.0`  2020/4/6

- 修复长消息判定.
- 为 `selectMessages`, `selectMessagesUnit` 添加可选筛选 context 的参数: `filterContext: Boolean`
- 统一消息日志
- 加快重连速度

`Message` 改动 (二进制兼容):

- 添加 `Message.contentToString` 以转换为最接近官方消息的字符串
- 添加 `ConstrainSingle` 的 `Message` 类型以保证一个消息链中只存在一个 `QuoteReply` 和一个 `MessageSource`
- `CombinedMessage` 现在实现接口 `MessageChian` 并变为 `internal` 以降低复杂度 (使用 `MessageChain` 替换 `CombinedMessage` 的引用).
- `Message.plus` 现在返回 `MessageChain` 而不是 `CombinedMessage`
- 弃用 `NullMessageChain` (使用 `null` 替代)
- `Message` 中 `eq`, `contains` 等函数移动至 `SingleMessage` 以避免歧义.
- 更名 `MessageChain.any<reified M>` 到 `MessageChain.anyInInstance<reified M>` 以与标准库的 `Iterable.any` 区分
- 更名 `MessageChain.first<reified M>` 到 `MessageChain.firstIsInstance<reified M>` 以与标准库的 `Iterable.first` 区分
- 更名 `MessageChain.firstOrNull<reified M>` 到 `MessageChain.firstIsInstanceOrNull<reified M>` 以与标准库的 `Iterable.firstOrNull` 区分

## `0.33.0`  2020/4/4
- 重构 [`MessageSource`](https://github.com/mamoe/mirai/blob/master/mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/MessageSource.kt), 支持直接获取相关对象, 支持所有类型的引用.
- 简化引用回复, 现在只需要 `source.quote()` 即可创建引用 (而不需要 `sender` 参数)
- 现在可通过 `QuoteReply.source` 获取源消息, 且可以撤回该消息或再次引用.
- 支持闪照: 可通过 `Image.flash()` 将普通图片转为闪照.
- 支持 `Bot.nick` (#93)
- 修复消息长度判断 (#195) (实验性)
- 修复 Android 目标上 `SystemDeviceInfo.imei` 可能会抛出 NPE 的问题
- 修复 `GroupNameChangeEvent` 重复广播的问题
- 修复 `ContactMessage.nextMessageContaining`
- 修复 `selectMessage` 时无法正常完结, 和 timeout 没有被取消的问题
- 修复 #133, #197, #187,  #180, #77, #192

## `0.32.0`  2020/4/2
- 使用 Kotlin 1.3.71, 兼容原使用 Kotlin 1.4-M1 编译的代码.
- 优化 `BotConfiguration`, 去掉 DSL 操作, 使用  `fileBasedDeviceInfo(filename)` 等函数替代. (兼容原操作方式, 计划于 `0.34.0` 删除)
- 调整长消息判定权重, 具体为: Chinese char=4, English char=1, Quote=700, Image=800, 其他消息类型转换为字符串后判断长度.
- 添加 `ContactMessage` 以替代 `MessagePacket<*, *>` 的情况
- 添加 `MessageTooLargeException`
- 使用 `Bot.id` 替代 `Bot.uin`
- 在 `Dispatchers.IO` 协程调度器中执行 Java API 创建的事件处理.
- 修复 Java API `Member.kick` 参数 `message` 没有正常传递的问题
- 将部分意外定义为 public 的 API 改为 internal.
- 将部分 internal API 从 `mirai-core` 移至 `mirai-core-qqandroid`

## `0.31.4`  2020/3/31
- 修复 At 在手机上显示错误的问题

## `0.31.3`  2020/3/31
- 修复 #178

## `0.31.2`  2020/3/30
- 修复长文本长度检测, 提高判断性能
- 修复特殊的好友图片 ID 无法构造为消息
- 新增 `AtAll.display`
- 所有消息元素统一 `toString`: `[mirai:image:ID]`, `[mirai:face:ID]`, `[mirai:at:TARGET]`, `[mirai:poke:TYPE,ID]`, `[mirai:quote:ID]` 等 (仍为实验性)

## `0.31.1`  2020/3/29
- 修复重复解析禁言事件的问题 (#83)

## `0.31.0`  2020/3/29
- 支持长消息发送, 单条消息最多含 4500 字符和 50 张图片
- 支持戳一戳消息: `PokeMessage`
- 修复 重复收到好友消息 (#129), 私聊图片出错 (#165)
- 为 `MessageChain.toString` 增加缓存 (非原子), 以提升长消息处理的性能
- 发消息失败时将抛出带提示的异常
- 添加 `MessageSource` 将被重写的警告

## `0.30.1`  2020/3/26
- 修复一些事件解析失败的问题

## `0.30.0`  2020/3/24
此版本为二进制不兼容更新, 全部使用者都需要重新编译.

源码兼容的改变:
- 删除全部 `@Depreacted` 兼容
- 删除全部多余的 `@JvmName` 以兼容将来的改变 (新 MPP 模块等级制架构)
- 调整部分函数的 JVM 可见性
- 内联部分 `MessageChain` 工具函数
- **更新到 Kotlin 1.4-M1** ([如何更新到 Kotlin 1.4-M1](https://gist.github.com/Him188/9f395d058b89226d412a200bdd0595be))

源码不兼容的改变:
- 群设置由 `Group` 移动到独立的 `GroupSettings`
- 调整 API 可见性: 将除 `BotFactory` 外 `mirai-core-qqandroid` 中全部 API 改为 `internal`

消息部分:
- `SingleMessage` 实现接口 `CharSequence` 和 `Comparable<String>`
- 为 `FriendImage`, `GroupImage`, `OnlineImage`, `OfflineImage` 增加 `companion object Key`
- 调整 `RichMessage`, 将所有子类聚合到一个文件
- 移动 `XmlMessageHelper` 为 `RichMessage.Compation`
- 命名调整: `buildXMLMessage` 改为 `buildXmlMessage`
- 修复 `CombinedMessage` 中错误的 `left` 和 `element`

事件部分:
- 加强 `selectMessages`, 增加回复, 引用回复, 默认值, 超时支持:
原处理方式:
```kotlin
val message = nextMessageOrNull(10.secondsToMillis) ?: kotlin.run {
    quoteReply("请在 10 秒内发送一张图片")
    return@case
}
val image = message.getOrNull(OnlineImage) ?: kotlin.run {
    reply(message.quote() + "请发送一张图片")
    return@case
}
reply(message.quote() + image.originUrl)
```
使用 `selectMessages` DSL:
```kotlin
selectMessagesUnit {
    has<OnlineImage>() quoteReply {
        message[OnlineImage].originUrl
    }
    timeout(10.secondsToMillis) quoteReply {
        "请在 10 秒内发送图片以获取链接"
    }
    defaultQuoteReply {
        "请发送一张图片"
    }
}
```

## `0.29.1` 2020/3/22
- 确保二进制兼容, #155
- 修复 Android 上 ECDH init 失败问题, #154

## `0.29.0` 2020/3/22
- 引入新消息监听 DSL: `whileSelectMessages`, 简化连续监听过程
```kotlin
bot.subscribeMessages {
    "开启复读模式" `->` {
        reply("成功开启")
        whileSelectMessages {
            "stop" `->` {
                reply("已关闭复读")
                false // 停止循环
            }
            default {
                reply(message)
                true // 继续循环
            }
        }
        reply("复读模式结束")
    }
}
```
- 引入新消息监听 DSL: `selectMessages`, 简化筛选监听过程
```kotlin
bot.subscribeMessages {
    "test" `->` {
        reply("choose option: 'hello', 'hi'")
        val value: String = selectMessages {
            "hello" `->` { "123" }
            "hi" `->` { "222" }
            default { "default value" }
        }
        reply(value)
    }
}
```

- 监听消息的 DSL 新增 `infix fun String.->(block)`
- 处理 `StatSvc.ReqMSFOffline` (#150)
- `Contact.sendMessage` 现在接受 `Message` 参数, 而不是 `MessageChain` 以兼容 `CombinedMessage`
- `Member.sendMessage` 现在返回 `MessageReceipt<Member>` 而不是 QQ 泛型
- 调整 JVM `MessageUtils` 中一些方法的可见性 (`@JvmSynthetic`)
- 调整命名: `OfflineImage.queryOriginUrl` 改为 `OfflineImage.queryUrl`
- 允许手动重新初始化 `Bot` (`BotNetworkHandler.init`), 确保重初始化资源释放

## `0.28.0` 2020/3/19
- 修复 Jce 反序列化在部分情况下出错的问题, 修复 #145
- 新增群公告低级 API
- 新增群活跃数据低级 API
- 修复 #141, #143, #131
- 更多原生表情 (`Face`)

## `0.27.0` 2020/3/8
- 支持 `XML`, `Json`, `LightApp` 等 `RichMessage`

## `0.26.2` 2020/3/8
- 新增 `MessageChain.repeat` 与 `MessageChain.times`
- JVM 平台下 `PlatformLogger` 可重定向输出
- 修复 `NullMessageChain.equals` 判断不正确的问题
- 新增 `PlainText.of` 以应对一些特殊情况

## `0.26.1` 2020/3/8
- 重写 Jce 序列化, 提升反序列性能
- 更新 `Kotlin` 版本到 1.3.70
- 更新 `kotlinx.coroutines`, `atomicfu`, `kotlinx.coroutines` 依赖版本

## `0.26.0` 2020/3/7
- 使用 `kotlinx.io` 而不是 `ktor.io`
- 修复 #111, #108, #116, #112

## `0.25.0` 2020/3/6
- 适配 8.2.7 版本（2020 年 3 月）协议
- 全面的 `Image` 类型: Online/Offline Image, Friend/Group Image
- 修复查询图片链接时好友图片链接错误的问题
- 修复 bugs: #105, #106, #107

## `0.24.1` 2020/3/3
- 修复 `Member` 的委托 `QQ` 弱引用被释放的问题
- 用 `Bot.friends` 替代 `Bot.qqs`
- 用 `Bot.containsFriend`, `Bot.containsGroup` 替代 `Bot.contains`
- 新增 `BotFactory.Bot(String, ByteArray)` 用 md5 密码登录
- 为 `BotFactory` 等类型的一些扩展指定 `JvmName`
- 移动 `Bot.QQ` 到低级 API

## `0.24.0` 2020/3/1
- Java 完全友好: Java 使用者可以同 Kotlin 方式直接阻塞式或异步（Future）调用 API
- 新增 `MessageSource.originalMessage: MessageChain` 以获取源消息内容
- 群消息的撤回现在已稳定 (`Bot.recall`)
- 现在可以引用回复机器人自己发送的消息: `MessageReceipt.quoteReply`
- 新增 `MessageRecallEvent`

- 整理 `MessageChain` 的构造, 优化性能
- 整理所有网络层代码, 弃用 `kotlinx.io` 而使用 `io.ktor.utils.io`
- 其他杂项优化

## `0.23.0` 2020/2/28
- 修复上传图片
- 一些问题修复
- 大量杂项优化

- `MessageReceipt.source` 现在为 public. 可获取源消息 id
- 修复上传好友图片失败的问题
- 上传群图片现在分包缓存, 优化性能

## `0.22.0` 2020/2/24
- 重构 `MessageChain`, 引入 `CombinedMessage`. (兼容大部分原 API)
- 新增 `MessageChainBuilder`, `buildMessageChain`
- `ExternalImage` 现在接收多种输入参数

- 修复访问好友消息回执 `.sequenceId` 时抛出异常的问题

## `0.21.0` 2020/2/23
- 支持好友消息的引用回复
- 更加结构化的 `QuoteReply` 架构, 支持引用任意群/好友消息回复给任意群/好友.

## `0.20.0` 2020/2/23

- 支持图片下载: `image.channel(): ByteReadChannel`, `image.url()`

- 添加 `LockFreeLinkedList<E>.iterator`
- 添加 `LockFreeLinkedList<E>.forEachNode`

- 并行处理事件监听
- 添加 `nextMessageContaining` 和相关可空版本

- '撤回' 从 `Contact` 移动到 `Bot`
- 删除 `MessageSource.sourceMessage`
- 让 MessageSource 拥有唯一的 long 类型 id, 删除原 `uid` 和 `sequence` 结构.
- 修复 `Message.eq` 歧义

## `0.19.1` 2020/2/21

- 支持机器人撤回群消息 (含自己发送的消息): `Group.recall`, `MessageReceipt.recall`
- 支持一定时间后自动撤回: `Group.recallIn`, `MessageReceipt.recallIn`
- `sendMessage` 返回 `MessageReceipt` 以实现撤回功能
- 添加 `MessageChain.addOrRemove`
- 添加 `ContactList.firstOrNull`, `ContactList.first`
- 新的异步事件监听方式: `subscribingGetAsync` 启动一个协程并从一个事件从获取返回值到 `Deferred`.
- 新的线性事件监听方式: `subscribingGet` 挂起当前协程并从一个事件从获取返回值.

##### 新的线性消息连续处理: `nextMessage` 挂起当前协程并等待下一条消息:
使用该示例, 发送两条消息, 一条为 "禁言", 另一条包含一个 At
```kotlin
case("禁言") {
    val value: At = nextMessage { message.any(At) }[At]
    value.member().mute(10)
}
```
示例 2:
```kotlin
case("复读下一条") {
    reply(nextMessage().message)
}
```


- 修复一些情况下 `At` 无法发送的问题
- 统一 ImageId: 群消息收到的 ImageId 均为 `{xxxxxxxx-xxxx-xxxx-xxxxxxxxxxxx}.jpg` 形式（固定长度 37）
- 支持成员主动离开事件的解析 (#51)

## `0.18.0` 2020/2/20


- 添加 `MessageSource.time`
- 添加事件监听时额外的 `coroutineContext`
- 为一些带有 `operator` 的事件添加 `.isByBot` 的属性扩展
- 优化事件广播逻辑, 修复可能无法触发监听的问题
- 为所有 `Contact` 添加 `toString()` (#80)


- 支持成员禁言状态和时间查询 `Member.muteTimeRemaining`
- 修复 `At` 的 `display` (#73), 同时修复 `QuoteReply` 无法显示问题 (#54).
- 广播 `BotReloginEvent` (#78)
- 支持机器人自身禁言时间的更新和查询 (#82)

## `0.17.0` 2020/2/20


- 支持原生表情 `Face`
- 修正 `groupCardOrNick` 为 `nameCardOrNick`
- 增加 `MessageChain.foreachContent(lambda)` 和 `Message.hasContent(): Boolean`


- 提高重连速度
- 修复重连后某些情况不会心跳
- 修复收包时可能产生异常

## `0.16.0` 2020/2/19


- 添加 `Bot.subscribe` 等筛选 Bot 实例的监听方法
- 其他一些小问题修复


- 优化重连处理逻辑
- 确保好友消息和历史事件在初始化结束前同步完成
- 同步好友消息记录时不广播

## `0.15.5` 2020/2/19


- 为 `MiraiLogger` 添加 common property `val isEnabled: Boolean`
- 修复 #62: 掉线重连后无 heartbeat
- 修复 #65: `Bot` close 后仍会重连
- 修复 #70: ECDH is not available on Android platform


- 从服务器收到的事件将会额外使用 `bot.logger` 记录 (verbose).
- 降低包记录的等级: `info` -> `verbose`
- 改善 `Bot` 的 log 记录
- 加载好友列表失败时会重试
- 改善 `Bot` 或 `NetworkHandler` 关闭时取消 job 的逻辑
- 修复初始化(init)时同步历史好友消息时出错的问题

## `0.15.4` 2020/2/18

- 放弃使用 `atomicfu` 以解决其编译错误的问题. (#60)

## `0.15.3` 2020/2/18

- 修复无法引入依赖的问题.

## `0.15.2` 2020/2/18


- 尝试修复 `atomicfu` 编译错误的问题


- 查询群信息失败后重试

## `0.15.1` 2020/2/15


- 统一异常处理: 所有群成员相关操作无权限时均抛出异常而不返回 `false`.


- 初始化未完成时缓存接收的所有事件包 (#46)
- 解析群踢人事件时忽略找不到的群成员
- 登录完成后广播事件 `BotOnlineEvent`

## `0.15.0` 2020/2/14



- 新增事件: `BotReloginEvent` 和 `BotOfflineEvent.Dropped`
- `AtAll` 现在实现 `Message.Key`
- 新增 `BotConfiguration` DSL, 支持自动将设备信息存储在文件系统等
- 新增 `MessageSource.quote(Member)`

- 更好的网络层连接逻辑
- 密码错误后不再重试登录
- 掉线后尝试快速重连, 失败则普通重连 (#47)
- 有原因的登录失败时将抛出特定异常: `LoginFailedException`
- 默认心跳时间调整为 60s



- 解决一些验证码无法识别的问题
- 忽略一些不需要处理的事件(机器人主动操作触发的事件)

## `0.14.0` 2020/2/13



- **支持 at 全体成员: `AtAll`**



- **支持 `AtAll` 的发送和解析**
- **修复某些情况下禁言处理异常**

小优化:
- 在 `GroupMessage` 添加 `quoteReply(Message)`, 可快速引用消息并回复
- 为 `CoroutineScope.subscribeMessages` 添加返回值. 返回 lambda 的返回值
- 在验证码无法处理时记录更多信息
- 优化 `At` 的空格处理 (自动为 `At` 之后的消息添加空格)
- 删除 `BotConfiguration` 中一些过时的设置

## `0.13.0` 2020/2/12


- 修改 BotFactory, 添加 `context` 参数.
- currentTimeMillis 减少不必要对象创建
- 优化无锁链表性能 (大幅提升 `addAll` 性能)

-qqanroid
安卓协议发布, 基于最新 QQ, 版本 `8.2.0`
支持的功能:
- 登录: 密码登录. 设备锁支持, 不安全状态支持, 图片验证码支持, 滑动验证码支持.
- 消息: 文字消息, 图片消息(含表情消息), 群员 At, 引用回复.
- 列表: 群列表, 群员列表, 好友列表均已稳定.
- 群操作: 查看和修改群名, 查看和修改群属性(含全体禁言, 坦白说, 自动批准加入, 匿名聊天, 允许成员拉人), 设置和解除成员禁言, 查看和修改成员名片, 踢出成员.
- 消息事件: 接受群消息和好友消息并解析
- 群事件: 群员加入, 群员离开, 禁言和解除禁言, 群属性(含全体禁言, 坦白说, 匿名聊天, 允许成员拉人)改动.

### mirai-api-http
HTTP API 已完成, by [@ryoii](https://github.com/ryoii).  
详见 [README](https://github.com/mamoe/mirai/tree/master/mirai-api-http)

Mirai 仍处于快速迭代状态. 将来仍可能会有 API 改动.
## `0.12.0`  *2020/1/19*

1. 监听消息时允许使用条件式的表达式, 如:
```kotlin
(contains("1") and has<Image>()){
    reply("Your message has a string '1' and an image contained")
}

(contains("1") or endsWith("2")){

}
```
原有单一条件语法不变:
```kotlin
contains("1"){

}

"Hello" reply "World"
```

2. Message: 修复 `eq` 无法正确判断的问题; 性能优化.
3. 简化 logger 结构(API 不变).
4. 事件 `cancelled` 属性修改为 `val` (以前是 `var` with `private set`)

## `0.11.0`  *2020/1/12*

- 弃用 `BotAccount.id`. 将来它可能会被改名成为邮箱等账号. QQ 号码需通过 `bot.uin` 获取.
- `Gender` 由 `inline class` 改为 enum
- `String.chain()` 改为 `String.toChain()`
- `List<Message>.chain()` 改为 `List<Message>.toChain()`
-timpc
- 修复在有入群验证时无法解析群资料的问题 (#30)

## `0.10.6`  *2020/1/8*
TIMPC
- Fix #27, 群成员找不到的问题
- 一些小优化

## `0.10.5`  *2020/1/3*
- 修复有时表情消息无法解析的问题
- 为心跳增加重试, 降低掉线概率
- 消息中的换行输出为 \n
- 其他一些小问题修复

## `0.10.4`  *2020/1/1*
- 事件处理抛出异常时不停止监听
- 添加 `Bot(qq, password, config=Default)`
- 一些性能优化

## `0.10.3`  *2020/1/1*
- 修复一个由 atomicfu 的 bug 导致的 VerifyError
- 添加 `ExternalImageAndroid`
- 事件处理抛出异常时正确地停止监听

## `0.10.1`  *2019/12/30*
**Bot 构造**  
`Bot` 构造时修改 `BotConfiguration` 而不是登录时.  
移除 `CoroutineScope.Bot`  
移除 `suspend Bot(...)`  
添加 `Bot(..., BotConfiguration.() -> Unit)`  
添加 `Bot(..., BotConfiguration = BotConfiguration.Default)`

**其他**  
全面的在线状态 (`OnlineStatus`)  
移动部分文件, 模块化

## `0.10.0`  *2019/12/23*
**事件优化**  
更快的监听过程  
现在监听不再是 `suspend`, 而必须显式指定 `CoroutineScope`. 详见 `Subscribers.kt`  
删除原本的 bot.subscribe 等监听模式.

**其他**  
`Contact` 现在实现接口 `CoroutineScope`

## `0.9.0`  *2019/12/20*
**协议模块独立**  
现在 `mirai-core` 只提供基础的抽象类. 具体的各协议实现为 `mirai-core-PROTOCOL`.  
这些模块都继承自 `mirai-core`.  
现在, 要使用 mirai, 必须依赖于特定的协议模块, 如 `mirai-core-timpc`.  
查阅 API 时请查看 `mirai-core`.  
每个模块只提供少量的额外方法. 我们会给出详细列表.

在目前的开发中您无需考虑多协议兼容.

**Bot 构造**  
协议抽象后构造 Bot 需指定协议的 `BotFactory`.  
在 JVM 平台, Mirai 通过 classname 自动加载协议模块的 `BotFactory`, 因此若您只使用一套协议, 则无需修改现行源码

**事件**  
大部分事件包名修改.

**UInt -> Long**  
修改全部 QQ ID, Group ID 的类型由 UInt 为 Long.  
**此为 API 不兼容更新**, 请将所有无符号标志 `u` 删除即可. 如 `123456u` 改为 `123456`

另还有其他 API 的包名或签名修改. 请使用 IDE 自动修补 import 即可.
## `0.8.2`  *2019/12/15*
- 修复 GroupId.toGroupInternalId 错误
- 修复解析群消息时小概率出现的一个错误

## `0.8.1`  *2019/12/15*
- 修复有时群资料无法获取的情况
- 现在 `At.qq`, `Long.qq` 等函数不再是 `suspend`

## `0.8.0`  *2019/12/14*
协议
- 现在查询群资料时可处理群号无效的情况
- 现在能正常分辨禁言事件包

功能
- 增加无锁链表: LockFreeLinkedList, 并将 ContactList 的实现改为该无锁链表
- **ContactSystem.getQQ 不再是 `suspend`**
- ContactSystem.getGroup 仍是 `suspend`, 原因为需要查询群资料. 在群 ID 无效时抛出 `GroupNotFoundException`

优化
- 日志中, 发送给服务器的包将会被以名字记录, 而不是 id

## `0.7.5`  *2019/12/09*
- 修复验证码包发出后无回复 (错误的验证码包)

## `0.7.4`  *2019/12/08*
- 修复 bug
- 优化 JVM 平台上需要验证码时的提示

## `0.7.3`  *2019/12/07*
- 删除 klock 依赖, 添加 Time.kt. 待将来 kotlin Duration 稳定后替换为 Duration

## `0.7.2`  *2019/12/07*
- 使所有协议相关类 `internal`
- 去掉一些 `close` 的不应该有的 `suspend`
- `QQ`, `Member`, `Group` 现在继承接口 `CoroutineScope`
- 将 `LoginResult` 由 `inline class` 修改为 `enum class`
- 添加和修改了 `BotAccount` 和 `Bot` 的构造器

## `0.7.1`  *2019/12/05*
- 修复禁言时间范围错误的问题
- 禁言的扩展函数现在会传递实际函数的返回值

## `0.7.0`  *2019/12/04*
协议
- 重新分析验证码包, 解决一些无法解析的情况. (这可能会产生新的问题, 遇到后请提交 issue)
- 重新分析提交密码包
- *提交验证码仍可能出现问题 (已在 `0.7.5` 修复)*

功能
- XML 消息 DSL 构造支持 (实验性) (暂不支持发送)
- 群成员列表现在包含群主 (原本就应该包含)
- 在消息事件处理中添加获取 `.qq()` 和 `.group()` 的扩展函数.
- 现在处理群消息时 sender 为 Member (以前为 QQ)
- 修改 `Message.concat` 为 `Message.followedBy`
- 修改成员权限 `OPERATOR` 为 `ADMINISTRATOR`
- **bot.subscribeAll<>() 等函数的 handler lambda 的 receiver 由 Bot 改变为 BotSession**; 此变动不会造成现有代码的修改, 但并不兼容旧版本编译的代码

性能优化
- 内联 ContactList
-  2 个 Contact.sendMessage 重载改为内联扩展函数 **(需要添加 import)**
- 其他小优化

## `0.6.1`  *2019/12/03*
- 新增: 无法解析密码包/验证码包时的调试输出. 以兼容更多的设备情况
- 新增: `MessagePacket` 下 `At.qq()` 捷径获取 QQ

## `0.6.0`  *2019/12/02*
- 新增: 禁言群成员 (`Member.mute(TimeSpan|Duration|MonthsSpan|Int|UInt)`)
- 新增: 解禁群成员 (`Member.unmute()`)
- 修复: ContactList key 无法匹配 (Kotlin 内联类型泛型投影错误)

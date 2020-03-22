# Major version 0

开发版本. 频繁更新, 不保证高稳定性

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
### mirai-core
- 修复上传图片
- 一些问题修复
- 大量杂项优化

### mirai-core-qqandroid
- `MessageReceipt.source` 现在为 public. 可获取源消息 id
- 修复上传好友图片失败的问题
- 上传群图片现在分包缓存, 优化性能

## `0.22.0` 2020/2/24
### mirai-core
- 重构 `MessageChain`, 引入 `CombinedMessage`. (兼容大部分原 API)
- 新增 `MessageChainBuilder`, `buildMessageChain`
- `ExternalImage` 现在接收多种输入参数

### mirai-core-qqandroid
- 修复访问好友消息回执 `.sequenceId` 时抛出异常的问题

## `0.21.0` 2020/2/23
- 支持好友消息的引用回复
- 更加结构化的 `QuoteReply` 架构, 支持引用任意群/好友消息回复给任意群/好友.

## `0.20.0` 2020/2/23

### mirai-core
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

### mirai-core
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

### mirai-core-qqandroid
- 修复一些情况下 `At` 无法发送的问题
- 统一 ImageId: 群消息收到的 ImageId 均为 `{xxxxxxxx-xxxx-xxxx-xxxxxxxxxxxx}.jpg` 形式（固定长度 37）
- 支持成员主动离开事件的解析 (#51)

## `0.18.0` 2020/2/20

### mirai-core
- 添加 `MessageSource.time`
- 添加事件监听时额外的 `coroutineContext`
- 为一些带有 `operator` 的事件添加 `.isByBot` 的属性扩展
- 优化事件广播逻辑, 修复可能无法触发监听的问题
- 为所有 `Contact` 添加 `toString()` (#80)

### mirai-core-qqandroid
- 支持成员禁言状态和时间查询 `Member.muteTimeRemaining`
- 修复 `At` 的 `display` (#73), 同时修复 `QuoteReply` 无法显示问题 (#54).
- 广播 `BotReloginEvent` (#78)
- 支持机器人自身禁言时间的更新和查询 (#82)

## `0.17.0` 2020/2/20

### mirai-core
- 支持原生表情 `Face`
- 修正 `groupCardOrNick` 为 `nameCardOrNick`
- 增加 `MessageChain.foreachContent(lambda)` 和 `Message.hasContent(): Boolean`

### mirai-core-qqandroid
- 提高重连速度
- 修复重连后某些情况不会心跳
- 修复收包时可能产生异常

## `0.16.0` 2020/2/19

### mirai-core
- 添加 `Bot.subscribe` 等筛选 Bot 实例的监听方法
- 其他一些小问题修复

### mirai-core-qqandroid
- 优化重连处理逻辑
- 确保好友消息和历史事件在初始化结束前同步完成
- 同步好友消息记录时不广播

## `0.15.5` 2020/2/19

### mirai-core
- 为 `MiraiLogger` 添加 common property `val isEnabled: Boolean`
- 修复 #62: 掉线重连后无 heartbeat
- 修复 #65: `Bot` close 后仍会重连
- 修复 #70: ECDH is not available on Android platform

### mirai-core-qqandroid
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

### mirai-core
- 尝试修复 `atomicfu` 编译错误的问题

### mirai-core-qqandroid
- 查询群信息失败后重试

## `0.15.1` 2020/2/15

### mirai-core
- 统一异常处理: 所有群成员相关操作无权限时均抛出异常而不返回 `false`.

### mirai-core-qqandroid
- 初始化未完成时缓存接收的所有事件包 (#46)
- 解析群踢人事件时忽略找不到的群成员
- 登录完成后广播事件 `BotOnlineEvent`

## `0.15.0` 2020/2/14

### mirai-core

- 新增事件: `BotReloginEvent` 和 `BotOfflineEvent.Dropped`
- `AtAll` 现在实现 `Message.Key`
- 新增 `BotConfiguration` DSL, 支持自动将设备信息存储在文件系统等
- 新增 `MessageSource.quote(Member)`

- 更好的网络层连接逻辑
- 密码错误后不再重试登录
- 掉线后尝试快速重连, 失败则普通重连 (#47)
- 有原因的登录失败时将抛出特定异常: `LoginFailedException`
- 默认心跳时间调整为 60s

### mirai-core-qqandroid

- 解决一些验证码无法识别的问题
- 忽略一些不需要处理的事件(机器人主动操作触发的事件)

## `0.14.0` 2020/2/13

### mirai-core

- **支持 at 全体成员: `AtAll`**

### mirai-core-qqandroid

- **支持 `AtAll` 的发送和解析**
- **修复某些情况下禁言处理异常**

小优化:
- 在 `GroupMessage` 添加 `quoteReply(Message)`, 可快速引用消息并回复
- 为 `CoroutineScope.subscribeMessages` 添加返回值. 返回 lambda 的返回值
- 在验证码无法处理时记录更多信息
- 优化 `At` 的空格处理 (自动为 `At` 之后的消息添加空格)
- 删除 `BotConfiguration` 中一些过时的设置

## `0.13.0` 2020/2/12

### mirai-core
- 修改 BotFactory, 添加 `context` 参数.
- currentTimeMillis 减少不必要对象创建
- 优化无锁链表性能 (大幅提升 `addAll` 性能)

### mirai-core-qqanroid
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
### mirai-core
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
### mirai-core
- 弃用 `BotAccount.id`. 将来它可能会被改名成为邮箱等账号. QQ 号码需通过 `bot.uin` 获取.
- `Gender` 由 `inline class` 改为 enum
- `String.chain()` 改为 `String.toChain()`
- `List<Message>.chain()` 改为 `List<Message>.toChain()`
### mirai-core-timpc
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

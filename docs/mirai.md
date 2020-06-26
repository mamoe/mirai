# mirai
欢迎来到 mirai 文档.

本文档基于 mirai 1.0.0, 最后修改时间为 2020/5/22

## 声明
1. mirai 为完全免费的开源项目，使用 AGPLv3 开源协议. mirai 项目维护者和贡献者 (下文简称 '我们') 不通过任何方式收取费用。所有人都可以免费获取并使用这个项目。
2. mirai 不允许被用于任何违反相关法律法规的工作, 包括但不限于: 盗取账号密码, 盗窃用户资金, 群发违禁内容。 因此我们没有支持设备锁验证, 也没有支持免密登录 (在非常用地点登录会频繁要求输入验证码)。
3. mirai 不提供任何可能促进上述违法行为的功能, 包括但不限于: 收发红包, 收发转账, 主动添加好友。
4. mirai 不公开任何协议接口. 不支持任何协议扩展.
5. mirai 开发者是友善的，建立在以下前提之上：
   - 我们只帮助**友善**的人
   - 我们只接受友善的**建议**，不接受任何**要求**
   - **我们没有任何义务解答你的问题**
   - 我们只接受友善的评价
   - 我们有权利拒绝你使用 mirai 相关服务


## 项目整体架构
mirai 项目整体由 核心 (`mirai-core`) 与 控制台(`mirai-console`) 组成.


- [`mirai-core`](../mirai-core) 是机器人服务支持**库**. 提供所有机器人相关 API. **本身只包含抽象类和接口, 使用时还需要同时依赖协议模块**.  
  可用的协议模块:
  - [`mirai-core-qqandroid`](../mirai-core-qqandroid): Android QQ 8.3.0 版本协议实现. 

  `mirai-core` 设计为一个 **`支持库`**, 意味着它可以被独立依赖, 在任意项目中使用. 详见下文.

- `mirai-serialization` 依赖 `mirai-core`, 是 mirai-core 的序列化支持模块. 提供 `Message` 类型的序列化支持与相关 [mirai 码](mirai-code-specification.md) 支持.  
  此模块自 mirai `1.1.0` 起可用, 引用方法同 `mirai-core`.

- [`mirai-console`](https://github.com/mamoe/mirai-console) 是基于 `mirai-core` 的, 支持插件加载, 指令系统, 和配置等的**控制台框架**.  
  **注意: 此模块正在重写, 短时间内不可用**  
  console 由 '后端' 和 '前端' 组成.  
  [`backend/mirai-console`](https://github.com/mamoe/mirai-console/tree/reborn/backend/mirai-console) 为唯一的后端, 包含所有开发时需要用到的功能.  
  可用的官方（由 mirai 所属 [mamoe](https://github.com/mamoe) 组成员维护）前端:  
  - 纯命令行: [`frontend/mirai-console-pure`](https://github.com/mamoe/mirai-console/tree/reborn/frontend/mirai-console-pure)
  - 基于 JavaFX 的图形端: [`frontend/mirai-console-graphical`](https://github.com/mamoe/mirai-console/tree/reborn/frontend/mirai-console-graphical)
  - Unix 友好的终端: [`frontend/mirai-console-terminal`](https://github.com/mamoe/mirai-console/tree/reborn/frontend/mirai-console-terminal)
  - Android APP: [`MiraiAndroid`](https://github.com/mzdluo123/MiraiAndroid) 额外支持多种脚本 API


**注意**: `mirai-core` 于 2020 年 5 月发布的 `1.0.0` 版本正式进入稳定更新阶段,   
**而 `mirai-console` 仍处于快速迭代阶段, 任何 API 都有可能在不经过警告的情况下改动, 任何 API 都不具有任何稳定性.**

## `mirai-core`

`mirai-core` 仅包含抽象类和接口和一些扩展方法, 且拥有非常完整的 KDoc (源码内文档). 此处没有必要过多赘述.

你可以在这里快速地大致了解 mirai 的全部 API.

### 准备

要能看懂下文, 建议至少学习 Java, Kotlin 或 C# 其中一门语言.

mirai 全部使用 Kotlin, 若你无法理解部分 API, 可先简略阅读 Kotlin 参考: [kotlincn.net](https://www.kotlincn.net/docs/reference/)  

有关协程 (`suspend` 修饰符)部分, mirai 做了大量的兼容性转换以让 Java 使用相同的 API 阻塞地调用一个协程函数。  
对于 Kotlin 函数如 `suspend fun sendMessage(msg: Message)`,  
Kotlin 编译后生成 Java 方法 `public Object sendMessage(Message msg, Continuation<Unit> cont)`  
Mirai 通过某种方式同时生成了桥梁方法 `public void sendMessage(Message msg)` 使得 Java 使用者可无缝接入。

建议在 IDE 内打开本文件 (位置 /docs/mirai.md), 可以进行源码内跳转.

### 开始

1. [实验性 API 注解 MiraiExperimentalAPI](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/Annotations.kt#L41)

2. '机器人' 和 '联系人'
   1. [ContactOrBot](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/ContactOrBot.kt)
   2. [机器人对象 Bot](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/Bot.kt)
   3. [Contact](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/Contact.kt)
   4. [用户对象 User](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/User.kt)
   5. [好友对象 Friend](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/Friend.kt)
   6. [群对象 Group](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/Group.kt)
   7. [群成员对象  Member](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/contact/Member.kt)

   总结: [机器人和联系人架构](../.github/机器人和联系人架构.png). 其中 `CoroutineScope` 为 Kotlin 协程作用域, Java 使用者可忽略.


3. '消息'
   1. [消息对象 Message](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Message.kt)  
      特别注意, `Message` 分为 单个消息(`SingleMessage`) 和 多个消息, 即消息链(`MessageChain` ).  
   2. [消息链 MessageChain](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/MessageChain.kt)  
      // TODO 此处还有更详细的扩展 API 解释, 待更新
   3. 接下来可按需阅读各类型消息 [各类型消息](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/). 一个文件包含一种消息.


4. '事件'  
   mirai 支持异步的事件系统.  
   1. [事件接口 Event](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/Event.kt)
   2. [广播事件 Event.broadcast](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/Event.kt)
   3. - Kotlin: [函数式监听事件 subscribe](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/subscriber.kt)
      - Kotlin & Java: [方法反射监听事件 JvmMethodListeners](../mirai-core/src/jvmMain/kotlin/net/mamoe/mirai/event/JvmMethodListeners.kt)
   4. 内建事件列表 [README](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/events/README.md).  
      **注意**: mirai 将接收到的消息事件独立放置在 `net.mamoe.mirai.message` 下, 并命名为 `MessageEvent`. 并为他们实现了一些扩展. 详见 [MessageEvent.kt](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/MessageEvent.kt)
   5. 事件工具类和工具函数 (仅 Kotlin) (可以跳过本节):  
      标注 (*) 代表需要比较好的 Kotlin 技能才能理解.
      - 挂起当前协程, 直到返回下一个事件实例: [nextEvent](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/nextEvent.kt)
      - 挂起当前协程, 并从一个事件中同步一个值: [syncFromEvent](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/linear.kt)
      - (*) 消息事件监听 DSL: [subscribeMessages](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/subscribeMessages.kt)
      - (*) 协程 `select` 语法的监听方式: [selectMessages](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/select.kt)
      - (*) 挂起协程并等待下一个与 `this` 语境相同的事件 [MessageEvent.nextMessage](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/utils.kt#L50)

<br><br>
<br><br>
一切准备就绪. 现在开始构造 `Bot` 实例:

1. `Bot` 的配置: [BotConfiguration](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/BotConfiguration.common.kt)
   可大致了解或跳过. 一般使用默认属性即可.
2. 构造 `Bot` 实例: [BotFactory](../mirai-core/src/jvmMain/kotlin/net/mamoe/mirai/BotFactory.kt#L23), [newBot](../mirai-core/src/jvmMain/kotlin/net/mamoe/mirai/BotFactory.kt#L53)

另外地, 你还可以了解 mirai 提供的多平台日志系统 (为了同时支持控制台和独立依赖): [MiraiLogger](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/utils/MiraiLogger.kt), 也可以跳过这个内容

### 使用

使用 mirai 作为一个依赖库: 阅读 [Quick Start](../docs/guide_quick_start.md) 配置依赖.  
使用 mirai 作为控制台框架: 使用 [mirai-console](https://github.com/mamoe/mirai-console) 开发插件

#### 第三方依赖

在 `1.0.0` 及更新版本, mirai-core 通过 gradle 依赖的 `api` 方式暴露了如下依赖库:

- `kotlin-stdlib`: Kotlin 标准库, 版本至少为 1.3.72
- `kotlin-reflect`: Kotlin 反射, 版本至少为 1.3.72
- `kotlinx-coroutines-core`: Kotlin 协程, 版本至少为 1.3.7
- `kotlinx-serialization-runtime`: Kotlin 序列化运行时, 和 JSON 序列化, 版本至少为 0.20.0
- `kotlinx-serialization-protobuf`: Kotlin ProtocolBuffers 序列化, 版本至少为 0.20.0
- `kotlinx-io`: Kotlin IO, 版本至少为 0.1.16 (此库非常不稳定, 不建议使用它)
- `kotlinx-coroutines-io`: Kotlin 异步 IO, 版本至少为 0.1.16 (此库非常不稳定, 不建议使用它)
- `kotlinx-atomicfu`: Kotlin 原子操作, 版本至少为 0.14.2
- `ktor-client-core`, `ktor-network`, `ktor-client-cio`: Ktor HTTP, 版本至少为 1.3.2

在 JVM, mirai 使用 `"org.bouncycastle:bcprov-jdk15on:1.64"` 进行密匙计算.

## `mirai-core-qqandroid`
`mirai-core` 的实现部分. 不提供任何说明. 使用者无需考虑任何协议实现过程.

## `mirai-console`
控制台框架。此模块处于实验性阶段, 可能没有很好地文档支持, 详见 [mirai-console](https://github.com/mamoe/mirai-console)

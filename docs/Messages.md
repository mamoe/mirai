# Mirai - Messages

## 目录
- [消息系统](#消息系统)
- [消息类型](#消息类型)
- [消息元素](#消息元素)
- [消息链](#消息链)
  - [发送消息](#发送消息)
  - [构造消息链](#构造消息链)
  - [元素唯一性](#元素唯一性)
  - [获取消息链中的消息元素](#获取消息链中的消息元素)
  - [序列化](#序列化)
  - [消息的其他常用功能](#消息的其他常用功能)
- [Mirai 码](#mirai-码)
  - [转义规则](#转义规则)
  - [消息链的 mirai 码](#消息链的-mirai-码)
  - [由 `CodableMessage` 取得 mirai 码字符串](#由-codablemessage-取得-mirai-码字符串)
  - [由 mirai 码字符串取得 `MessageChain` 实例](#由-mirai-码字符串取得-messagechain-实例)
  - [`serializeToMiraiCode` 与 `toString` 的区别](#serializetomiraicode-与-tostring-的区别)

## 消息系统

在 Contacts 章节提到，要发送消息，使用 `Contact.sendMessage(Message)`。`Message` 架构如下图所示。

[![](https://mermaid.ink/img/eyJjb2RlIjoiY2xhc3NEaWFncmFtXG5cbmNsYXNzIE1lc3NhZ2VDaGFpblxuTWVzc2FnZUNoYWluIDogTGlzdH5TaW5nbGVNZXNzYWdlflxuXG5NZXNzYWdlPHwtLU1lc3NhZ2VDaGFpblxuTWVzc2FnZTx8LS1TaW5nbGVNZXNzYWdlXG5cbk1lc3NhZ2VDaGFpbiBvLS0gU2luZ2xlTWVzc2FnZVxuXG5TaW5nbGVNZXNzYWdlPHwtLU1lc3NhZ2VDb250ZW50XG5TaW5nbGVNZXNzYWdlPHwtLU1lc3NhZ2VNZXRhZGF0YVxuXG4iLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlfQ)](https://mermaid-js.github.io/mermaid-live-editor/#/edit/eyJjb2RlIjoiY2xhc3NEaWFncmFtXG5cbmNsYXNzIE1lc3NhZ2VDaGFpblxuTWVzc2FnZUNoYWluIDogTGlzdH5TaW5nbGVNZXNzYWdlflxuXG5NZXNzYWdlPHwtLU1lc3NhZ2VDaGFpblxuTWVzc2FnZTx8LS1TaW5nbGVNZXNzYWdlXG5cbk1lc3NhZ2VDaGFpbiBvLS0gU2luZ2xlTWVzc2FnZVxuXG5TaW5nbGVNZXNzYWdlPHwtLU1lc3NhZ2VDb250ZW50XG5TaW5nbGVNZXNzYWdlPHwtLU1lc3NhZ2VNZXRhZGF0YVxuXG4iLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlfQ)

`SingleMessage` 表示单个消息元素。  
`MessageChain`（消息链） 是 `List<SingleMessage>`。主动发送的消息和从服务器接收消息都是 `MessageChain`。


> 回到 [目录](#目录)

## 消息类型

Mirai 支持富文本消息。

*单个消息元素（`SingleMessage`）* 分为 *内容（`MessageContent`）* 和 *元数据（`MessageMetadata`）*。

实践中，消息内容和消息元数据会混合存在于消息链中。

### 内容

*内容（`MessageContent`）* 即为 *纯文本*、*提及某人*、*图片*、*语音* 和 *音乐分享* 等**有内容**的数据，一条消息中必须包含内容才能发送。

### 元数据

*元数据（`MessageMetadata`）* 包含 *来源*、*引用回复* 和 *秀图标识* 等。

- *消息来源*（`MessageSource`）存在于每条消息中，包含唯一识别信息，用于撤回和引用回复的定位。
- *引用回复*（`QuoteReply`）若存在，则会在客户端中解析为本条消息引用了另一条消息。
- *秀图标识*（`ShowImageFlag`）若存在，则表明这条消息中的图片是以秀图发送（QQ 的一个功能）。

元数据与内容的区分就在于，一条消息没有元数据也能显示，但一条消息不能没有内容。**元数据是消息的属性**。

后文会介绍如何获取元数据。

> 回到 [目录](#目录)

## 消息元素

Mirai 支持多种消息类型。

消息拥有三种转换到字符串的表示方式。

| 方法                      | 解释                                                                                          |
|:-------------------------|:---------------------------------------------------------------------------------------------|
| `serializeToMiraiCode()` | 对应的 Mirai 码. 消息的一种序列化方式，格式为 `[mirai:TYPE:PROP]`，其中 `TYPE` 为消息类型, `PROP` 为属性 |
| `contentToSting()`       | QQ 对话框中以纯文本方式会显示的消息内容。无法用纯文字表示的消息会丢失信息，如任何图片都是 `[图片]`             |
| `toString()`             | Java 对象的 `toString()`，会尽可能包含多的信息用于调试作用，**行为可能不确定**                           |

各类型消息元素及其 `contentToString()` 如下表格所示。

[`MessageContent`]: ../mirai-core-api/src/commonMain/kotlin/message/data/SingleMessage.kt
[`MessageMetadata`]: ../mirai-core-api/src/commonMain/kotlin/message/data/SingleMessage.kt

[`PlainText`]: ../mirai-core-api/src/commonMain/kotlin/message/data/PlainText.kt
[`At`]: ../mirai-core-api/src/commonMain/kotlin/message/data/At.kt
[`AtAll`]: ../mirai-core-api/src/commonMain/kotlin/message/data/AtAll.kt
[`Face`]: ../mirai-core-api/src/commonMain/kotlin/message/data/Face.kt
[`PokeMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/PokeMessage.kt
[`VipFace`]: ../mirai-core-api/src/commonMain/kotlin/message/data/VipFace.kt
[`Image`]: ../mirai-core-api/src/commonMain/kotlin/message/data/Image.kt
[`FlashImage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/FlashImage.kt
[`MarketFace`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MarketFace.kt
[`MusicShare`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MusicShare.kt
[`Dice`]: ../mirai-core-api/src/commonMain/kotlin/message/data/Dice.kt
[`FileMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/FileMessage.kt

[`MessageSource`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MessageSource.kt
[`QuoteReply`]: ../mirai-core-api/src/commonMain/kotlin/message/data/QuoteReply.kt
[`LightApp`]: ../mirai-core-api/src/commonMain/kotlin/message/data/RichMessage.kt
[`SimpleServiceMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/RichMessage.kt
[`Voice`]: ../mirai-core-api/src/commonMain/kotlin/message/data/Voice.kt
[`ForwardMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/ForwardMessage.kt
[`ShowImageFlag`]: ../mirai-core-api/src/commonMain/kotlin/message/data/ShowImageFlag.kt
[`RichMessageOrigin`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MessageOrigin.kt
[`MessageOrigin`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MessageOrigin.kt


|  [`MessageContent`] 类型  | 解释                 | `contentToString()`     |      最低支持的版本      |
|:------------------------:|:--------------------|:------------------------|:---------------------:|
|      [`PlainText`]       | 纯文本               | `$content`              |          2.0          |
|        [`Image`]         | 自定义图片            | `[图片]`                 |          2.0          |
|          [`At`]          | 提及某人              | `@$target`              |          2.0          |
|        [`AtAll`]         | 提及全体成员           | `@全体成员`              |          2.0          |
|         [`Face`]         | 原生表情              | `[表情对应的中文名]`       |          2.0          |
|      [`FlashImage`]      | 闪照                 | `[闪照]`                 |          2.0          |
|     [`PokeMessage`]      | 戳一戳消息（消息非动作） | `[戳一戳]`               |          2.0          |
|       [`VipFace`]        | VIP 表情             | `[${kind.name}]x$count` |          2.0          |
|       [`LightApp`]       | 小程序               | `$content`              |          2.0          |
|        [`Voice`]         | 语音                 | `[语音消息]`              |          2.0          |
|      [`MarketFace`]      | 商城表情              | `[表情对应的中文名]`       |          2.0          |
|    [`ForwardMessage`]    | 合并转发              | `[转发消息]`             | 2.0  *<sup>(1)</sup>* |
| [`SimpleServiceMessage`] | （不稳定）服务消息      | `$content`              |          2.0          |
|      [`MusicShare`]      | 音乐分享              | `[分享]曲名`             |          2.1          |
|         [`Dice`]         | 魔法表情骰子           | `[骰子:$value]`         |          2.5          |
|     [`FileMessage`]      | 文件消息              | `[文件]文件名称`          |          2.5          |




| [`MessageMetadata`] 类型 | 解释         |     最低支持的版本     |
|:-----------------------:|:------------|:-------------------:|
|    [`MessageSource`]    | 消息来源元数据 |         2.0         |
|     [`QuoteReply`]      | 引用回复      |         2.0         |
|    [`ShowImageFlag`]    | 秀图标识      |         2.2         |
|  [`RichMessageOrigin`]  | 富文本消息源   | 2.3 *<sup>(2)</sup>* |
|    [`MessageOrigin`]    | 富文本消息源   |         2.6         |

> *(1):* [`ForwardMessage`] 在 2.0 支持发送, 在 2.3 支持接收  
> *(2):* [`RichMessageOrigin`] 在 2.3 增加, 在 2.6 弃用并以 [`MessageOrigin`] 替换


### 用法

只需要得到各种类型 `Message` 的实例就可以使用，可以直接发送（`Contact.sendMessage`）也可以连接到消息链中（`Message.plus`）。

可在上文表格中找到需要的类型并在源码内文档获取更多实践上的帮助。

> 回到 [目录](#目录)

## 消息链

[`MessageChain`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MessageChain.kt
[`SingleMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/SingleMessage.kt
[`CodableMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/code/CodableMessage.kt

前文已经介绍消息链，这里简略介绍消息链的使用。详细的使用请查看源码内注释。

### 发送消息

在 [Contacts 章节](Contacts.md) 提到，要发送消息使用 `Contact.sendMessage`。`Contact.sendMessage` 的定义是：
```kotlin
 suspend fun sendMessage(message: Message): MessageReceipt<Contact>
```

要发送字符串消息，使用：（第一部分是 Kotlin，随后是 Java，下同）
```kotlin
contact.sendMessage("Hello!")
```
```java
contact.sendMessage("Hello!");
```

发送字符串实际上是在发送纯文本消息。上面的代码相当于：
```kotlin
contact.sendMessage(PlainText("Hello!"))
```
```java
contact.sendMessage(new PlainText("Hello!"));
```

要发送多元素消息，可将消息使用 `plus` 操作连接：
```kotlin
contact.sendMessage(PlainText("你要的图片是") + Image("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png")) // 一个纯文本加一个图片
```
```java
contact.sendMessage(new PlainText("你要的图片是：").plus(Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"))); // 一个纯文本加一个图片
```

注: 当需要拼接的消息较多的时候, 建议使用 [构造消息链](#构造消息链) 而不是 `plus`
  - `plus` 在需要拼接的元素较多的时候运行效率较慢

### 构造消息链

更复杂的消息则需要构造为消息链。

#### 在 Kotlin 构造消息链

| 定义                                                     |
|:--------------------------------------------------------|
| `fun Iterable<Messaged>.toMessageChain(): MessageChain` |
| `fun Sequence<Messaged>.toMessageChain(): MessageChain` |
| `fun Array<Message>.toMessageChain(): MessageChain`     |
| `fun Message.toMessageChain(): MessageChain`            |
| `fun messageChainOf(vararg Message): MessageChain`      |
| `fun Message.plus(tail: Message): MessageChain`         |

可以使用如上表格所示的方法构造，或使用 DSL builder。
```kotlin
class MessageChainBuilder : MutableList<SingleMessage>, Appendable {
    operator fun Message.unaryPlus()
    operator fun String.unaryPlus()
    fun add(vararg messages: Message)
}
```

```kotlin
val chain = buildMessageChain {
    +PlainText("a")
    +AtAll
    +Image("/f8f1ab55-bf8e-4236-b55e-955848d7069f")
    add(At(123456)) // `+` 和 `add` 作用相同
}

// chain 结果是包含 PlainText, AtAll, Image, At 的 MessageChain
```

该示例中 `+` 是位于 `MessageChainBuilder` 的 `Message.unaryPlus` 扩展。使用 `+` 和使用 `add` 是相等的。

#### 在 Java 构造消息链

| 定义                                                                  |
|:---------------------------------------------------------------------|
| `public static MessageChain newChain(Iterable<Message> iterable)`    |
| `public static MessageChain newChain(Message iterable...)`           |
| `public static MessageChain newChain(Iterator<Message> iterable...)` |

方法都位于 `net.mamoe.mirai.message.data.MessageUtils`。

使用 `newChain`：
```java
MessageChain chain = MessageUtils.newChain(new PlainText("Hello"), Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"));
```

使用 `MessageChainBuilder`:
```java
MessageChain chain = new MessageChainBuilder()
    .append(new PlainText("string"))
    .append("string") // 会被构造成 PlainText 再添加, 相当于上一行
    .append(AtAll.INSTANCE)
    .append(Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"))
    .build();
```

### 作为字符串处理消息

通常要把消息作为字符串处理，在 Kotlin 使用 `message.content` 或在 Java 使用 `message.contentToString()`。

获取到的字符串表示只包含各 [`MessageContent`] 以官方风格显示的消息内容。如 `"你本次测试的成绩是[图片]"`、`[语音]`、`[微笑]`。

### 处理富文本消息

Mirai 不内置富文本消息的处理工具类。`MessageChain` 实现接口 `List<SingleMessage>`，一个思路是遍历 list 并判断类型处理：
```java
for (element : messageChain) {
    if (element instanceof Image) {
        // 处理一个 Image
    }
}
```
也可以像数组一样按下标随机访问：
```java
SingleMessage element = messageChain.get(0);
if (element instanceof Image) {
    // 处理一个 Image
}
```


### 元素唯一性

[`MessageKey`]: ../mirai-core-api/src/commonMain/kotlin/message/data/MessageKey.kt
[`ConstrainSingle`]: ../mirai-core-api/src/commonMain/kotlin/message/data/ConstrainSingle.kt
[`HummerMessage`]: ../mirai-core-api/src/commonMain/kotlin/message/data/HummerMessage.kt

部分元素只能单一存在于消息链中。这样的元素实现接口 [`ConstrainSingle`]。

唯一的元素例如 *消息元数据* [`MessageSource`]，在连接时，新的（右侧）元素会替换旧的（左侧）元素。如：
```kotlin
val source1: MessageSource
val source2: MessageSource

val chain: MessageChain = source1 + source2
// 结果 chain 只包含一个元素，即右侧的 source2。
```

元素唯一性的识别基于 [`MessageKey`]。有些 [`MessageKey`] 拥有多态机制。例如 [`HummerMessage`] 的继承关系
```
              MessageContent
                    ↑
              HummerMessage
                    ↑
       +------------+-------------+------------+
       |            |             |            |
 PokeMessage     VipFace      FlashImage      ...
```

当连接一个 [`VipFace`] 到一个 [`MessageChain`] 时，由于 [`VipFace`] 最远父类为 `MessageContent`，消息链中第一个 `MessageContent` 会被（保留顺序地）替换为 [`VipFace`]，其他所有 `MessageContent` 都会被删除。
```kotlin
// Kotlin

val face = VipFace(VipFace.AiXin, 1) // VipFace 是 ConstrainSingle
val chain = messageChainOf(plainText, quoteReply, at, atAll) // quoteReply 是 MessageMetadata, 其他三个都是 MessageContent
val result = chain + face // 右侧的 VipFace 替换掉所有的 MessageContent. 它会存在于第一个 MessageContent 位置.
// 结果为 [VipFace, QuoteReply]
```

```java
// Java

VipFace face = new VipFace(VipFace.AiXin, 1); // VipFace 是 ConstrainSingle
MessageChain chain = MessageChain.newChain(plainText, quoteReply, at, atAll); // quoteReply 是 MessageMetadata, 其他三个都是 MessageContent
MessageChain result = chain.plus(face); // 右侧的 VipFace 替换掉所有的 MessageContent. 它会存在于第一个 MessageContent 位置.
// 结果为 [VipFace, QuoteReply]
```

简单来说，这符合现实的逻辑：一条消息如果包含了语音，就不能同时包含群文件、提及全体成员、文字内容等元素。进行 `chain.plus(voice)` 时，如果消息内容冲突则会发生替换，且总是右侧元素替换左侧元素。  
而如果消息可以同时存在，比如一个纯文本和一个或多个图片相连 `plainText.plus(image1).plus(image2)` 时没有冲突，不会发生替换。结果将会是 `[PlainText, Image, Image]` 的 `MessageChain`。

### 获取消息链中的消息元素

#### A. 筛选 List
[`MessageChain`] 继承接口 `List<SingleMessage>`。
```kotlin
val image: Image? = chain.findIsInstance<Image>()
```
```java
Image image = (Image) chain.stream().filter(Image.class::isInstance).findFirst().orElse(null);
```

在 Kotlin 要获取第一个指定类型实例还可以使用快捷扩展。
```kotlin
val image: Image? = chain.findIsInstance<Image>()
val image: Image = chain.firstIsInstance<Image>() // 不存在时 NoSuchElementException
```

#### B. 获取唯一元素
如果要获取 `ConstrainSingle` 的消息元素，可以快速通过键获得。

```kotlin
val quote: QuoteReply? = chain[QuoteReply] // 类似 Map.get
val quote: QuoteReply = chain.getOrFail(QuoteReply) // 不存在时 NoSuchElementException
```
```java
QuoteReply quote = chain.get(QuoteReply.Key);
```

一些元数据就可以通过这个方法获得。如上述示例就是在获取引用回复（`QuoteReply`）。如果获取不为 `null` 则表明这条消息包含对其他某条消息的引用。

> 这是因为 `MessageKey` 一般都以消息元素的 `companion object` 实现

#### C. 使用属性委托

可在 Kotlin 使用属性委托。这样的方法与上述方法在性能上等价。

```kotlin
val image: Image by chain // 不存在时 NoSuchElementException
val image: Image? by chain.orNull()
val image: Image? by chain.orElse { /* 返回一个 Image */ }
```

#### D. 遍历 List
```java
for (SingleMessage message : messageChain) {
    // ...    
}
```

也可以使用 `messageChain.iterator()`。

### 序列化

> 简单地讲，序列化指的是将内存中的对象转换为其他易于存储等的表示方式。如对象变 JSON 字符串、对象变 MiraiCode 字符串。

消息可以序列化为 JSON 字符串，使用 `MessageChain.serializeToJsonString` 和 `MessageChain.deserializeFromJsonString`。

```java

String json = MessageChain.serializeToJsonString(message);

MessageChain chain = MessageChain.deserializeFromJsonString(message);

```

#### 使用 kotlinx.serialization

若要将消息类型使用在其他类型中，如
```kotlin
data class Foo(
    val image: Image
)
```

则需要在序列化时为 format 添加 `serializersModule`：
```kotlin
val json = Json {
    serializersModule = MessageSerializers.serializersModule
}
```

如果遇到 [`Encountered unknown key 'type'.`](https://github.com/mamoe/mirai/issues/1273#issuecomment-850997979) 等序列化错误，请添加：
```kotlin
Json {
    serializersModule = MessageSerializers.serializersModule
    ignoreUnknownKeys = true // 添加这一行
}
```

#### 元素类型不一定被保留

如 _消息源 `MessageSource`_ 有 _在线消息源 `OnlineMessageSource`_ 和 _离线消息源 `OfflineMessageSource`_ 之分。`OnlineMessageSource` 在序列化并反序列化后会变为 `OfflineMessageSource`，因为在线消息源只能从消息回执和消息事件中的 `MessageChain` 得到。

但在 API 使用上不会有区别（共有属性获取到的值不会变化）。只是可能需要注意 `equals` 等方法的使用。

所有 `Message` 反序列化的结果都是 `MessageChain`，即使原消息可能是 `SingleMessage`。如果原消息是 `SingleMessage`，可在反序列化后通过 `chian.get(0)` 下标访问并强转类型（或者其他更好的方法）。

#### 序列化稳定性

在旧版本产生的 JSON 字符串在绝大多数情况下可以由新版本 Mirai 读取并反序列化成原 `MessageChain`。如果更新时放弃了对某个旧版本消息元素的支持，将会在更新日志中说明。

但这个规则不适用于 _实验性特性_（带有 `@MiraiExperimentalApi` 注解）。任何使用了实验性特性的消息元素都随时可变。

### 消息的其他常用功能

#### 撤回自己或群员的消息

撤回的核心操作是 `MessageSource.recall`。 如果操作目标 `MessageSource` 指代的是自己的消息，那么就撤回该消息，操作群员消息同理。

来自 `GroupMessageEvent` 等消息事件的属性 `message` 的 `MessageChain` 都包含 `MessageSource` 元素，指代这条消息本身。那么就可以用来撤回这条消息。

```kotlin
// Kotlin
messageSource.recall()
messageChain.recall() // 获取其中 MessageSource 元素并操作 recall 

messageSource.recallIn(3000) // 启动协程, 3 秒后撤回消息. 返回的 AsyncRecallResult 可以获取结果
messageChain.recallIn(3000)
```

```java
// Java
MessageSource.recall(messageSource);
MessageSource.recall(messageChain); // 获取其中 MessageSource 元素并操作 recall 

MessageSource.recallIn(messageSource, 3000) // 启动异步任务, 3 秒后撤回消息. 返回的 AsyncRecallResult 可以获取结果
MessageSource.recallIn(messageChain, 3000)
```

#### 发送带有引用回复的消息

引用回复 `QuoteReply` 是一个元数据 `MessageMetadata`。将 `QuoteReply` 实例连接到消息链中，发送后的消息就会引用一条其他消息。

例如监听事件并回复一条群消息：

```kotlin
// Kotlin
bot.eventChannel.subscribeAlways<GroupMessageEvent> { // this: GroupMessageEvent
    subject.sendMessage(message.quote() + "Hi!") // 引用收到的消息并回复 "Hi!", 也可以添加图片等更多元素.
}
```

```java
// Java
bot.getEventChannel().subscribeAlways<GroupMessageEvent>(event -> {
    MessageChain chain = new MessageChainBuilder() // 引用收到的消息并回复 "Hi!", 也可以添加图片等更多元素.
        .append(new QuoteReply(event.getMessage()))
        .append("Hi!")
        .build();
    event.getSubject().sendMessage(chain);
});
```

#### 更多消息类型

在 [消息元素](#消息元素) 表格中找到你需要的消息元素，然后到源码内注释查看相应的用法说明。


## Mirai 码

实现了接口 `CodableMessage` 的消息类型支持 mirai 码表示。可以在[下文](#由-codablemessage-取得-mirai-码字符串)找到这些消息类型的列表。

### 转义规则

mirai 码内的属性字符串会被转义。

|   原字符    | 转义结果字符 |
|:----------:|:---------:|
|    `[`     |   `\[`    |
|    `]`     |   `\]`    |
|    `:`     |   `\:`    |
|    `,`     |   `\,`    |
|    `\`     |   `\\`    |
| *换行符 \n* |    `\n`    |
| *换行符 \r* |    `\r`    |

### 组成约定

一个有效的 mirai 码 (如 `[mirai:atall]` (无参数), `[mirai:at:123]` (有参数)) 可分为以下几个组成部分

- `[mirai:` 固定开头
- 消息类型， 如 `at`
- 消息参数
  - `:` 固定分隔符
  - 参数内容 **(需要进行转义)**
- `]` 固定结尾

#### 为何需要进行转义

为了 mirai 码的正确解析, 不转义无法正确解析原本意义

假如有以下参数

```
{"msg": [1, 2, 3]}
```

如果不进行转义直接进行 mirai 码拼接 (如: `[mirai:msg:{"msg": [1, 2, 3]}]`), 那么 mirai 码会被错误解析

> 解析结果如下:
>
> - mirai 码 `[mirai:msg:{"msg": [1, 2, 3]`
> - 纯文本 `}]`

### 消息链的 mirai 码

消息链 [`MessageChain`] 是多个 [`SingleMessage`] 的集合。[`MessageChain`] 也实现 [`CodableMessage`]。在转换为 mirai 码时所有 [`CodableMessage`] 直接相连：
```
val chain = messageChainOf(PlainText("plain"), At(123), AtAll)

chain.serializeToMiraiCode() // "plain[mirai:at:123][mirai:atall]"
```

### 由 `CodableMessage` 取得 mirai 码字符串

通过 `CodableMessage.serializeToMiraiCode()`。

```
val at = At(123)

at.serializeToMiraiCode() // 结果为 `[mirai:at:123]`
```

|          消息类型          | `serializeToMiraiCode()`                         |
|:------------------------:|:-------------------------------------------------|
|      [`PlainText`]       | `$content`                                       |
|        [`Image`]         | `[mirai:image:$imageId]`                         |
|          [`At`]          | `[mirai:at:$target]`                             |
|        [`AtAll`]         | `[mirai:atall]`                                  |
|         [`Face`]         | `[mirai:face:id]`                                |
|      [`FlashImage`]      | `[mirai:flash:${image.imageId}]`                 |
|     [`PokeMessage`]      | `[mirai:poke:$name,$pokeType,$id]`               |
|       [`VipFace`]        | `[mirai:vipface:${kind.id},${kind.name},$count]` |
|       [`LightApp`]       | `[mirai:app:$content]`                           |
| [`SimpleServiceMessage`] | `[mirai:service:$serviceId,$content]`            |
|         [`Dice`]         | `[mirai:dice:$value]`                            |
|      [`MusicShare`]      | `[mirai:musicshare:$args]`                       |
|     [`FileMessage`]      | `[mirai:file:$id,$internalId,$name,$size]`       |

### 由 mirai 码字符串取得 `MessageChain` 实例

```kotlin
val chain = "[mirai:atall]".deserializeMiraiCode()
```
```java
MessageChain chain = MiraiCode.deserializeFromMiraiCode("[mirai:atall]");
```

### 转义字符串

若要执行转义（一般没有必要手动这么做）：

```kotlin
PlainText("[mirai:atall]").serializeToMiraiCode() // \[mirai\:atall\]
```
```java
MiraiCode.serializeToMiraiCode(new PlainText("[mirai:atall]")) // \[mirai\:atall\]
```

### `serializeToMiraiCode` 与 `toString` 的区别


- 如 [消息元素](#消息元素) 所示, `toString()` 会尽可能包含多的信息用于调试作用，**行为可能不确定**
- `toString()` 偏人类可读, `serializeToMiraiCode()` 偏机器可读
- `toString()` **没有转义**, `serializeToMiraiCode()` 有正确转义
- `serializeToMiraiCode()` 会跳过 `不支持 mirai 码处理` 的元素
- `toString()` 基本不可用于 `deserializeMiraiCode`/`MiraiCode.deserializeFromMiraiCode`


---------

到这里，你已经完成了 Mirai 所有文档的阅读。现在你已经熟悉了 Mirai，并可以开始使用了。

你可以首先构造 Bot，登录，然后从监听事件起开始创建你的机器人，或从 Bot 获取到指定群主动发送消息。在使用中遇到问题可以参考 Mirai 源码内注释，该注释会包含更多实践上的帮助。

如果你仍然对 Mirai 架构有不明确的地方，欢迎在 [#848](https://github.com/mamoe/mirai/discussions/848) 提出建议，或者直接在 PR 提交你的修改。

> 回到 [目录](#目录)
>
> [回到 Mirai 文档索引](CoreAPI.md)

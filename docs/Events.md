# Mirai - Events

## 目录

- [事件系统](#事件系统)
- [快速指导](#快速指导)
- [事件通道](#事件通道)
- [通道操作](#通道操作)
  - [过滤](#过滤)
  - [添加 `CoroutineContext`](#添加-coroutinecontext)
  - [限制作用域](#限制作用域)
  - [链式调用](#链式调用)
- [在 `EventChannel` 监听事件](#在-eventchannel-监听事件)
- [监听事件的其他方法](#监听事件的其他方法)
  - [使用 `ListenerHost` 监听事件](#使用-eventhandler-注解标注的方法监听事件)
  - [在 Kotlin 使用 DSL 监听事件](#在-kotlin-使用-dsl-监听事件)
- [实现事件](#实现事件)
  * [新建事件](#新建事件)
  * [广播自定义事件](#广播自定义事件)
  * [监听自定义事件](#监听自定义事件)
  * [在 Console 中自定义事件](../mirai-console/docs/plugin/JVMPlugin-DataExchange.md)
- [工具函数（Kotlin）](#工具函数kotlin)
  - [线性同步（`syncFromEvent`）](#线性同步syncfromevent)
  - [线性同步（`nextEvent`）](#线性同步nextevent)
  - [条件选择（`selectMessages`）](#条件选择selectmessages)
  - [循环条件选择（`whileSelectMessages`）](#循环条件选择whileselectmessages)

## 事件系统

Mirai 许多功能都依赖事件。

[`Event`]: ../mirai-core-api/src/commonMain/kotlin/event/Event.kt#L21-L62

每个事件都实现接口 [`Event`]，且继承 `AbstractEvent`。  
实现 `CancellableEvent` 的事件可以被取消（`CancellableEvent.cancel`）。

**[事件列表](EventList.md)**

> 回到 [目录](#目录)


## 快速指导

如果你了解事件且不希望详细阅读，可以立即仿照下面示例创建事件监听并跳过本章节。

注意，**`GlobalEventChannel` 会监听到来自所有 `Bot` 的事件，如果只希望监听某一个 `Bot` 的事件，请使用 `bot.eventChannel`。**

有关消息 `Message`、`MessageChain` 将会在后文 _消息系统_ 章节解释。

### Kotlin

```kotlin
// 事件监听器是协程任务。如果你有 CoroutineScope，可从 scope 继承生命周期管理和 coroutineContext
GlobalEventChannel.parentScope(coroutineScope).subscribeAlways<GroupMessageEvent> { event ->
    // this: GroupMessageEvent
    // event: GroupMessageEvent
    
    // `event.message` 是接收到的消息内容, 可自行处理. 由于 `this` 也是 `GroupMessageEvent`, 可以通过 `message` 直接获取. 详细查阅 `GroupMessageEvent`.
    
    subject.sendMessage("Hello!")
}
// `GlobalEventChannel.parentScope(coroutineScope)` 也可以替换为使用扩展 `coroutineScope.globalEventChannel()`, 根据个人习惯选择



// 如果不想限制生命周期，可获取 listener 处理
val listener: CompletableJob =
    GlobalEventChannel.subscribeAlways<GroupMessageEvent> { event -> }

listener.complete() // 停止监听
```

异常默认会被相关 `Bot` 日志记录。可以在 `subscribeAlways` 之前添加如下内容来处理异常。

```
.exceptionHandler { e -> e.printStackTrace() }
```

**注意**：如果要在 Mirai Console 插件中监听事件，请不要使用使用无作用域控制的 `GlobalEventChannel`
，如 `GlobalEventChannel.subscribeAlways`
。请使用插件主类的扩展函数 `globalEventChannel()`
或者 `GlobalEventChannel.parentScope(scope)` 等方式控制监听器协程作用域。

### Java

```java
// 创建监听
Listener listener=GlobalEventChannel.INSTANCE.parentScope(scope).subscribeAlways(GroupMessageEvent.class,event->{
        MessageChain chain=event.getMessage(); // 可获取到消息内容等, 详细查阅 `GroupMessageEvent`

        event.getSubject().sendMessage("Hello!"); // 回复消息
        })

        listener.complete(); // 停止监听 
```

异常默认会被相关 `Bot` 日志记录。可以在 `subscribeAlways` 之前添加如下内容来处理异常。

```java
.exceptionHandler(e->e.printStackTrace())
```

**注意**：如果要在 Mirai Console 插件中监听事件，请不要使用使用无作用域控制的 `GlobalEventChannel`
，如 `GlobalEventChannel.subscribeAlways`
。请使用 `GlobalEventChannel.parentScope(PluginMain.INSTANCE)`
等方式控制监听器协程作用域。

> 你已经了解了基本事件操作。现在你可以继续阅读通道处理和扩展等内容，或：
>
> - 跳到下一章 [Messages](Messages.md)
> - [查看事件列表](EventList.md)
> - [回到事件文档目录](#目录)
> - [回到 Mirai 文档索引](CoreAPI.md)

## 事件通道

[`EventChannel`]: ../mirai-core-api/src/commonMain/kotlin/event/EventChannel.kt

[事件通道][`EventChannel`]是监听事件的入口。 **在不同的事件通道中可以监听到不同类型的事件**。

### 获取事件通道

[`GlobalEventChannel`]: ../mirai-core-api/src/commonMain/kotlin/event/GlobalEventChannel.kt

[`GlobalEventChannel`] 是最大的通道：所有的事件都可以在 [`GlobalEventChannel`] 监听到。**因此，[`GlobalEventChannel`] 会包含来自所有 `Bot` 实例的事件。**

通常不会直接使用 [`GlobalEventChannel`]，而是使用经过 [通道操作](#通道操作) 操作的子通道。

> 回到 [目录](#目录)

## 通道操作

`EventChannel` 可以通过一些操作转换。

**一个通道的属性都是*不变的*：每个转换操作都会创建一个新的通道而不会修改原通道。**

### 过滤

`GlobalEventChannel` 包含任何 `Event`，可以通过 `EventChannel.filter` 过滤得到一个只包含期望的事件的 `EventChannel`。

```kotlin
var channel = GlobalEventChannel.filter { it is BotEvent && it.bot.id == 123456L } // 筛选来自某一个 Bot 的事件
```
```java
EventChannel channel = GlobalEventChannel.INSTANCE.filter(ev -> ev instanceof BotEvent && ((BotEvent) ev).bot.id == 123456); // 筛选来自某一个 Bot 的事件
```

> 回到 [通道操作](#通道操作)

> 你可以选择跳过下文介绍的协程属性和作用域，直接阅读 [在 `EventChannel` 监听事件](#在-eventchannel-监听事件)

### 添加 `CoroutineContext`

一个通道持有属性 `defaultCoroutineContext`，将会自动添加给每个事件监听器（见后文）。

可以为通道添加一些 `CoroutineContext`，如 `CoroutineExceptionHandler`（用于处理监听时产生的异常）。
```kotlin
channel.exceptionHandler { exception ->
    logger.error(exception)
} 
```

```java
channel.exceptionHandler(exception ->
    logger.error(exception);
);
```

这本质上是添加了一个 `CoroutineExceptionHandler`。之后当事件监听器出现异常，异常就会被传递到这个 `CoroutineExceptionHandler` 处理。

> 回到 [通道操作](#通道操作)


### 限制作用域

在监听时会创建一些*事件监听器*。*事件监听器*本质上是一个协程 *`Job`*，因此可以有父 `Job`。

要指定父 `Job`，请使用 `parentJob` 或 `parentScope` 操作。
```kotlin
// parentScope
channel = channel.parentScope(MyApplicationScope)

// parentJob
val job = SupervisorJob()
channel = channel.parentJob(job)
```

在 Kotlin，可以使用如下扩展快速在 GlobalEventChannel 创建一个指定协程作用域下的事件通道。
> ```kotlin
>  fun CoroutineScope.globalEventChannel(coroutineContext: CoroutineContext = EmptyCoroutineContext): EventChannel<Event> = GlobalEventChannel.parentScope(this, coroutineContext)
> ```

```kotlin
val channel = MyApplicationScope.globalEventChannel()
```


作用域限制对于应用生命周期管理会很有用。请看如下 Mirai Console 插件示例。
```kotlin
object MyPluginMain : KotlinPlugin() { // KotlinPlugin 实现了 CoroutineScope

    // 插件被启用时调用
    override fun onEnable() {
    
        // `this` 是插件的协程作用域
        // 在插件协程作用域里创建事件监听。当插件被停用时，插件的协程作用域也会被关闭，事件监听就会被同步停止。
        this.globalEventChannel().subscribeAlways<MessageEvent> { event ->
            // 处理事件 
        }
    }
}
```

> 有关限制作用域的实现细节，可在使用时阅读源码内文档。

### 链式调用

对通道的操作都会返回 `this`，因此可以链式调用。
```kotlin
val channel = GlobalEventChannel
    .filterIsInstance<BotEvent>()
    .filter { it.bot.id == 123456L }
    .filter { /* some other conditions */ }
    .parentScope(MyApplicationScope)
    .exceptionHandler { exception ->
        exception.printStacktrace()
    }
```

> 回到 [通道操作](#通道操作)
> 回到 [目录](#目录)

## 在 `EventChannel` 监听事件

使用：
- `EventChannel.subscribe`：监听事件并自行决定何时停止
- `EventChannel.subscribeAlways`：一直监听事件
- `EventChannel.subscribeOnce`：只监听一次事件

```kotlin
bot.eventChannel.subscribeAlways<GroupMessageEvent> { event ->
    // this: GroupMessageEvent
    // event: GroupMessageEvent
    
    subject.sendMessage("Hello from mirai!")
}
```

```java
bot.eventChannel.subscribeAlways(GroupMessageEvent.class, event -> {
    event.getSubject().sendMessage("Hello from mirai!");
})
```

> 实现细节可查看源码内注释。


> 回到 [目录](#目录)

## 监听事件的其他方法

监听都需要在*事件通道*中进行。如下几种方法都本质上会调用上述 `EventChannel.subscribe` 等方法。

- [使用 `@EventHandler` 注解标注的方法监听事件](#使用-eventhandler-注解标注的方法监听事件)
- [在 Kotlin 使用 DSL 监听事件](#在-kotlin-使用-dsl-监听事件)

### 使用 `@EventHandler` 注解标注的方法监听事件

标注一个函数（方法）为事件监听器。mirai 通过反射获取他们并为之注册事件。

- [Kotlin 函数](#kotlin-函数)
- [Java 方法](#java-方法)

#### Kotlin 函数

Kotlin 函数要求:
- 接收者和函数参数: 所标注的 Kotlin 函数必须至少拥有一个接收者或一个函数参数, 或二者都具有. 接收者和函数参数的类型必须相同 (如果二者都存在)
  接收者或函数参数的类型都必须为 `Event` 或其子类.

所有 Kotlin 非 `suspend` 的函数都将会在 `Dispatchers.IO` 中调用

支持的函数类型:
```kotlin
// 所有函数参数, 函数返回值都不允许标记为可空 (带有 '?' 符号)
// T 表示任何 Event 类型.
suspend fun T.onEvent(T)
suspend fun T.onEvent(T): ListeningStatus
suspend fun T.onEvent(T): Nothing
suspend fun onEvent(T)
suspend fun onEvent(T): ListeningStatus
suspend fun onEvent(T): Nothing
suspend fun T.onEvent()
suspend fun T.onEvent(): ListeningStatus
suspend fun T.onEvent(): Nothing
fun T.onEvent(T)
fun T.onEvent(T): ListeningStatus
fun T.onEvent(T): Nothing
fun onEvent(T)
fun onEvent(T): ListeningStatus
fun onEvent(T): Nothing
fun T.onEvent()
fun T.onEvent(): ListeningStatus
fun T.onEvent(): Nothing
```

Kotlin 使用示例:

- 独立 `CoroutineScope` 和 `ListenerHost`
```kotlin
object MyEvents : ListenerHost {
    override val coroutineContext = SupervisorJob()
    // 可以抛出任何异常, 将在 this.coroutineContext 或 registerEvents 时提供的 CoroutineScope.coroutineContext 中的 CoroutineExceptionHandler 处理.
    @EventHandler
    suspend fun MessageEvent.onMessage() {
        reply("received")
    }
}
eventChannel.registerListenerHost(MyEvents)
```
`onMessage` 抛出的异常将会交给 `myCoroutineScope` 处理

- 合并 `CoroutineScope` 和 `ListenerHost`: 使用 `SimpleListenerHost`
```kotlin
object MyEvents : SimpleListenerHost( /* override coroutineContext here */ ) {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // 处理 onMessage 中未捕获的异常
    }
    @EventHandler
    suspend fun MessageEvent.onMessage() { // 可以抛出任何异常, 将在 handleException 处理
        reply("received")
        // 无返回值 (或者返回 Unit), 表示一直监听事件.
    }
    @EventHandler
    suspend fun MessageEvent.onMessage(): ListeningStatus { // 可以抛出任何异常, 将在 handleException 处理
        reply("received")
        return ListeningStatus.LISTENING // 表示继续监听事件
        // return ListeningStatus.STOPPED // 表示停止监听事件
    }
}
eventChannel.registerListenerHost(MyEvents)
```


#### Java 方法

所有 Java 方法都会在 `Dispatchers.IO` 中调用，因此在 Java 也可以调用阻塞方法。

支持的方法类型：
```
// T 表示任何 Event 类型.
void onEvent(T)
Void onEvent(T)
ListeningStatus onEvent(T) // 禁止返回 null
```

Java 使用示例:

```java
public class MyEventHandlers extends SimpleListenerHost {
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception){
        // 处理事件处理时抛出的异常
    }
    @EventHandler
    public void onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        event.getSubject().sendMessage("received");
        // 无返回值, 表示一直监听事件.
    }
    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        event.getSubject().sendMessage("received");
        return ListeningStatus.LISTENING; // 表示继续监听事件
        // return ListeningStatus.STOPPED; // 表示停止监听事件
    }
}

// 注册：
// eventChannel.registerListenerHost(new MyEventHandlers())
```

> 回到 [监听事件的其他方法](#监听事件的其他方法)

### 在 Kotlin 使用 DSL 监听事件
> **警告：此节内容需要坚实的 Kotlin 技能，盲目使用会导致问题**

[subscribeMessages](../mirai-core-api/src/commonMain/kotlin/event/subscribeMessages.kt#L37-L64)

示例：
```kotlin
eventChannel.subscribeMessages {
    "test" {
        // 当消息内容为 "test" 时执行
        // this: MessageEvent
        reply("test!")
    }
    
    "Hello" reply "Hi" // 当消息内容为 "Hello" 时回复 "Hi"
    "quote me" quoteReply "ok" // 当消息内容为 "quote me" 时引用该消息并回复 "ok"
    "quote me2" quoteReply {
        // lambda 也是允许的：
        // 返回值接受 Any? 
        // 为 Unit 时不发送任何内容；
        // 为 Message 时直接发送；
        // 为 String 时发送为 PlainText；
        // 否则 toString 并发送为 PlainText
        
        "ok" 
    } 
    
    case("iGNorECase", ignoreCase=true) reply "OK" // 忽略大小写
    startsWith("-") reply { cmd ->
        // 当消息内容以 "-" 开头时执行
        // cmd 为消息去除开头 "-" 的内容
    }
    
    
    val listener: Listener<MessageEvent> = "1" reply "2"
    // 每个语句都会被注册为事件监听器，可以这样获取监听器
    
    listener.complete() // 停止 "1" reply "2" 这个事件监听
}
```

> 回到 [目录](#目录)

## 实现事件

相信你在使用 mirai 自带的事件时已经感到受益匪浅了，这种机制也可以作用在你的程序上，让其他人的程序也能像监听 mirai 自带的事件一样，对你程序的行为作出反应。

### 新建事件

新建一个类，让类实现接口 `Event` 并继承 `AbstractEvent` 即可。根据内部实现，事件必须继承 `AbstractEvent`，如下示例。
```kotlin
// kotlin:
class ExampleEvent(
    var action: String
) : AbstractEvent()
```
```java
// java:
public class ExampleEvent extends AbstractEvent {
    String action;
    public ExampleEvent(String action) {
        this.action = action;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
}
```
### 广播自定义事件

事件需要被广播，才会被监听器接收到，使已监听事件的程序作出响应。以上文的 `ExampleEvent` 为例，

kotlin：`event.broadcast()`

java：`EventKt.broadcast(Event)`

> 注: 在 kotlin 中进行事件的广播需要在协程上下文执行

```kotlin
// kotlin:
var event = ExampleEvent("some action")
var finalAction = event.broadcast().action
println("action = $finalAction")
```
```java
// java:
ExampleEvent event = new ExampleEvent("some action");
String finalAction = EventKt.broadcast(event).getAction();
System.out.println("action = " + finalAction);
```
### 监听自定义事件

同上文监听事件的方式几乎一样。不过需要注意的是，从 bot 获取的消息通道 (`bot.eventChannel`)，只能监听 `BotEvent`，如果你的事件类没有实现 `BotEvent`，将无法通过这个通道来监听此事件。因此你可能需要使用 `GlobalEventChannel` 来代替 `bot.eventChannel`。

以下的示例是 监听事件以影响上一个部分 `广播自定义事件` 中的变量 `action` 的值。

```kotlin
// kotlin:
GlobalEventChannel.subscribeAlways<ExampleEvent> { event ->
    // 处理事件
    if (event.action == "some action") {
        event.action = "another action"
    }
}
```
```java
// java:
GlobalEventChannel.INSTANCE.subscribeAlways(ExampleEvent.class, event -> { 
    // 处理事件
    if (event.getAction().equals("some action")) {
        event.setAction("another action");
    }
});
```

### 在 Console 中自定义事件

请参考 [Console - JVMPlugin - Data Exchange](../mirai-console/docs/plugin/JVMPlugin-DataExchange.md)

> 回到 [目录](#目录)

## 工具函数（Kotlin）
> *可能需要较好的 Kotlin 技能才能理解以下内容。*
> **可以[跳过本节](#　)**

基于 Kotlin 协程特性，mirai 提供 `

### 线性同步（`syncFromEvent`）
[syncFromEvent.kt](../mirai-core-api/src/commonMain/kotlin/event/syncFromEvent.kt)

挂起协程并获取下一个戳 Bot 的对象：
```kotlin
val target: UserOrBot = syncFromEvent<BotNudgedEvent> { sender }
```

带超时版本：
```kotlin
val target: UserOrBot = syncFromEvent<BotNudgedEvent>(5000) { sender } // 5000ms
```

异步 `async` 版本：

```kotlin
val target: Deferred<UserOrBot> = coroutineScope.asyncFromEvent<BotNudgedEvent> { sender }
```

### 线性同步（`nextEvent`）
[nextEvent.kt](../mirai-core-api/src/commonMain/kotlin/event/nextEvent.kt)

挂起协程并获取下一个指定事件：

```kotlin
val event: BotNudgedEvent = nextEvent<BotNudgedEvent>()
```

带超时和过滤器版本：

```kotlin
val event: BotNudgedEvent = nextEvent<BotNudgedEvent>(5000) { it.bot.id == 123456L }
```

### 条件选择（`selectMessages`）
> **警告：此节内容需要坚实的 Kotlin 技能，盲目使用会导致问题**

[select.kt](../mirai-core-api/src/commonMain/kotlin/event/select.kt)

类似于 Kotlin 协程 `select`，mirai 也提供类似的功能。

`selectMessages`：挂起当前协程，等待任意一个事件监听器触发后返回其返回值。

```kotlin
MyCoroutineScope.subscribeAlways<GroupMessageEvent> {
    if (message.contentEquals("ocr")) {
        subject.sendMessage("请发送你要进行 OCR 的图片或图片链接")
        val image: InputStream = selectMessages {
            has<Image> { URL(it.queryUrl()).openStream() }
            has<PlainText> { URL(it.content).openStream() }
            defaultReply { "请发送图片或图片链接" }
            timeout(30_000) { event.quoteReply("请在 30 秒内发送图片或图片链接"); null }
        } ?: return@subscribeAlways
        
        val result = ocr(image)
        subject.sendMessage(message.quote() + result)
    }
}
```

这种语法就相当于（伪代码）：
```
val image = when (下一条消息) {
   包含图片 { 查询图片链接() } 
   包含纯文本URL { 下载图片() }
   其他情况 { 引用回复() }
   超时 { 引用回复() }
}
```

### 循环条件选择（`whileSelectMessages`）
> **警告：此节内容需要坚实的 Kotlin 技能，盲目使用会导致问题**

[select.kt](../mirai-core-api/src/commonMain/kotlin/event/select.kt)

类似于 Kotlin 协程 `whileSelect`，mirai 也提供类似的功能。

`whileSelectMessages`：挂起当前协程，等待任意一个事件监听器返回 `false` 后返回。

```kotlin
subject.sendMessage("开启复读模式")
whileSelectMessages {
    "stop" {
        subject.sendMessage("已关闭复读")
        false // 停止循环
    }
    // 也可以使用 startsWith("") { ... } 等 DSL
    default {
        subject.sendMessage(message)
        true // 继续循环
    }
    timeout(3000) {
        // on
        true
    }
} // 等待直到 `false`
subject.sendMessage("复读模式结束")
```

> 回到 [目录](#目录)

###### 　

----

> 下一步，[Messages](Messages.md)
>
> [回到 Mirai 文档索引](CoreAPI.md)

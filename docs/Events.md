# Mirai - Events

## 目录

- [事件系统](#事件系统)
- [事件通道](#事件通道)
- [通道操作](#通道操作)
  - [缩窄（过滤）](#缩窄过滤)
  - [添加 `CoroutineContext`](#添加-coroutinecontext)
  - [限制作用域](#限制作用域)
  - [链式调用](#链式调用)
- [在 `EventChannel` 监听事件](#在-eventchannel-监听事件)
- [监听事件的其他方法](#监听事件的其他方法)
  - [使用 `ListenerHost` 监听事件](#使用-eventhandler-注解标注的方法监听事件)
  - [在 Kotlin 使用 DSL 监听事件](#在-kotlin-使用-dsl-监听事件)
- [实现事件](#实现事件)
- [工具函数（Kotlin）](#工具函数kotlin)
  - [线性同步（`syncFromEvent`）](#线性同步syncfromevent)
  - [线性同步（`nextEvent`）](#线性同步nextevent)
  - [条件选择（`selectMessages`）](#条件选择selectmessages)
  - [循环条件选择（`whileSelectMessages`）](#循环条件选择whileselectmessages)

## 事件系统

Mirai 以事件驱动，使用者需要监听如 `收到消息`，`收到入群申请` 等事件。

[`Event`]: ../mirai-core-api/src/commonMain/kotlin/event/Event.kt#L21-L62

每个事件都实现接口 [`Event`]，且继承 `AbstractEvent`。  
实现 `CancellableEvent` 的事件可以被取消（`CancellableEvent.cancel`）。

**[事件列表](../mirai-core-api/src/commonMain/kotlin/event/events/README.md#事件)**

> 回到 [目录](#目录)

## 事件通道

[`EventChannel`]: ../mirai-core-api/src/commonMain/kotlin/event/EventChannel.kt

[事件通道][`EventChannel`]是监听事件的入口。 **在不同的事件通道中可以监听到不同类型的事件**。

> 对通道的转换操作可以在使用时查看源码内注释 ([`EventChannel`])。

### 获取事件通道

`GlobalEventChannel` 是最大的通道：所有的事件都可以在 `GlobalEventChannel` 监听到。**因此，`GlobalEventChannel` 会包含来自所有 `Bot` 实例的事件。**

通常不会直接使用 `GlobalEventChannel`，而是使用经过 [通道操作](#通道操作) 操作的子通道。

> 回到 [目录](#目录)

## 通道操作

`EventChannel` 可以通过一些操作转换。

**一个通道的属性都是*不变的*：每个转换操作都会创建一个新的通道而不会修改原通道。**

### 缩窄（过滤）

`GlobalEventChannel` 包含任何 `Event`，可以通过 `EventChannel.filter` 过滤得到一个只包含期望的事件的 `EventChannel`。

```kotlin
var channel = GlobalEventChannel.filter { it is BotEvent && it.bot.id == 123456L } // 筛选来自某一个 Bot 的事件
```
```java
EventChannel channel = GlobalEventChannel.INSTANCE.filter(ev -> ev instanceof BotEvent && ((BotEvent) ev).bot.id == 123456); // 筛选来自某一个 Bot 的事件
```

> 回到 [通道操作](#通道操作)

### 添加 `CoroutineContext`

一个通道持有属性 `defaultCoroutineContext`，将会自动添加给每个事件监听器（见后文）。

可以为通道添加一些 `CoroutineContext`，如 `CoroutineExceptionHandler`。
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
- `EventChannel.subscribe`：监听事件并自行觉得何时停止
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

> 有关监听事件的实现细节可在使用时查看源码内注释。


> 回到 [目录](#目录)

## 监听事件的其他方法

监听都需要在*事件通道*中进行。如下几种方法都本质上会调用上述 `EventChannel.subscribe` 等方法。

- [使用 `ListenerHost` 监听事件](#使用-eventhandler-注解标注的方法监听事件)
- [在 Kotlin 使用 DSL 监听事件](#在-kotlin-使用-dsl-监听事件)

### 使用 `@EventHandler` 注解标注的方法监听事件

标注一个函数（方法）为事件监听器。mirai 通过反射获取他们并为之注册事件。

> 详见 [EventHandler](../mirai-core-api/src/commonMain/kotlin/event/JvmMethodListeners.kt#L22-L144)

- [Kotlin 函数](#kotlin-函数)
- [Java 方法](#java-方法)

#### Kotlin 函数

Kotlin 函数要求:
- 接收者 (英 receiver) 和函数参数: 所标注的 Kotlin 函数必须至少拥有一个接收者或一个函数参数, 或二者都具有. 接收者和函数参数的类型必须相同 (如果二者都存在)
  接收者或函数参数的类型都必须为 `Event` 或其子类.
- 返回值: 为 `Unit` 或不指定返回值时将注册为 `EventChannel.subscribeAlways`, 为 `ListeningStatus` 时将注册为 `EventChannel.subscribe`.
  任何其他类型的返回值将会在注册时抛出异常.

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

所有 Java 方法都会在 `Dispatchers.IO` 中调用，因此在 Java 可以调用阻塞方法。

支持的方法类型：
```
// T 表示任何 Event 类型.
void onEvent(T)
Void onEvent(T)
ListeningStatus onEvent(T) // 返回 null 时将抛出异常
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
        event.subject.sendMessage("received");
        // 无返回值, 表示一直监听事件.
    }
    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        event.subject.sendMessage("received");
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

只要实现接口 `Event` 并继承 `AbstractEvent` 的对象就可以被广播。

要广播一个事件，使用 `Event.broadcast()`（Kotlin）或 `EventKt.broadcast(Event)`（Java）。

> 回到 [目录](#目录)

## 工具函数（Kotlin）

*可能需要较好的 Kotlin 技能才能理解以下内容。*

基于 Kotlin 协程特性，mirai 提供 `

### 线性同步（`syncFromEvent`）
[linear.kt](../mirai-core-api/src/commonMain/kotlin/event/linear.kt)

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
        reply("请发送你要进行 OCR 的图片或图片链接")
        val image: InputStream = selectMessages {
            has<Image> { URL(it.queryUrl()).openStream() }
            has<PlainText> { URL(it.content).openStream() }
            defaultReply { "请发送图片或图片链接" }
            timeout(30_000) { event.quoteReply("请在 30 秒内发送图片或图片链接"); null }
        } ?: return@subscribeAlways
        
        val result = ocr(image)
        quoteReply(result)
    }
}
```

这种语法就相当于（伪代码）：
```
val image = when (下一条消息) {
   包含图片 { 查询图片链接() } 
   包含纯文本 { 下载图片() }
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
reply("开启复读模式")
whileSelectMessages {
    "stop" {
        reply("已关闭复读")
        false // 停止循环
    }
    // 也可以使用 startsWith("") { true } 等 DSL
    default {
        reply(message)
        true // 继续循环
    }
    timeout(3000) {
        // on
        true
    }
} // 等待直到 `false`
reply("复读模式结束")
```

> 回到 [目录](#目录)


> 下一步，[Messages](Messages.md)
>
> [回到 Mirai 文档索引](README.md)

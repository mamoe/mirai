# Mirai - Events

## 目录

- [事件系统](#事件系统)
- [监听事件](#监听事件)
  - [使用 `ListenerHost` 监听事件](#使用-listenerhost-监听事件)
  - [在 Kotlin 函数式监听](#在-kotlin-函数式监听)
- [实现事件](#实现事件)
- [工具函数（Kotlin）](#工具函数kotlin)


## 事件系统

Mirai 以事件驱动，使用者需要监听如 `收到消息`，`收到入群申请` 等事件。

[`Event`]: ../mirai-core-api/src/commonMain/kotlin/event/Event.kt#L21-L62

每个事件都实现接口 [`Event`]，且继承 `AbstractEvent`。  
实现 `CancellableEvent` 的事件可以被取消（`CancellableEvent.cancel`）。

[事件列表](../mirai-core-api/src/commonMain/kotlin/event/events/README.md#事件)

## 监听事件

有两种方法监听事件：
- [使用 `ListenerHost` 监听事件](#使用-listenerhost-监听事件)
- [在 Kotlin 函数式监听](#在-kotlin-函数式监听)

### 使用 `ListenerHost` 监听事件

标注一个函数（方法）为事件监听器。mirai 通过反射获取他们并为之注册事件。

> 详见 [EventHandler](../mirai-core-api/src/commonMain/kotlin/event/JvmMethodListeners.kt#L27-L168)

#### Kotlin 函数

Kotlin 函数要求:
- 接收者 (英 receiver) 和函数参数: 所标注的 Kotlin 函数必须至少拥有一个接收者或一个函数参数, 或二者都具有. 接收者和函数参数的类型必须相同 (如果二者都存在)
  接收者或函数参数的类型都必须为 `Event` 或其子类.
- 返回值: 为 `Unit` 或不指定返回值时将注册为 `CoroutineScope.subscribeAlways`, 为 `ListeningStatus` 时将注册为 `CoroutineScope.subscribe`.
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
myCoroutineScope.registerEvents(MyEvents)
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
MyEvents.registerEvents()
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
// Events.registerEvents(new MyEventHandlers())
```

### 在 Kotlin 函数式监听
[CoroutineScope.subscribe](../mirai-core-api/src/commonMain/kotlin/event/subscriber.kt#L137-L220)

用法示例：
```kotlin
object MyApplication : CoroutineScope by CoroutineScope(SupervisorJob())

// 启动事件监听器
MyApplication.subscribeAlways<GroupMessageEvent> {
    // this: GroupMessageEvent
    // it: GroupMessageEvent
    // lambda 的 this 和参数都是 GroupMessageEvent

    group.sendMessage(sender.at() + "Hello! ${sender.nick}") 
}

// YouApplication[Job]!!.cancel() // 
```

Mirai 也支持传递函数引用：
```kotlin
suspend fun GroupMessageEvent.onEvent() {
    group.sendMessage(sender.at() + "Hello! ${sender.nick}") 
}

MyApplication.subscribeAlways<GroupMessageEvent>(GroupMessageEvent::onEvent)
```
既可以使用接收者参数，又可以使用普通参数，还可以同时拥有。如下三个定义都是被接受的：
```kotlin
suspend fun GroupMessageEvent.onEvent() 
suspend fun GroupMessageEvent.onEvent(event: GroupMessageEvent) 
suspend fun onEvent(event: GroupMessageEvent) 
```

### 在 Kotlin 使用 DSL 监听事件
> **警告：此节内容需要坚实的 Kotlin 技能，盲目使用会导致问题**

[subscribeMessages](../mirai-core-api/src/commonMain/kotlin/event/subscribeMessages.kt#L37-L64)

示例：
```kotlin
MyApplication.subscribeMessages {
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

## 实现事件

只要实现接口 `Event` 并继承 `AbstractEvent` 的对象就可以被广播。

要广播一个事件，使用 `Event.broadcast()`（Kotlin）或 `EventKt.broadcast(Event)`（Java）。

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


> 下一步，[Messages](Messages.md)
>
> [回到 Mirai 文档索引](README.md)
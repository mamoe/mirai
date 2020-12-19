# Mirai - Events

## 目录
- [使用 `ListenerHost` 监听事件](#使用-listenerhost-监听事件)
- [在 Kotlin 函数式监听](#在-kotlin-函数式监听)
- [事件列表](#事件列表)

## 事件系统

Mirai 以事件驱动，使用者需要监听如 `收到消息`，`收到入群申请` 等事件。

有两种方法监听事件：
- [使用 `ListenerHost` 监听事件](#使用-listenerhost-监听事件)
- [在 Kotlin 函数式监听](#在-kotlin-函数式监听)

## 使用 `ListenerHost` 监听事件

标注一个函数（方法）为事件监听器。mirai 通过反射获取他们并为之注册事件。

> 详见 [EventHandler](../mirai-core-api/src/commonMain/kotlin/event/JvmMethodListeners.kt#L27-L168)

### Kotlin 函数

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
### Java 方法

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

## 在 Kotlin 函数式监听
[CoroutineScope.subscribe](../mirai-core-api/src/commonMain/kotlin/event/subscriber.kt#L137-L220)

用法示例：
```kotlin
object YourApplication : CoroutineScope by CoroutineScope(SupervisorJob())

// 启动事件监听器
YourApplication.subscribeAlways<GroupMessageEvent> {
    // this: GroupMessageEvent
    // it: GroupMessageEvent
    // lambda 的 this 和参数都是 GroupMessageEvent

    group.sendMessage(sender.at() + "Hello! ${sender.nick}") 
}

// YouApplication[Job]!!.cancel() // 
```

Mirai 也支持传递函数引用：
```kotlin
YourApplication.subscribeAlways<GroupMessageEvent> {
    // this: GroupMessageEvent
    // it: GroupMessageEvent
    // lambda 的 this 和参数都是 GroupMessageEvent

    group.sendMessage(sender.at() + "Hello! ${sender.nick}") 
}
```

## 事件列表

[事件列表](../mirai-core-api/src/commonMain/kotlin/event/events/README.md#事件)
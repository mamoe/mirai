# Mirai - Creating Bots

## 获取 `Bot` 实例

一个机器人被以 `Bot` 对象描述。mirai 的交互入口点是 `Bot`。`Bot` 只可通过 [`BotFactory`](../mirai-core-api/src/commonMain/kotlin/BotFactory.kt#L22-L87) 内的 `newBot` 方法获得：

```kotlin
interface BotFactory {
    fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot
    fun newBot(qq: Long, password: String): Bot
    fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot
    fun newBot(qq: Long, passwordMd5: ByteArray): Bot
    
    companion object : BotFactory by BotFactoryImpl
}
```

通常的调用方法为：
```
// Kotlin
val bot = BotFactory.newBot(    )

// Java
Bot bot = BotFactory.INSTANCE.newBot(    );
```

### 获取当前所有 `Bot` 实例
在登录后，`Bot` 实例会被自动记录。可在 `Bot.instances` 获取到当前在线的所有 `Bot` 列表。当 `Bot` 离线，其实例就会被删除。

## 登录

创建 `Bot` 后不会自动登录，需要手动调用其 `login()` 方法。在 Kotlin 还可以使用 `Bot.alsoLogin()` 扩展，相当于 `bot.apply { login() }`

### 重新登录

可以再次调用 `Bot.login()` 以重新登录 `Bot`。这一般没有必要——`Bot` 运行时会自动与服务器同步事件（如群成员变化，好友数量变化）。


> 下一步，[Contacts](Contacts.md)
> [回到 Mirai 文档索引](README.md)

# Mirai - Bots

**目录**

- [1. 创建和配置 `Bot`](#1-创建和配置-bot)
- [2. 登录](#2-登录)

## 1. 创建和配置 `Bot`

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

### 配置 Bot
可以切换使用的协议、控制日志输出等。

仅能在构造 Bot 时修改其配置：
```
// Kotlin
val bot = BotFactory.newBot(qq, password) {
    // 配置，例如：
    fileBasedDeviceInfo()
}

// Java
Bot bot = BotFactory.INSTANCE.newBot(qq, password, new BotConfiguration() {{
    // 配置，例如：
    fileBasedDeviceInfo()
}})
```

下文示例代码都要放入 `// config` 中。


### 常用配置
> 可在 [BotConfiguration.kt](../mirai-core-api/src/commonMain/kotlin/utils/BotConfiguration.kt#L23) 查看完整配置列表

#### 设备信息
Bot 默认使用全随机的设备信息。在更换账号地点时候使用随机设备信息可能会导致无法登录。

要使用 `deviceInfo.json` 存储设备信息：
```
fileBasedDeviceInfo() // "myDeviceInfo.json" 
fileBasedDeviceInfo("myDeviceInfo.json") // "myDeviceInfo.json"
```

要自定义设备信息：
```
// Kotlin
deviceInfo = { bot ->  /* create device info */   }

// Java
setDeviceInfo(bot -> /* create device info */)
```

在线生成自定义设备信息的 `device.json`: https://ryoii.github.io/mirai-devicejs-generator/

#### 切换登录协议
Mirai 支持多种登录协议：`ANDROID_PHONE`，`ANDROID_PAD`，`ANDROID_WATCH`，默认使用 `ANDROID_PHONE`。

若登录失败，可尝试切换协议。但注意，部分功能在部分协议上不受支持，详见源码内注释。

要切换协议：
```
// Kotlin
protocol = BotConfiguration.MiraiProtocol.ANDROID_PAD

// Java
setProtocol(MiraiProtocol.ANDROID_PAD)
```

#### 重定向日志
Bot 有两个日志类别，`Bot` 或 `Net`。`Bot` 为通常日志，如收到事件。`Net` 为网络日志，包含收到和发出的每一个包和网络层解析时遇到的错误。

重定向日志到文件：
```
redirectBotLogToFile()
redirectNetworkLogToFile()
```

手动覆盖日志：
```
// Kotlin
networkLoggerSupplier = { bot -> /* create logger */ }
botLoggerSupplier = { bot -> /* create logger */ }

// Java
setNetworkLoggerSupplier(bot -> /* create logger */)
setBotLoggerSupplier(bot -> /* create logger */)
```

#### 覆盖登录解决器
在遇到验证码时，Mirai 会寻找 `LoginSolver` 以解决验证码。

- 在 Android 需要手动提供 `LoginSolver`
- 在 JVM, Mirai 会根据环境支持情况选择 Swing/CLI 实现

覆盖默认的 `LoginSolver`：
```
// Kotlin
loginSolver = YourLoginSolver

// Java
setLoginSolver(new YourLoginSolver())
```

要获取更多有关验证码解决器的信息，查看 [LoginSolver.kt](../mirai-core-api/src/commonMain/kotlin/utils/LoginSolver.kt#L32)

### 获取当前所有 `Bot` 实例

在登录后，`Bot` 实例会被自动记录。可在 `Bot.instances` 获取到当前在线的所有 `Bot` 列表。当 `Bot` 离线，其实例就会被删除。

## 2. 登录

创建 `Bot` 后不会自动登录，需要手动调用其 `login()` 方法。在 Kotlin 还可以使用 `Bot.alsoLogin()` 扩展，相当于 `bot.apply { login() }`。

### 处理滑动验证码

由于服务器正在大力推广滑块验证码，登录时可能需要解决滑动验证码。部分账号可以跳过滑块验证码直接重新登录，服务器就会要求图片验证码。

处理验证码需要浏览器支持，可在 [project-mirai/mirai-login-solver-selenium](https://github.com/project-mirai/mirai-login-solver-selenium) 查看详细处理方案。

### 常见登录失败原因

| 错误信息       | 可能的原因 | 可能的解决方案           |
|:--------------|:---------|:----------------------|
| 当前版本过低    | 密码错误   | 检查密码               |
| 当前上网环境异常 | 设备锁    | 开启或关闭设备锁后重试登录 |

若以上方案无法解决问题，请尝试 [切换登录协议](#切换登录协议) 和 **[处理滑动验证码](#处理滑动验证码)**。

### 重新登录

可以再次调用 `Bot.login()` 以重新登录 `Bot`。这一般没有必要——`Bot` 运行时会自动与服务器同步事件（如群成员变化，好友数量变化）。


> 下一步，[Contacts](Contacts.md)
>
> [回到 Mirai 文档索引](README.md)

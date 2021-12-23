# mirai-core-mock

mirai 模拟环境测试框架

> 模拟环境目前仅支持 JVM

--------------

# 在非 console 中进行模拟

## 环境准备

需要先添加 `net.mamoe:mirai-core-mock` 依赖

```kotlin
depencencies {
    testApi("net.mamoe:mirai-core-mock:$VERSION")
}
```

并在启动前运行以下的代码

```kotlin
internal fun main() {
    MockBotFactory.initialize()
    // .....
}
```

> 注:
> - **不支持**同时运行模拟环境和真实环境
> - **不支持**从模拟环境切换回真实环境

## 创建 Bot

我们建议使用 `MockBotFactory.newMockBotBuilder()` 创建新的 `MockBot`.

也可以使用原始 `BotFactory` 来创建一个新的 `MockBot`, 将会使用默认的缺省值填充相关的信息

关于 `MockBot` 可以在 [这里](https://github.com/mamoe/mirai/tree/dev/mirai-core-mock/test/mock)
找到 mirai-core-mock 的相关用法

------

# 在 console 中进行模拟

WIP

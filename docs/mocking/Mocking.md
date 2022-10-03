# Mirai - Mocking

本章节介绍 mirai 模拟环境

> mirai 模拟环境从 `2.13` 开始支持
>
> 注:
> - **不支持**同时运行模拟环境和真实环境
> - **不支持**从模拟环境切换回真实环境

-----------------------------------

# 在非 console 中进行模拟

## 环境准备

要使用 mirai 模拟环境测试框架, 首先需要额外添加一项依赖

```kotlin
dependencies {
    testImplementation("net.mamoe:mirai-core-mock:$VERSION")
}
```

并在本地的测试入口添加以下的代码

```kotlin
internal fun main() {
    MockBotFactory.initialize()
    // .....
}
```

## 创建 Bot

对于创建 `MockBot`, 更好的方法是使用 `MockBotFactory.newMockBotBuilder()`

也可以使用原始的 `BotFactory` 来创建一个新的 `MockBot`, 系统会使用默认值填充相关的信息

## 使用

关于 `MockBot` 可以在 [这里](https://github.com/mamoe/mirai/tree/dev/mirai-core-mock/test/mock)
找到 mirai-core-mock 的相关用法

可以在 [DslTest.kt](https://github.com/mamoe/mirai/blob/dev/mirai-core-mock/test/DslTest.kt)
中找到关于 mirai-core-mock DSL 的用法

----------------

# 在 console 中进行模拟

Work In Progress...

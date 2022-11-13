# mirai-core - jvm test - debug run

-------------

!! IMPORTANT !!

在 `jvmTest` 直接启动 `mirai-core` 前, 您必须先阅读此篇的内容

否则你可能会遇到一些问题

-----------------------------------

## 测试启动点配置

在直接启动前, 首先需要手动创建一个专属于自己本地测试的测试启动点

> mirai-core 自带 `jvmTest/kotlin/local` 的忽略, 您只需要在 `jvmTest/kotlin` 手动创建此文件夹即可

在 `jvmTest/kotlin/local` 创建一个新的 kotlin 文件, 并写入以下内容

```kotlin
fun main() {
    prepareEnvironmentForDebugRun()
    // .....

    val bot = DebugRunHelper.newBot(/* ..... */) {
    }
    bot as QQAndroidBot

    runBlocking {
        bot.login()

        bot.eventChannel.subscribeAlways<MessageEvent> {
            //......
        }
        bot.join()
    }
}
```

---------------

## 测试数据存储

Intellij IDEA 直接以默认配置运行程序时, 程序的工作目录都是顶层根目录 (`$rootProject/`)

即在测试代码中的文件相关的操作都是相对 `$rootProject` 而言的

`$rootProject/test` 在 `$rootProject/.gitignore` 中被声明完全忽略

所以 `$rootProject/test` 可以用于存储测试数据, 如果目前本地环境中没有 `test` 文件夹, 可以手动创建

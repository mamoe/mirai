# Mirai Guide - Subscribe Events

由于Mirai项目在快速推进中，因此内容时有变动，本文档的最后更新日期为`2020-04-01`，对应版本`0.31.4`

本页面采用Kotlin作为开发语言，**若你希望使用 Java 开发**, 请参阅: [mirai-japt](https://github.com/mamoe/mirai-japt)

本页面是[Mirai Guide - Getting Started](/docs/guide_getting_started.md)的后续Guide

## 消息事件-Message Event

首先我们来回顾上一个Guide的源码

```kotlin
suspend fun main() {
    val qqId = 10000L//Bot的QQ号，需为Long类型，在结尾处添加大写L
    val password = "your_password"//Bot的密码
    val miraiBot = Bot(qqId, password).alsoLogin()//新建Bot并登录
    miraiBot.subscribeMessages {
        "你好" reply "你好!"
        case("at me") {
            reply(sender.at() + " 给爷爬 ")
        }

        (contains("舔") or contains("刘老板")) {
            "刘老板太强了".reply()
        }
    }
    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}
```

在本例中，`miraiBot`是一个Bot对象，让其登录，然后对`Message Event`进行了监听。

对于`Message Event`，`Mirai`提供了较其他Event更强大的[MessageSubscribersBuilder](https://github.com/mamoe/mirai/wiki/mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/MessageSubscribers.kt#L140)，本例也采用了[MessageSubscribersBuilder](https://github.com/mamoe/mirai/wiki/mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/MessageSubscribers.kt#L140)。其他具体使用方法可以参考[Wiki:Message Event](https://github.com/mamoe/mirai/wiki/Development-Guide---Kotlin#Message-Event)部分。

## 事件-Event

上一节中提到的`Message Event`仅仅是众多`Event`的这一种，其他`Event`有：群员加入群，离开群，私聊等等...

具体事件文档暂不提供，可翻阅源码[**BotEvents.kt**](https://github.com/mamoe/mirai/blob/master/mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/events/BotEvents.kt)，查看注释。当前事件仍在扩充中，可能有一定不足。

下面我们开始示例对一些事件进行监听。

## 尝试监听事件-Try Subscribing Events

### 监听加群事件

在代码中的`miraiBot.join()`前添加

```kotlin
miraiBot.subscribeAlways<MemberJoinEvent> {
    it.group.sendMessage(PlainText("欢迎 ${it.member.nameCardOrNick} 加入本群！"))
}
```

本段语句监听了加入群的事件。

### 监听禁言事件

在代码中添加

```kotlin
miraiBot.subscribeAlways<MemberMuteEvent> {
    it.group.sendMessage(PlainText("恭喜老哥 ${it.member.nameCardOrNick} 喜提禁言套餐一份"))
}
```

在被禁言后，Bot将发送恭喜语句。

### 添加后的可执行代码

至此，当前的代码为

```kotlin
package net.mamoe.mirai.simpleloader

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText

suspend fun main() {
    val qqId = 10000L//Bot的QQ号，需为Long类型，在结尾处添加大写L
    val password = "your_password"//Bot的密码
    val miraiBot = Bot(qqId, password).alsoLogin()//新建Bot并登录
    miraiBot.subscribeMessages {
        "你好" reply "你好!"
        case("at me") {
            reply(sender.at() + " 给爷爬 ")
        }

        (contains("舔") or contains("刘老板")) {
            "刘老板太强了".reply()
        }
    }
    miraiBot.subscribeAlways<MemberJoinEvent> {
        it.group.sendMessage(PlainText("欢迎 ${it.member.nameCardOrNick} 加入本群！"))
    }
    miraiBot.subscribeAlways<MemberMuteEvent> {
        it.group.sendMessage(PlainText("恭喜老哥 ${it.member.nameCardOrNick} 喜提禁言套餐一份"))
    }
    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}
```

下面可以参阅[Mirai Guide - Build For Mirai](/docs/guide_build_for_mirai.md)，对你的Mirai应用进行打包

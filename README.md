# Mirai
[![HitCount](http://hits.dwyl.io/him188/mamoe/mirai.svg)](http://hits.dwyl.io/him188/mamoe/mirai) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7d0ec3ea244b424f93a6f59038a9deeb)](https://www.codacy.com/manual/Him188/mirai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mamoe/mirai&amp;utm_campaign=Badge_Grade)

一个以 **TIM PC协议(非web)** 驱动的跨平台QQ机器人服务端核心, 虽然目前仅支持 JVM  
采用服务端-插件模式运行，同时提供独立的跨平台核心库.  
Mirai 的所有模块均开源
  
项目处于开发阶段, 还有很多未完善的地方. 欢迎任何的代码贡献, 或是 issue.  
部分协议来自网络上开源项目  
**一切开发旨在学习，请勿用于非法用途**

## 抢先体验  
核心框架结构已经开发完毕，一些核心功能也测试完成。  
仅需几分钟就可以测试 Mirai.  
现在你可以登录小号来测试 Mirai.  
即使测试消息时未发现冻结情况，我们也无法100%保证账号冻结不会发生。

目前还没有写构建，请使用 IDE 运行单个 main 函数。
1. Clone
2. Import as Gradle project
3. Run demo main [Demo 1 Main](mirai-demos/mirai-demo-1/src/main/java/demo1/Main.kt#L22)

### 事件

#### 使用 Kotlin
这里只演示进行不终止地监听。
##### Top-level reified
多数情况下这是最好的方式。
```kotlin
inline fun <reified E: Event> subscribeAlways(handler: (E) -> Unit)

subscribeAlways<FriendMessageEvent>{
  //it: FriendMessageEvent
}
```

##### DSL
查看更多: [ListenerBuilder](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/event/Subscribers.kt#L87)
```kotlin
inline fun <reified E: Event> subscribeAll(builder: ListenerBuilder.() -> Unit)

subscribe<FriendMessageEvent>{
  always{
    //it: FriendMessageEvent
    //coroutineContext: EventScope.coroutineContext
  }
  //可同时开始多个监听。
  always{
    //it: FriendMessageEvent
    //coroutineContext: EventScope.coroutineContext
  }
}
```

![AYWVE86P](.github/A%7DYWVE860U%28%25YQD%24R1GB1%5BP.png)

### 图片测试
现在可以接收图片消息(并解析为消息链):  
![JsssF](.github/J%5DCE%29IK4BU08%28EO~UVLJ%7B%5BF.png)  
![](.github/68f8fec9.png)

发送图片已经完成，但我们还在开发上传图片至服务器。  
机器人可以转发图片消息.详情查看 [Image.kt](mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/Message.kt#L81)

## 现已支持

- 发送好友/群消息(10/14)
- 接受解析好友消息(10/14)
- 接收解析群消息(10/14)
  - 成员权限, 昵称(10/18)
- 好友在线状态改变(10/14)
- Android客户端上线/下线(10/18)

## 使用方法
### 要求
  - Kotlin 1.3+
#### 用于 JVM 平台
 - Java 8
## 插件开发
``` text
    to be continued
    ...
```

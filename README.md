# Mirai
[![HitCount](http://hits.dwyl.io/him188/mamoe/mirai.svg)](http://hits.dwyl.io/him188/mamoe/mirai)

一个以<b>TIM QQ协议(非web)</b>驱动的JAVA(+Kotlin) QQ机器人服务端核心  
采用服务端-插件模式运行，同时提供独立的协议层库  
**我们承诺项目的所有模块均开源**  
  
项目处于开发阶段，学生无法每日大量更新。  
项目还有很多未完善的地方, 欢迎任何的代码贡献, 或是 issue.   
部分协议来自网络上开源项目  
一切开发旨在学习，请勿用于非法用途  

## 抢先体验  
核心框架结构已经开发完毕，一些核心功能也测试完成。  
仅需几分钟就可以测试 Mirai.  
现在你可以登录小号来测试 Mirai.  
即使测试消息时未发现冻结情况，我们也无法100%保证账号冻结不会发生。

1. Clone
2. Import as Maven project
3. Run [MiraiMain](mirai-core/src/main/java/net/mamoe/mirai/MiraiMain.java#L7)

### 事件 Hook
#### Java:
```
MiraiEventHook.onEvent(FriendMessageEvent.class)
       .handler(a -> {
               if(a.message.eq("你好")) 
                       a.getSender().sendMessage("你好！");
       })
       .mountAlways();
```
#### Kotlin:
```
FriendMessageEvent::class.hookAlways{
    if(it.message eq "你好")
          it.reply("你好！")
}
```
![AYWVE86P](.github/A%7DYWVE860U%28%25YQD%24R1GB1%5BP.png)

### 图片测试
**现在可以接受图片消息**(并解析为消息链):  
![JsssF](.github/J%5DCE%29IK4BU08%28EO~UVLJ%7B%5BF.png)  
![](.github/68f8fec9.png)

发送图片已经完成，但我们还在开发上传图片至服务器。  
现在你可以通过发送一张图片给机器人账号，再让机器人账号发送这张图片。你可以查看 [Image](src/main/java/net/mamoe/mirai/message/Image.kt)

## 语言使用说明
我们使用 Kotlin，但我们也会保留对 Java 和 Java开发者的支持。

# TODO
- [x] 事件(Event)模块  
- [ ] 插件(Plugin)模块  **(Working on)**
- [x] Network - Touch  
- [X] Network - Login
- [X] Network - Session  
- [X] Network - Verification Code
- [X] Network - Message Receiving  
- [X] Network - Message Sending  
- [ ] Network - Events
- [ ] Bot - Friend/group list
- [ ] Bot - Actions(joining group, adding friend, etc.)
- [x] Message Section
- [ ] Image uploading **(Working on)**
- [ ] Contact  
- [ ] UI
- [ ] Console

<br>

# 使用方法
## 要求
- Java 11 或更高
- Kotlin 1.3 或更高
## 插件开发
``` text
    to be continued
    ...
```

# Mirai

<br>

A JAVA(+Kotlin) powered open-source project under GPL license<br>
It use protocols from <i>TIM QQ</i>, that is, it won't be affected by the close of <i>Smart QQ</i><br>
The project is all for <b>learning proposes</b> and still in <b>developing stage</b><br>

# Usage
## Requirements
- Java 11 or higher
- Kotlin 1.3 or higher
## Plugin Development
``` text
    to be continued
    ...
```





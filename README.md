# Mirai
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7d0ec3ea244b424f93a6f59038a9deeb)](https://www.codacy.com/manual/Him188/mirai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mamoe/mirai&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Actions Status](https://github.com/mamoe/mirai/workflows/CI/badge.svg)](https://github.com/mamoe/mirai/actions)
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
**[English](README-eng.md)**  

跨平台 **TIM PC 和 QQ Android** 协议支持库.   
纯 Kotlin 实现协议和支持框架，模块全部开源。  
目前可运行在 JVM 或 Android。

**一切开发旨在学习，请勿用于非法用途**  

加入 Gitter, 或加入 QQ 群: 655057127

## Update log
在 [Project](https://github.com/mamoe/mirai/projects/1) 查看已支持功能和计划（更新不及时）   
在 [UpdateLog](https://github.com/mamoe/mirai/blob/master/UpdateLog.md) 查看版本更新记录（准确更新发布的版本）   

## Modules
#### mirai-core 
通用 API 模块，一套 API 适配两套协议。  
**请参考此模块的 API**  
  
#### mirai-core-timpc 
TIM PC （2.3.2 版本，2019 年 8 月）协议的实现，相较于 core，仅新增少量 API. 详见 [README.md](mirai-core-timpc/)   
支持的功能： 
- 消息收发：图片文字复合消息，图片消息
- 群管功能：群员列表，禁言

（目前不再更新此协议，请关注下文的安卓协议）

#### mirai-core-qqandroid 
QQ for Android （8.2.0 版本，2019 年 12 月）协议的实现，目前还未完成。   
- 高兼容性：Mirai 协议仅含极少部分为硬编码，其余全部随官方方式动态生成
- 高安全性：密匙随机，ECDH 动态计算，硬件信息真机模拟（Android 平台获取真机信息）

开发进度：  
- 完成 密码登录 （2020/1/23）
- 完成 群消息解析 (2020/1/25）
- 完成 图片验证码登录 (2020/1/26）
- 完成 设备锁登录 (2020/1/29)
- 进行中 消息解析和发送
- 进行中 图片上传和下载

## Use directly
**直接使用 Mirai(终端环境/网页面板（将来））.**  
[Mirai-Console](https://github.com/mamoe/mirai/tree/master/mirai-console) 插件支持, 在终端中启动 Mirai 并获得机器人服务  
本模块还未完善。

## Use as a library
**mirai-core 为独立设计, 可以作为库内置于任意 Java(JVM)/Android 项目中使用.**   

### Gradle
Mirai 只发布在 `jcenter`, 因此请确保在 `build.gradle` 添加 `jcenter()` 仓库：  
```kotlin
repositories{
  jcenter()
}
```
若您需要使用在跨平台项目, 则要对各个目标平台添加不同的依赖，这与 kotlin 相关跨平台库的依赖是类似的。   
**若您只需要使用在单一平台, 则只需要添加一项该平台的依赖. 如只在 JVM 运行则只需要`-jvm`的依赖**  

请将 `VERSION` 替换为最新的版本(如 `0.10.6`):
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
**Mirai 目前还处于实验性阶段, 我们无法保证任何稳定性, API 也可能会随时修改.**

**注意：**
Mirai 核心由 API 模块（`mirai-core`）和协议模块组成。  
只添加 API 模块将无法正常工作。  
现在只推荐使用 TIMPC 协议，请参照下文选择对应目标平台的依赖添加。

**common**
```kotlin
implementation("net.mamoe:mirai-core-timpc-common:VERSION")
```
**jvm**
```kotlin
implementation("net.mamoe:mirai-core-timpc-jvm:VERSION")
```
**android**
```kotlin
implementation("net.mamoe:mirai-core-timpc-android:VERSION")
```
### Performance
Android 上, Mirai 运行需使用 80M 内存.  
JVM 上需 120M-150M 内存

## Contribution

我们 (Mamoe, NaturalHG & Him188) 将会一直维护这个项目，除非遇到不可抗力因素。

我们欢迎一切形式的贡献。  
我们也期待有更多人能加入 Mirai 的开发。  

若在使用过程中有任何疑问, 可提交 issue 或是邮件联系(support@mamoe.net). 我们希望 Mirai 变得更易用.

您的 star 是对我们最大的鼓励(点击项目右上角);  

## Wiki
在 [Wiki](https://github.com/mamoe/mirai/wiki/Development-Guide---Kotlin) 中查看各类帮助，**如 API 示例**（可能过时，待 QQ Android 协议完成后会重写）。

## Try

### On JVM or Android
现在体验低付出高效率的 Mirai

```kotlin
val bot = TIMPC.Bot(qqId, password).alsoLogin()
bot.subscribeMessages {
  "你好" reply "你好!"
  "profile" reply { sender.queryProfile() }
  contains("图片"){ File(imagePath).send() }
}
bot.subscribeAlways<MemberPermissionChangedEvent> {
  if (it.kind == BECOME_OPERATOR)
    reply("${it.member.id} 成为了管理员")
}
```

我们也考虑到了 Java 兼容的问题，这正在计划中，但不是高优先的。

1. Clone
2. Import as Gradle project
3. 运行 Demo 程序: [mirai-demo](#mirai-demo) 示例和演示程序


## Build Requirements

- Kotlin 1.3.61 
- JDK 8 (required)
- JDK 11（for protocol tools, optional）
- Android SDK 29 (for Android target, optional)

#### Libraries used
感谢:
- [kotlin-stdlib](https://github.com/JetBrains/kotlin)
- [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [kotlinx-io](https://github.com/Kotlin/kotlinx-io)
- [kotlin-reflect](https://github.com/JetBrains/kotlin)
- [pcap4j](https://github.com/kaitoy/pcap4j)
- [atomicfu](https://github.com/Kotlin/kotlinx.atomicfu)
- [ktor](https://github.com/ktorio/ktor)
- [tornadofx](https://github.com/edvin/tornadofx)
- [javafx](https://github.com/openjdk/jfx)
- [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)
- [bouncycastle](https://www.bouncycastle.org/java.html)

## License
协议原版权归属腾讯科技股份有限公司所有，本项目其他代码遵守：  
**GNU AFFERO GENERAL PUBLIC LICENSE version 3**  

其中部分要求:  
- (见 LICENSE 第 13 节) 尽管本许可协议有其他规定，但如果您修改本程序，则修改后的版本必须显着地为所有通过计算机网络与它进行远程交互的用户（如果您的版本支持这种交互）提供从网络服务器通过一些标准或惯用的软件复制方法**免费**访问相应的**源代码**的机会
- (见 LICENSE 第 4 节) 您可以免费或收费地传递这个项目的源代码或目标代码(即编译结果), **但前提是提供明显的版权声明** (您需要标注本 `GitHub` 项目地址)

## Acknowledgement
特别感谢 [JetBrains](https://www.jetbrains.com/?from=mirai) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 的授权  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)

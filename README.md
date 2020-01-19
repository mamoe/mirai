# Mirai
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7d0ec3ea244b424f93a6f59038a9deeb)](https://www.codacy.com/manual/Him188/mirai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mamoe/mirai&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Actions Status](https://github.com/mamoe/mirai/workflows/CI/badge.svg)](https://github.com/mamoe/mirai/actions)
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
**[English](README-eng.md)**  

**TIM PC 协议** 跨平台 QQ 协议支持库.  
**纯 Kotlin 实现协议和支持框架. 目前可运行在 JVM 或 Android.**  
部分协议来自网络上开源项目.   

**一切开发旨在学习，请勿用于非法用途**  

您可在 Gitter 提问, 或加入 QQ 群: 655057127

## Update log
在 [Project](https://github.com/mamoe/mirai/projects/1) 查看已支持功能和计划  
在 [UpdateLog](https://github.com/mamoe/mirai/blob/master/UpdateLog.md) 查看版本更新记录

## Contribution

我们 (Mamoe, NaturalHG & Him188) 将会一直维护这个项目，除非遇到不可抗力因素。

我们欢迎一切形式的贡献。  
我们也期待有更多人能加入 Mirai 的开发。  

若在使用过程中有任何疑问, 可提交 issue 或是邮件联系(support@mamoe.net). 我们希望 Mirai 变得更易用.

您的 star 是对我们最大的鼓励(点击项目右上角);  

## Use directly
**直接使用Mirai(终端环境/网页面板）.**  
[Mirai-Console](https://github.com/mamoe/mirai/mirai-console)

## Use as a library
**把 Mirai 作为库内置于您的项目中使用.**  
Mirai 只上传在 `jcenter`, 因此请确保在 `build.gradle` 添加 `jcenter()` 仓库  
```kotlin
repositories{
  jcenter()
}
```
若您需要使用在跨平台项目, 您需要对各个目标平台添加不同的依赖.  
**若您只需要使用在单一平台, 则只需要添加一项该平台的依赖. 如只在JVM运行则只需要`-jvm`的依赖**  

您需要将 `VERSION` 替换为最新的版本(如 `0.10.6`):
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
**Mirai 目前还处于实验性阶段, 我们无法保证任何稳定性, API 也可能会随时修改.**

现在 Mirai 只支持 TIM PC 协议.  
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

## Wiki
在 [Wiki](https://github.com/mamoe/mirai/wiki/Development-Guide---Kotlin) 中查看各类帮助

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

1. Clone
2. Import as Gradle project
3. 运行 Demo 程序: [mirai-demo](#mirai-demo) 示例和演示程序


## Build Requirements

- Kotlin 1.3.61  
- JDK 8
- Android SDK 29

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
特别感谢 [JetBrains](https://www.jetbrains.com/?from=mirai) 提供的免费 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 授权  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)
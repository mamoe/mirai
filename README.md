<div align="center">
 
   <img width="160" src="http://img.mamoe.net/2020/02/16/a759783b42f72.png" alt="logo"></br>
   <img width="95" src="http://img.mamoe.net/2020/02/16/c4aece361224d.png" alt="title">
----
[![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Actions Status](https://github.com/mamoe/mirai/workflows/CI/badge.svg)](https://github.com/mamoe/mirai/actions)
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  

Mirai 是一个在全平台下运行，提供 QQ Android 和 TIM PC 协议支持的高效率机器人框架

这个项目的名字来源于
     <p><a href = "http://www.kyotoanimation.co.jp/">京都动画</a>作品<a href = "https://www.bilibili.com/bangumi/media/md3365/?from=search&seid=14448313700764690387">《境界的彼方》</a>的<a href = "https://zh.moegirl.org/zh-hans/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5">栗山未来(Kuriyama <b>Mirai</b>)</a></p>
     <p><a href = "https://www.crypton.co.jp/">CRYPTON</a>以<a href = "https://www.crypton.co.jp/miku_eng">初音未来</a>为代表的创作与活动<a href = "https://magicalmirai.com/2019/index_en.html">(Magical <b>Mirai</b>)</a></p>
图标以及形象由画师<a href = "">DazeCake</a>绘制
</div>

## Mirai
**[English](README-eng.md)**  

多平台 **QQ Android 和 TimPC** 协议支持库与高效率的机器人框架.   
纯 Kotlin 实现协议和支持框架，模块<b>全部免费开源</b>。  
目前可运行在 JVM 或 Android。
Mirai既可以作为你项目中的QQ协议支持Lib, 也可以作为单独的Application与插件承载QQ机器人

**一切开发旨在学习，请勿用于非法用途**  

加入 Gitter, 或加入 QQ 群: 655057127

## CHANGELOG
在 [Project](https://github.com/mamoe/mirai/projects/3) 查看已支持功能和计划
在 [CHANGELOG](https://github.com/mamoe/mirai/blob/master/CHANGELOG.md) 查看版本更新记录 (仅发布的版本)

## Modules
### mirai-core
通用 API 模块，一套 API 适配两套协议。  
**请参考此模块的 API**  
  
### mirai-core-qqandroid
  QQ for Android （8.2.0 版本，2019 年 12 月）协议的实现，目前完成大部分。   
  - 高兼容性：协议仅含极少部分为硬编码，其余全部随官方方式动态生成
  - 高安全性：密匙随机，ECDH 动态计算
  - 已支持大部分使用场景, 详情请在[Project](https://github.com/mamoe/mirai/projects/3)查看

### mirai-core-timpc
TIM PC （2.3.2 版本，2019 年 8 月）协议的实现，相较于 core，仅新增少量 API. 详见 [README.md](mirai-core-timpc/)   
支持的功能： 
- 消息收发：图片文字复合消息，图片消息
- 群管功能：群员列表，禁言
（目前不再更新此协议，请关注上文的安卓协议）

## Use directly
**直接使用 Mirai(终端环境/网页面板（将来））.**  
[Mirai-Console](https://github.com/mamoe/mirai/tree/master/mirai-console) 插件支持, 在终端中启动 Mirai 并获得机器人服务  
本模块还未完善。

## Use as a library
**mirai-core 为独立设计, 可以作为库内置于任意 Java(JVM)/Android 项目中使用.**   

### Maven
Kotlin 在 Maven 上只支持 JVM 平台.
```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-qqandroid</artifactId>
        <version>0.15.1</version> <!-- 替换版本为最新版本 -->
    </dependency>
</dependencies>
```

### Gradle
Mirai 只发布在 `jcenter`, 因此请确保添加 `jcenter()` 仓库：
```kotlin
repositories{
  jcenter()
}
```
若您需要使用在跨平台项目, 则要对各个目标平台添加不同的依赖，这与 kotlin 相关多平台库的依赖是类似的。  
**若您只需要使用在单一平台, 则只需要添加一项该平台的依赖.**

请将 `VERSION` 替换为最新的版本(如 `0.15.0`):
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  
**Mirai 目前还处于实验性阶段, 我们无法保证任何稳定性, API 也可能会随时修改.**

**注意：**
Mirai 核心由 API 模块（`mirai-core`）和协议模块组成。  
只添加 API 模块将无法正常工作。  
现在只推荐使用 QQAndroid 协议，请参照下文选择对应目标平台的依赖添加。

**jvm** (JVM 平台)
```kotlin
implementation("net.mamoe:mirai-core-qqandroid:VERSION")
```
**common** (通用平台)
```kotlin
implementation("net.mamoe:mirai-core-qqandroid-common:VERSION")
```
**android** (Android 平台)
```kotlin
implementation("net.mamoe:mirai-core-qqandroid-android:VERSION")
```

## Java Compatibility
**若你希望使用 Java 开发**, 请查看: [mirai-japt](mirai-japt/README.md)

### Performance
Android 上, Mirai 运行需使用 80M 内存.  
JVM 上启动需 80M 内存, 每多一个机器人实例需要 30M 内存.

## Contribution

我们欢迎一切形式的贡献。  
我们也期待有更多人能加入 Mirai 的开发。  

若在使用过程中有任何疑问, 可提交 issue 或是邮件联系(support@mamoe.net). 我们希望 Mirai 变得更易用.

您的 star 是对我们最大的鼓励(点击项目右上角)

## Wiki
在 [Wiki](https://github.com/mamoe/mirai/wiki/Home) 中查看各类帮助，**如 API 示例**（可能过时，待 QQ Android 协议完成后会重写）。

## Try

### On JVM or Android
现在体验低付出高效率的 Mirai

```kotlin
val bot = Bot(qqId, password).alsoLogin()
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
- [lanterna](https://github.com/mabe02/lanterna/tree/master)
- [fastjson](https://github.com/alibaba/fastjson)
- [toml4j](https://github.com/mwanji/toml4j)
- [snakeyaml](https://mvnrepository.com/artifact/org.yaml/snakeyaml)

## License
协议原版权归属腾讯科技股份有限公司所有，本项目其他代码遵守：  
**GNU AFFERO GENERAL PUBLIC LICENSE version 3**  

其中部分要求:  
- (见 LICENSE 第 13 节) 尽管本许可协议有其他规定，但如果您修改本程序，则修改后的版本必须显着地为所有通过计算机网络与它进行远程交互的用户（如果您的版本支持这种交互）提供从网络服务器通过一些标准或惯用的软件复制方法**免费**访问相应的**源代码**的机会
- (见 LICENSE 第 4 节) 您可以免费或收费地传递这个项目的源代码或目标代码(即编译结果), **但前提是提供明显的版权声明** (您需要标注本 `GitHub` 项目地址)

## Acknowledgement
特别感谢 [JetBrains](https://www.jetbrains.com/?from=mirai) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 的授权  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)
 
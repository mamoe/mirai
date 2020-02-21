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

## 开始使用Mirai

Mirai支持以多种方式进行部署，但是目前，我们在集中对mirai-core，mirai-japt, mirai-api-http, mirai-console等核心模块进行特性的开发。

### 开发者
#### 以编写机器人逻辑为主

- 需了解<b>Java</b>或<b>Kotlin</b>, 使用[mirai-console](https://github.com/mamoe/mirai/tree/master/mirai-console)机器人框架的插件系统进行[插件开发(DOING)](), 直接进行逻辑编写并与其他插件开发者合作共享

#### 使用Mirai为第三方库

- 假如你倾向使用Kotlin及包管理工具，请参阅[Mirai Guide - Quick Start](/docs/guide_quick_start.md)
- 假如你想要试用Kotlin，希望一份较详细的起步教程，请参阅[Mirai Guide - Getting Started](/docs/guide_getting_started.md)
- 假如你倾向Java作为开发语言，请参阅[mirai-japt](/mirai-japt/README.md)
- 假如你倾向其他语言开发，可以通过了解 [mirai-api-http](https://github.com/mamoe/mirai/tree/master/mirai-api-http) 进行接入，欢迎开发不同平台的mirai-sdk
- 此外，你还可以在 [Wiki](https://github.com/mamoe/mirai/wiki/Home) 中查看各类帮助，**如 API 示例**。

### 使用者

- [mirai-console](https://github.com/mamoe/mirai/tree/master/mirai-console) 支持插件, **全平台可运行(UI版, Unix版, Android版, Web版)** **本模块正在完善**

### 我是其他平台的使用者

#### 酷Q平台用户: 

- 全部酷Q的DLL插件可以在mirai中直接加载, 需使用 `Mirai-Native` ([源码(DOING)](https://github.com/iTXTech/mirai-native),[插件版(mirai-console插件)(DOING)]())
- 全部基于 `酷Q HTTPAPI` 的插件可以在`mirai`中直接加载, 需使用`Mirai-CQ-Adapter`([源码(TODO)](https://github.com/iTXTech/mirai-native),[插件版(mirai-console插件)(TODO)]())

#### 其他平台

- 如果你的插件是通过 `酷Q HTTPAPI` 实现的(绝大部分), 都可以通过 `Mirai-CQ-Adapter` 在mirai中直接使用

## 更新日志

* 在 [Project](https://github.com/mamoe/mirai/projects/3) 查看已支持功能和计划
* 在 [CHANGELOG](https://github.com/mamoe/mirai/blob/master/CHANGELOG.md) 查看版本更新记录 (仅发布的版本)

## 模块

### mirai-core

通用 API 模块，一套 API 适配两套协议。
**请参考此模块的 API**  

### mirai-core-qqandroid

  QQ for Android （8.2.0 版本，2019 年 12 月）协议的实现，目前完成大部分。   

  - 高兼容性：协议仅含极少部分为硬编码，其余全部随官方方式动态生成
  - 高安全性：密匙随机，ECDH 动态计算
  - 已支持大部分使用场景, 详情请在[Project](https://github.com/mamoe/mirai/projects/3)查看

### mirai-core-timpc

TIM PC （2.3.2 版本，2019 年 8 月）协议的实现  
支持的功能： 

- 消息收发：图片文字复合消息，图片消息
- 群管功能：群员列表，禁言
  （目前不再更新此协议，请关注上文的安卓协议）

## 加入开发

我们欢迎一切形式的贡献。  
我们也期待有更多人能加入 `Mirai` 的开发。  

若在使用过程中有任何疑问, 可提交 `issue` 或是[邮件联系](mailto:support@mamoe.net). 我们希望 `Mirai` 变得更易用.

您的 `star` 是对我们最大的鼓励(点击项目右上角)

## 鸣谢

特别感谢 [JetBrains](https://www.jetbrains.com/?from=mirai) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 的授权  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)

### 第三方类库

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

## 许可证

协议原版权归属腾讯科技股份有限公司所有，本项目其他代码遵守：  
**GNU AFFERO GENERAL PUBLIC LICENSE version 3**  

其中部分要求:  

- (见 LICENSE 第 13 节) 尽管本许可协议有其他规定，但如果您修改本程序，则修改后的版本必须显着地为所有通过计算机网络与它进行远程交互的用户（如果您的版本支持这种交互）提供从网络服务器通过一些标准或惯用的软件复制方法**免费**访问相应的**源代码**的机会
- (见 LICENSE 第 4 节) 您可以免费或收费地传递这个项目的源代码或目标代码(即编译结果), **但前提是提供明显的版权声明** (您需要标注本 `GitHub` 项目地址)


    Copyright (C) 2019-2020 mamoe and Mirai contributors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

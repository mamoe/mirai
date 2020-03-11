<div align="center">
   <img width="160" src="http://img.mamoe.net/2020/02/16/a759783b42f72.png" alt="logo"></br>


   <img width="95" src="http://img.mamoe.net/2020/02/16/c4aece361224d.png" alt="title">

----

[![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
![Gradle CI](https://github.com/mamoe/mirai/workflows/Gradle%20CI/badge.svg?branch=master)
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  

Mirai 是一个在全平台下运行，提供 QQ Android 和 TIM PC 协议支持的高效率机器人框架

这个项目的名字来源于
     <p><a href = "http://www.kyotoanimation.co.jp/">京都动画</a>作品<a href = "https://zh.moegirl.org/zh-hans/%E5%A2%83%E7%95%8C%E7%9A%84%E5%BD%BC%E6%96%B9">《境界的彼方》</a>的<a href = "https://zh.moegirl.org/zh-hans/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5">栗山未来(Kuriyama <b>Mirai</b>)</a></p>
     <p><a href = "https://www.crypton.co.jp/">CRYPTON</a>以<a href = "https://www.crypton.co.jp/miku_eng">初音未来</a>为代表的创作与活动<a href = "https://magicalmirai.com/2019/index_en.html">(Magical <b>Mirai</b>)</a></p>
图标以及形象由画师<a href = "">DazeCake</a>绘制
</div>

## Mirai

**[English](README-eng.md)**  


多平台 **QQ Android** 和 **TIM PC** 协议支持库与高效率的机器人框架.   
纯 Kotlin 实现协议和支持框架，模块<b>全部免费开源</b>。  
目前可运行在 JVM 或 Android 平台。
mirai 既可以作为你项目中的 QQ 协议支持库, 也可以作为单独的应用程序与插件承载 QQ 机器人服务。

**一切开发旨在学习，请勿用于非法用途**  

加入 [![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge), 或加入 QQ 群: 655057127


## 开始

Mirai 目前为快速流转（Moving fast）状态, 增量版本之间可能不具有兼容性，任何功能都可能在没有警告的情况下添加、删除或者更改。

Mirai 源码完全开放, 您可以参考 Mirai 的协议实现来开发其他框架, 但需注明来源并遵守开源协议要求 (AGPLv3)。

### 开发者

**了解 mirai 架构**： [Wiki](https://github.com/mamoe/mirai/wiki/Home) 

#### 使用 mirai 作为服务器，为 mirai 开发插件

- （官方）`Java` 或 `Kotlin`： 为 [mirai-console](https://github.com/mamoe/mirai-console) 直接编写插件并与其他插件开发者合作共享
- （社区）`C`, `C++` 等原生语言： [mirai-native](https://github.com/iTXTech/mirai-native) 支持酷Q插件在mirai上运行
- （社区）`Python`: [python-mirai](https://github.com/Chenwe-i-lin/python-mirai) 基于`Mirai-http-api`的 Mirai Framework for Python
- （社区）`JavaScript`(`NodeJS`) [node-mirai](https://github.com/RedBeanN/node-mirai) Mirai的NodeJs SDK
- （官方）其他任意语言： [mirai HTTP 接口](https://github.com/mamoe/mirai-api-http) 进行接入

#### 使用 mirai 为第三方依赖库引入项目

Demos: [mirai-demos](https://github.com/mamoe/mirai-demos)

- `Kotlin` 简略版： [Mirai Guide - Quick Start](/docs/guide_quick_start.md)
- `Kotlin` 新手版： [Mirai Guide - Getting Started](/docs/guide_getting_started.md)
- `Java`： 查看上述 Demos

### 使用者

- [mirai-console](https://github.com/mamoe/mirai-console) 支持插件 **本模块正在完善**

### 我是其他平台的使用者

#### 酷 Q 平台用户: 

- 酷Q的插件可以在 mirai 中加载, 详见 [Mirai-Native](https://github.com/iTXTech/mirai-native)
- 使用 `酷Q HTTP API` 的插件将可以在 mirai 中加载，`Mirai-CQ-Adapter` 正在进行中

## 更新日志

* 在 [Project](https://github.com/mamoe/mirai/projects/3) 查看已支持功能和计划
* 在 [CHANGELOG](https://github.com/mamoe/mirai/blob/master/CHANGELOG.md) 查看版本更新记录 (仅发布的版本)

## [贡献](https://github.com/mamoe/mirai/blob/master/CONTRIBUTING.md)

我们欢迎一切形式的贡献。  
我们也期待有更多人能加入 mirai 的开发。  

若在使用过程中有任何疑问, 可提交 `issue` 或是[邮件联系](mailto:support@mamoe.net). 我们希望 mirai 变得更易用.

您的 `star` 是对我们最大的鼓励(点击项目右上角)

### 贡献者
感谢以下全体开发者对 mirai 的贡献（排名不分先后）

[<img width="60px" height="60px" src="https://avatars2.githubusercontent.com/u/12100985?s=60&v=4" />](https://github.com/Him188)
[<img width="60px" height="60px" src="https://avatars0.githubusercontent.com/u/24618776?s=60&v=4" />](https://github.com/liujiahua123123)
[<img width="60px" height="60px" src="https://avatars2.githubusercontent.com/u/28707253?s=60&v=4" />](https://github.com/ryoii)
[<img width="60px" height="60px" src="https://avatars1.githubusercontent.com/u/11070535?s=60&v=4" />](https://github.com/jasonczc)
[<img width="60px" height="60px" src="https://avatars2.githubusercontent.com/u/13656668?s=60&v=4" />](https://github.com/PeratX)
[<img width="60px" height="60px" src="https://avatars2.githubusercontent.com/u/18532671?s=60&v=4" />](https://github.com/uebian)
[<img width="60px" height="60px" src="https://avatars2.githubusercontent.com/u/10308687?s=60&v=4" />](https://github.com/Freedom0925)
[<img width="60px" height="60px" src="https://avatars3.githubusercontent.com/u/16398479?s=60&v=4" />](https://github.com/ice1000)
[<img width="60px" height="60px" src="https://avatars0.githubusercontent.com/u/20042607?s=60&v=4" />](https://github.com/PragmaTwice)
[<img width="60px" height="60px" src="https://avatars0.githubusercontent.com/u/25280943?s=60&v=4" />](https://github.com/HoshinoTented)
[<img width="60px" height="60px" src="https://avatars3.githubusercontent.com/u/40517459?s=60&v=4" />](https://github.com/Cyenoch)


## 鸣谢

特别感谢 [JetBrains](https://www.jetbrains.com/?from=mirai) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai) 等 IDE 的授权  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)

### 第三方类库(无排名)

- [kotlin-stdlib](https://github.com/JetBrains/kotlin)
- [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [kotlinx-io](https://github.com/Kotlin/kotlinx-io)
- [kotlin-reflect](https://github.com/JetBrains/kotlin)
- [atomicfu](https://github.com/Kotlin/kotlinx.atomicfu)
- [ktor](https://github.com/ktorio/ktor)
- [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)
- [bouncycastle](https://www.bouncycastle.org/java.html)



## 许可证

协议原版权归属腾讯科技股份有限公司所有，本项目其他代码遵守：  
**GNU AFFERO GENERAL PUBLIC LICENSE version 3**  

其中部分要求:  

- (见 LICENSE 第 13 节) 尽管本许可协议有其他规定，但如果您修改本程序，则修改后的版本必须显着地为所有通过计算机网络与它进行远程交互的用户（如果您的版本支持这种交互）提供从网络服务器通过一些标准或惯用的软件复制方法**免费**访问相应的**源代码**的机会
- (见 LICENSE 第 4 节) 您可以免费或收费地传递这个项目的源代码或目标代码(即编译结果), **但前提是提供明显的版权声明** (您需要标注本 `GitHub` 项目地址)

------

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

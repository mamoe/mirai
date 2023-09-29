# Mirai

欢迎来到 mirai 开发文档。

本文面向要进行开发的用户。对于只使用现成插件的用户，请阅读 [用户手册](UserManual.md)。

[Mirai 生态概览](mirai-ecology.md)

[Mirai VuePress 文档](https://docs.mirai.mamoe.net/)

## 社区 SDK

**mirai 官方提供 [Kotlin/Java 等 JVM 平台语言开发支持](#使用-mirai)
。如果不熟悉这些语言，请使用以下社区 SDK：**

要使用这些社区 SDK 需要先配置 Mirai
Console，可以使用 [一键安装](https://mirai.mamoe.net/assets/uploads/files/1618372079496-install-20210412.cmd)
（32位，带
HTTP 插件），也可以阅读 [用户手册](UserManual.md) 进行个性化安装。

你可以使用一个或多个语言来开发插件，而且在自己开发的同时也可以[使用下载的插件](UserManual.md#下载和安装插件)。

[`mirai-console`]: ../mirai-console

[mamoe/mirai-api-http]: https://github.com/mamoe/mirai-api-http

[iTXTech/mirai-native]: https://github.com/iTXTech/mirai-native

[iTXTech/mirai-js]: https://github.com/iTXTech/mirai-js

[iTXTech/mirai-kts]: https://github.com/iTXTech/mirai-kts

[AliceBot]: https://github.com/AliceBotProject/alicebot

[GraiaProject/Ariadne]: https://github.com/GraiaProject/Ariadne

[GraiaProject/Avilla]: https://github.com/GraiaProject/Avilla

[Elaina]: https://github.com/wyapx/Elaina

[ArcletProject/Edoves]: https://github.com/ArcletProject/Edoves

[NoneBot]: https://github.com/nonebot/nonebot2

[RedBeanN/node-mirai]: https://github.com/RedBeanN/node-mirai

[Logiase/gomirai]: https://github.com/Logiase/gomirai

[cyanray/mirai-cpp]: https://github.com/cyanray/mirai-cpp

[Chlorie/miraipp]: https://github.com/Chlorie/miraipp-template

[Numendacil/cpp-mirai-client]: https://github.com/Numendacil/cpp-mirai-client

[Executor-Cheng/mirai-CSharp]: https://github.com/Executor-Cheng/mirai-CSharp

[HoshinoTented/mirai-rs]: https://github.com/HoshinoTented/mirai-rs

[YunYouJun/mirai-ts]: https://github.com/YunYouJun/mirai-ts

[nepsyn/miraipie]: https://github.com/nepsyn/miraipie

[only52607/e-mirai]: https://github.com/only52607/e-mirai

[theGravityLab/ProjHyperai]: https://github.com/theGravityLab/ProjHyperai

[yyuueexxiinngg/onebot-kotlin]: https://github.com/yyuueexxiinngg/onebot-kotlin

[Nambers/MiraiCP]:https://github.com/Nambers/MiraiCP

[drinkal/Mirai-js]:https://github.com/drinkal/Mirai-js

[Coloryr/ColorMirai]: https://github.com/Coloryr/ColorMirai

[AHpxChina/Mirai.Net]: https://github.com/AHpxChina/Mirai.Net

[Cyl18/Chaldene]: https://github.com/Cyl18/Chaldene

[Miyakowww/CocoaFramework2]: https://github.com/Miyakowww/CocoaFramework2

[Shimogawa/rubirai]: https://github.com/Shimogawa/rubirai

[Excaive/miraicle]: https://github.com/Excaive/miraicle

[nkxingxh/miraiez]: https://github.com/nkxingxh/miraiez

[Xwdit/RainyBot-Core]: https://github.com/Xwdit/RainyBot-Core

[OneBot]: https://github.com/howmanybots/onebot

[Mirai HTTP]: https://github.com/project-mirai/mirai-api-http

[jerrita/saaya]: https://github.com/jerrita/saaya

[YiriMirai]: https://github.com/YiriMiraiProject/YiriMirai

[MiraiBots.jl]: https://github.com/melonedo/MiraiBots.jl

[Novices666/mirai-epl]:https://github.com/Novices666/mirai-epl

[easyMirai]:https://github.com/easyMirais/easyMirai

[MR-XieXuan/MiraiTravel]:https://github.com/MR-XieXuan/MiraiTravel

[yuansicloud/Abp.Mirai]:https://github.com/yuansicloud/Abp.Mirai
### 原生接口

这些接口直接在 JVM 上实现，不需要中间件，拥有更佳的性能。

| 技术                 | 维护者及项目地址               |
|:-------------------|:-----------------------|
| `Kotlin Scripting` | [iTXTech/mirai-kts]    |
| `C++`              | [Nambers/MiraiCP]      |
| `JavaScript`       | [iTXTech/mirai-js]     |
| *酷 Q DLL 插件*       | [iTXTech/mirai-native] |

### HTTP 接口

目前有两个 HTTP 协议插件。使用 HTTP 协议插件可以支持更多编程语言和技术。

- [***Mirai HTTP***][Mirai HTTP] 由 Mirai 开发团队提供第一级支持，目前多数 SDK 都基于它；
- [OneBot] 标准则兼容酷Q协议，可以让基于酷Q HTTP 插件的项目在 Mirai 平台运行。

| 名称               | 实现          | 维护者及项目地址                       |
|:-----------------|:------------|:-------------------------------|
| ***Mirai Http*** | Mirai 标准    | [mamoe/mirai-api-http]         |
| *OneBot Http*    | [OneBot] 标准 | [yyuueexxiinngg/onebot-kotlin] |

下表列举基于 Mirai HTTP
插件实现对一些编程语言支持的项目列表。要使用它们，你需要[在 Mirai Console 安装 `mirai-api-http`](https://github.com/project-mirai/mirai-api-http#%E5%AE%89%E8%A3%85mirai-api-http)（如果使用上面的一键安装则不需要额外操作）。

| 语言和技术                     | 维护者及项目地址                             |
|:--------------------------|:-------------------------------------|
| `C#`                      | [Executor-Cheng/mirai-CSharp]        |
| `C#`                      | [Hyperai][theGravityLab/ProjHyperai] |
| `C#`                      | [Coloryr/ColorMirai]                 |
| `C#`                      | [AhpxChina/Mirai.Net]                |
| `C#`                      | [Cyl18/Chaldene]                     |
| `C#`                      | [Miyakowww/CocoaFramework2]          |
| `C#`                      | [yuansicloud/Abp.Mirai]              |
| `C++`                     | [cyanray/mirai-cpp]                  |
| `C++`                     | [Chlorie/miraipp]                    |
| `C++`                     | [Numendacil/cpp-mirai-client]        |
| `GDScript`                | [Xwdit/RainyBot-Core]                |
| `Go`                      | [Logiase/gomirai]                    |
| `JavaScript` / Node.js    | [RedBeanN/node-mirai]                |
| `JavaScript` / Node.js    | [drinkal/Mirai-js]                   |
| `JavaScript` / TypeScript | [YunYouJun/mirai-ts]                 |
| `JavaScript` / TypeScript | [nepsyn/miraipie]                    |
| `Julia`                   | [MiraiBots.jl]                       |
| `PHP`                     | [MiraiEz][nkxingxh/MiraiEz]          |
| `PHP`                     | [MR-XieXuan/MiraiTravel]             |
| `Python`                  | [AliceBot]                           |
| `Python`                  | [Ariadne][GraiaProject/Ariadne]      |
| `Python`                  | [Avilla][GraiaProject/Avilla]        |
| `Python`                  | [easyMirai]                          |
| `Python`                  | [Edoves][ArcletProject/Edoves]       |
| `Python`                  | [Elaina]                             |
| `Python`                  | [NoneBot]                            |
| `Python`                  | [jerrita/saaya]                      |
| `Python`                  | [YiriMirai]                          |
| `Python`                  | [Excaive/miraicle]                   |
| `Ruby`                    | [Shimogawa/rubirai]                  |
| `Rust`                    | [HoshinoTented/mirai-rs]             |
| `易语言`                     | [only52607/e-mirai]                  |
| `易语言`                     | [Novices666/mirai-epl]                  |
> 按字母顺序排序，排序不代表排名  
> *
想在这里添加你的项目？欢迎[提交 PR](https://github.com/mamoe/mirai/edit/dev/docs/README.md)
。*

特别地，有一些 SDK 直接基于 mirai-core 开发，不需要 [`mirai-console`]：

- `Lua`: [lua-mirai](https://github.com/only52607/lua-mirai)

## 使用 Mirai

Mirai 原生支持 Java、Kotlin 等 JVM 平台编程语言。

要使用 Mirai，可以使用 mirai-core 作为一个依赖库获得机器人功能，也可以为 mirai-console 开发插件。

生态详情可阅读：[Mirai 生态概览](mirai-ecology.md)。

### JVM 平台 mirai-core 开发

本节介绍使用 Java、Kotlin 等 JVM 平台编程语言使用 mirai-core 作为一个依赖库获得机器人功能。
这通常适用于你在开发一个其他应用程序而需要使用机器人功能的情况。

1. [JVM 环境和开发准备工作](Preparations.md#mirai---preparations)
2. [配置 mirai-core 项目依赖](ConfiguringProjects.md)
3. [阅读 mirai-core 文档](CoreAPI.md)

> 如果你希望先体验 mirai
> 的机器人功能，可克隆 [mirai-hello-world](https://github.com/project-mirai/mirai-hello-world)
> 并在 IDE 内运行其中 Kotlin 或 Java 入口点 `main`。

### JVM 平台 mirai-console 插件开发

本节介绍使用 Java、Kotlin 等 JVM 平台编程语言基于 mirai-core，开发可于 mirai-console
加载的插件来提供机器人功能。
这通常适用于你为了开发一个机器人程序的情况。开发 mirai-console
插件既可以[单独使用](../mirai-console/docs/Run.md)，也可以使用来自社区的其他插件。

1. [JVM 环境和开发准备工作](Preparations.md#mirai---preparations)

2. [配置 mirai-console 插件项目](../mirai-console/docs/ConfiguringProjects.md)

3. [阅读 mirai-core 文档](CoreAPI.md)  
   mirai-core 文档可让你了解如何使用 Bot 功能。

4. [阅读 mirai-console 文档](../mirai-console/docs/README.md)
   mirai-console 文档可让你了解 mirai-console 的一些系统。

### 多平台 mirai-core 开发

[Kotlin 多平台]: https://kotlinlang.org/docs/multiplatform.html

本节介绍使用 Kotlin 使用 mirai-core 开发 [Kotlin 多平台] 应用程序。

1. [JVM 环境和开发准备工作](Preparations.md#mirai---preparations)
2. [配置 mirai-core 多平台项目依赖](ConfiguringMultiplatformProjects.md)
3. [阅读 mirai-core 文档](CoreAPI.md)

## 发布项目

欢迎各类基于 mirai 开发的开源项目在论坛发布。

- [在论坛发布](https://mirai.mamoe.net/category/6/%E9%A1%B9%E7%9B%AE%E5%8F%91%E5%B8%83)

## 文档

mirai 在 GitHub 托管的文档可让你简要了解各个系统。

mirai 的源码内注释十分详细，包含各种实践示例。

### mirai-core 文档

请在 [CoreAPI.md](CoreAPI.md) 阅读 JVM 平台的 mirai-core 开发文档。

### mirai-core API KDoc

可在 <https://kdoc.mirai.mamoe.net/> 查看基于源码内注释生成的 KDoc（类似 JavaDoc）。
但更建议使用 IntelliJ IDEA 等 IDE 在开发时查询源码内注释。

### mirai-console 文档

请在 [mirai-console/docs](../mirai-console/docs/README.md) 阅读
mirai-console 开发文档。

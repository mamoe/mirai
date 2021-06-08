# Mirai

欢迎来到 mirai 开发文档。

本文面向要进行开发的用户。对于只使用现成插件的用户，请阅读 [用户手册](UserManual.md)。

[Mirai 生态概览](mirai-ecology.md)

[Mirai VuePress 文档](https://docs.mirai.mamoe.net/)

## 社区 SDK

**mirai 官方提供 [Kotlin/Java 等 JVM 平台语言开发支持](#jvm-平台-mirai-开发)。如果不熟悉这些语言，请使用以下社区 SDK：**

要使用这些社区 SDK 需要先配置 Mirai Console，可以使用 [一键安装](https://mirai.mamoe.net/assets/uploads/files/1618372079496-install-20210412.cmd)（32位，带 HTTP 插件），也可以阅读 [用户手册](UserManual.md) 进行个性化安装。

你可以使用一个或多个语言来开发插件，而且在自己开发的同时也可以[使用下载的插件](UserManual.md#下载和安装插件)。

[`mirai-console`]: https://github.com/mamoe/mirai-console

[mamoe/mirai-api-http]: https://github.com/mamoe/mirai-api-http
[iTXTech/mirai-native]: https://github.com/iTXTech/mirai-native
[iTXTech/mirai-js]: https://github.com/iTXTech/mirai-js
[iTXTech/mirai-kts]: https://github.com/iTXTech/mirai-kts
[GraiaProject/Application]: https://github.com/GraiaProject/Application
[NoneBot]: https://github.com/nonebot/nonebot2
[RedBeanN/node-mirai]: https://github.com/RedBeanN/node-mirai
[Logiase/gomirai]: https://github.com/Logiase/gomirai
[cyanray/mirai-cpp]: https://github.com/cyanray/mirai-cpp
[Chlorie/miraipp]: https://github.com/Chlorie/miraipp-template
[Executor-Cheng/mirai-CSharp]: https://github.com/Executor-Cheng/mirai-CSharp
[HoshinoTented/mirai-rs]: https://github.com/HoshinoTented/mirai-rs
[YunYouJun/mirai-ts]: https://github.com/YunYouJun/mirai-ts
[only52607/e-mirai]: https://github.com/only52607/e-mirai
[theGravityLab/ProjHyperai]: https://github.com/theGravityLab/ProjHyperai
[yyuueexxiinngg/onebot-kotlin]: https://github.com/yyuueexxiinngg/onebot-kotlin
[Nambers/MiraiCP]:https://github.com/Nambers/MiraiCP
[drinkal/Mirai-js]:https://github.com/drinkal/Mirai-js
[Coloryr/ColorMirai]: https://github.com/Coloryr/ColorMirai
[AHpxChina/Mirai.Net]: https://github.com/AHpxChina/Mirai.Net
[Shimogawa/rubirai]: https://github.com/Shimogawa/rubirai

[OneBot]: https://github.com/howmanybots/onebot
[Mirai HTTP]: https://github.com/project-mirai/mirai-api-http
[jerrita/saaya]: https://github.com/jerrita/saaya

### 原生接口

这些接口直接在 JVM 上实现，不需要中间件，拥有更佳的性能。

| 技术                | 维护者及项目地址          |
|:-------------------|:-----------------------|
| `Kotlin Scripting` | [iTXTech/mirai-kts]    |
| `C++`              | [Nambers/MiraiCP]      |
| `JavaScript`       | [iTXTech/mirai-js]     |
| *酷 Q DLL 插件*     | [iTXTech/mirai-native] |

### HTTP 接口

目前有两个 HTTP 协议插件。使用 HTTP 协议插件可以支持更多编程语言和技术。

- [***Mirai HTTP***][Mirai HTTP] 由 Mirai 开发团队提供第一级支持，目前多数 SDK 都基于它；
- [OneBot] 标准则兼容原酷Q协议，可以让基于酷Q HTTP 插件的项目在 Mirai 平台运行。

| 名称              | 实现          | 维护者及项目地址                  |
|:-----------------|:-------------|:-------------------------------|
| ***Mirai Http*** | Mirai 标准    | [mamoe/mirai-api-http]         |
| *OneBot Http*    | [OneBot] 标准 | [yyuueexxiinngg/onebot-kotlin] |

下表列举基于 Mirai HTTP 插件实现对一些编程语言支持的项目列表。要使用它们，你需要[在 Mirai Console 安装 `mirai-api-http`](https://github.com/project-mirai/mirai-api-http#%E5%AE%89%E8%A3%85mirai-api-http)（如果使用上面的一键安装则不需要额外操作）。


| 语言和技术                  | 维护者及项目地址                               |
|:--------------------------|:--------------------------------------------|
| `Python`                  | [Graia Framework][GraiaProject/Application] |
| `Python`                  | [NoneBot]                                   |
| `Python`                  | [jerrita/saaya]                             |
| `C++`                     | [cyanray/mirai-cpp]                         |
| `C++`                     | [Chlorie/miraipp]                           |
| `C#`                      | [Executor-Cheng/mirai-CSharp]               |
| `C#`                      | [Hyperai][theGravityLab/ProjHyperai]        |
| `C#`                      | [Coloryr/ColorMirai]                        |
| `C#`                      | [AhpxChina/Mirai.Net]                       |
| `Ruby`                    | [Shimogawa/rubirai]                         |
| `Rust`                    | [HoshinoTented/mirai-rs]                    |
| `JavaScript` / Node.js    | [RedBeanN/node-mirai]                       |
| `JavaScript` / TypeScript | [YunYouJun/mirai-ts]                        |
| `JavaScript` / Node.js    | [drinkal/Mirai-js]                          |
| `Go`                      | [Logiase/gomirai]                           |
| `易语言`                   | [only52607/e-mirai]                         |


> 排名不分先后  
> *想在这里添加你的项目？欢迎[提交 PR](https://github.com/mamoe/mirai/edit/dev/docs/README.md)。*

特别地，有一些 SDK 直接基于 mirai-core 开发，不需要 [`mirai-console`]：

- `Lua`: [lua-mirai](https://github.com/only52607/lua-mirai)

## JVM 平台 Mirai 开发

本节介绍使用 Java、Kotlin 等 JVM 平台编程语言开发 Mirai 或 Mirai Console 插件。

**为了避免遇到各种问题，请逐步仔细阅读。**

1. [JVM 环境和开发准备工作](Preparations.md#mirai---preparations)

2. 选择框架
   建议先阅读 [Mirai 生态概览](mirai-ecology.md)。

   - 若要将 mirai 当做依赖库嵌入你的应用使用（你调用 mirai），则需要使用 mirai-core，请阅读 [配置项目依赖](ConfiguringProjects.md)。

   - 若要以插件模式开发（mirai 调用你），可以使用 mirai-console，请阅读 [mirai-console 的配置插件项目](https://github.com/mamoe/mirai-console/blob/master/docs/ConfiguringProjects.md)。

4. 阅读 API 文档（见下文）


> 如果你希望先确认 mirai 能够正常运行才能安心阅读文档，可克隆 [mirai-hello-world](https://github.com/project-mirai/mirai-hello-world) 并运行其中 Kotlin 或 Java 入口点 `main`。


之后...

- [让更多人看到你的项目 - 在论坛发布](https://mirai.mamoe.net/category/6/%E9%A1%B9%E7%9B%AE%E5%8F%91%E5%B8%83)

### mirai-core API 文档

请在 [CoreAPI.md](CoreAPI.md) 阅读 JVM 平台的 mirai-core-api 使用文档。

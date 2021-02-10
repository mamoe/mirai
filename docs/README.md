# Mirai

欢迎来到 mirai 文档。

## 生态

**[Mirai 生态概览](mirai-ecology.md)**

## 社区 SDK

**mirai 官方提供 Kotlin/Java 等 JVM 平台语言开发支持。如果不熟悉这些语言，请使用以下社区 SDK：**

[`mirai-console`]: https://github.com/mamoe/mirai-console

这些 SDK 基于 [`mirai-console`]。[`mirai-console`] 是 mirai 官方维护的一个*应用程序*。可以在 [这里](https://github.com/mamoe/mirai-console/blob/master/docs/Run.md) 了解如何启动 [`mirai-console`]（也可以稍后在各 SDK 的说明中了解）。

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

[Rhino]: https://github.com/mozilla/rhino
[OneBot]: https://github.com/howmanybots/onebot

| 技术                | 实现                          | 维护者及项目地址                               |
|:-------------------|:-----------------------------|:--------------------------------------------|
| ***Mirai Http***   | Mirai 标准                    | [mamoe/mirai-api-http]                      |
| *OneBot Http*      | [OneBot] 标准                 | [yyuueexxiinngg/onebot-kotlin]              |
| `Kotlin Scripting` | JVM                          | [iTXTech/mirai-kts]                         |
| `Python`           | *Mirai Http*                 | [Graia Framework][GraiaProject/Application] |
| `Python`           | *Mirai Http* / *OneBot Http* | [NoneBot]                                   |
| `C++`              | JNI                          | [Nambers/MiraiCP]                           |
| `C++`              | *Mirai Http*                 | [cyanray/mirai-cpp]                         |
| `C++`              | *Mirai Http*                 | [Chlorie/miraipp]                           |
| `C#`               | *Mirai Http*                 | [Executor-Cheng/mirai-CSharp]               |
| `C#`               | *Mirai Http*                 | [Hyperai][theGravityLab/ProjHyperai]        |
| `Rust`             | *Mirai Http*                 | [HoshinoTented/mirai-rs]                    |
| `JavaScript`       | [Rhino] / JVM                | [iTXTech/mirai-js]                          |
| `JavaScript`       | Node.js / *Mirai Http*       | [RedBeanN/node-mirai]                       |
| `JavaScript`       | TypeScript / *Mirai Http*    | [YunYouJun/mirai-ts]                        |
| `JavaScript`       | Node.js / *Mirai Http*       | [drinkal/Mirai-js]                          |
| `Go`               | *Mirai Http*                 | [Logiase/gomirai]                           |
| `易语言`            | *Mirai Http*                 | [only52607/e-mirai]                         |
| *酷 Q DLL 插件*     | JNI                          | [iTXTech/mirai-native]                      |

> 排名不分先后  
> *想在这里添加你的项目？欢迎[提交 PR](https://github.com/mamoe/mirai/edit/dev/docs/README.md)。*

特别地，有一些 SDK 直接基于 mirai-core 开发，不需要 [`mirai-console`]：

- `Lua`: [lua-mirai](https://github.com/only52607/lua-mirai)

## JVM 平台 Mirai 开发

**为了避免遇到各种问题，请仔细阅读。**

1. [JVM 环境和开发准备工作（2 分钟）](Preparations.md#mirai---preparations)
2. **配置项目依赖** （二选一）
   - 要把 mirai-core 嵌入一个应用使用，请阅读 [配置项目依赖](ConfiguringProjects.md)。
   - 要为 [`mirai-console`] 框架开发插件，请阅读 [mirai-console 的配置插件项目](https://github.com/mamoe/mirai-console/blob/master/docs/ConfiguringProjects.md)。
3. 阅读 API 文档（见下文）

> 如果你不知道 `mirai-core` 或 [`mirai-console`] 是什么，请阅读 [Mirai 生态概览](mirai-ecology.md)。
>
> 如果你希望先确认 mirai 能够正常运行才能安心阅读文档，可克隆 [mirai-hello-world](https://github.com/project-mirai/mirai-hello-world) 并运行其中 Kotlin 或 Java 入口点 `main`。


### mirai-core API 文档

> *适用于 2.x 版本*

- [Bots](Bots.md)
- [Contacts](Contacts.md)
- [Events](Events.md)
- [Messages](Messages.md)

> 希望改进文档? 请在 [#848](https://github.com/mamoe/mirai/discussions/848) 提出建议

# Mirai

欢迎来到 mirai 文档。

## 生态

**[Mirai 生态概览](mirai-ecology.md)**

## 确定 SDK

mirai 官方提供 Kotlin/Java 等 JVM 平台语言开发支持。如果你不熟悉该语言，请使用以下社区提供的 SDK：

### 基于 [`mirai-console`]

[`mirai-console`]: https://github.com/mamoe/mirai-console

这些 SDK 基于 [`mirai-console`]，意味着需要使用 [`mirai-console`] 框架。[`mirai-console`] 也是 mirai 的官方项目之一。

[mamoe/mirai-api-http]: https://github.com/mamoe/mirai-api-http
[iTXTech/mirai-native]: https://github.com/iTXTech/mirai-native
[iTXTech/mirai-js]: https://github.com/iTXTech/mirai-js
[GraiaProject/Application]: https://github.com/GraiaProject/Application
[RedBeanN/node-mirai]: https://github.com/RedBeanN/node-mirai
[Logiase/gomirai]: https://github.com/Logiase/gomirai
[StageGuard/mirai-rhinojs-sdk]: https://github.com/StageGuard/mirai-rhinojs-sdk
[cyanray/mirai-cpp]: https://github.com/cyanray/mirai-cpp
[Chlorie/miraipp]: https://github.com/Chlorie/miraipp-template
[Executor-Cheng/mirai-CSharp]: https://github.com/Executor-Cheng/mirai-CSharp
[HoshinoTented/mirai-rs]: https://github.com/HoshinoTented/mirai-rs
[YunYouJun/mirai-ts]: https://github.com/YunYouJun/mirai-ts
[only52607/e-mirai]: https://github.com/only52607/e-mirai
[theGravityLab/ProjHyperai]: https://github.com/theGravityLab/ProjHyperai
[yyuueexxiinngg/cqhttp-mirai]: https://github.com/yyuueexxiinngg/cqhttp-mirai

| 技术             | 维护者及项目地址                               | 描述                                                                    |
|:----------------|:--------------------------------------------|:-----------------------------------------------------------------------|
| *Http*          | [mamoe/mirai-api-http]                      | Mirai 官方维护的 HTTP API 插件                                            |
| `JavaScript`    | [iTXTech/mirai-js]                          | 支持使用 `JavaScript` 编写插件并**直接**与 mirai 交互                        |
| `Python`        | [Graia Framework][GraiaProject/Application] | 基于 `mirai-api-http` 的机器人开发框架                                     |
| `Node.js`       | [RedBeanN/node-mirai]                       | mirai 的 Node.js SDK                                                   |
| `Go`            | [Logiase/gomirai]                           | 基于 mirai-api-http 的 GoLang SDK                                       |
| `Mozilla Rhino` | [StageGuard/mirai-rhinojs-sdk]              | 为基于 Rhino(如 Auto.js 等安卓 app 或运行环境)的 JavaScript 提供简单易用的 SDK |
| `C++`           | [cyanray/mirai-cpp]                         | mirai-http-api 的 C++ 封装，方便使用 C++ 开发 mirai-http-api 插件           |
| `C++`           | [Chlorie/miraipp]                           | mirai-http-api 的另一个 C++ 封装，使用现代 C++ 特性，并提供了较完善的说明文档    |
| `C#`            | [Executor-Cheng/mirai-CSharp]               | 基于 mirai-api-http 的 C# SDK                                           |
| `Rust`          | [HoshinoTented/mirai-rs]                    | mirai-http-api 的 Rust 封装                                             |
| `TypeScript`    | [YunYouJun/mirai-ts]                        | mirai-api-http 的 TypeScript SDK，附带声明文件，拥有良好的注释和类型提示       |
| `易语言`         | [only52607/e-mirai]                         | mirai-api-http 的 易语言 SDK，使用全中文环境开发插件，适合编程新手使用           |
| `.Net/C#`       | [Hyperai][theGravityLab/ProjHyperai]        | 从 mirai-api-http 对接到机器人开发框架再到开箱即用的插件式机器人程序一应俱全       |
| *酷 Q 插件*      | [iTXTech/mirai-native]                      | 支持酷 Q 插件在 mirai 上运行                                               |
| *酷 Q HTTP*     | [yyuueexxiinngg/cqhttp-mirai]               | 在 mirai-console 开启酷 Q HTTP 服务。                                     |

> *想在这里添加你的项目？欢迎提交 PR。*

### 基于 `mirai-core` 的 SDK

- `Lua`: [lua-mirai](https://github.com/only52607/lua-mirai) 基于 mirai-core 的 Lua SDK，并提供了 Java 扩展支持，可在 Lua 中调用 Java 代码开发机器人

## 开发

- [准备工作](Preparations.md#mirai---preparations)

### 配置 JVM 项目使用 mirai

> 可以首先体验让机器人发送消息：在 IDE 克隆 [mirai-hello-world](https://github.com/project-mirai/mirai-hello-world) 并运行其中入口点。

要把 mirai 作为一个依赖库使用，请参考 [Configuring Projects](ConfiguringProjects.md)。

要使用 mirai-console 框架，请前往 [mirai-console](https://github.com/mamoe/mirai-console)。

### API 文档

***本文档正在更新中***

- [Bots](Bots.md)
- [Contacts](Contacts.md)
- [Events](Events.md)
- [Messages](Messages.md)

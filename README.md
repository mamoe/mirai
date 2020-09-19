<div align="center">
   <img width="160" src="http://img.mamoe.net/2020/02/16/a759783b42f72.png" alt="logo"></br>


   <img width="95" src="http://img.mamoe.net/2020/02/16/c4aece361224d.png" alt="title">

----
Mirai 是一个在全平台下运行，提供 QQ 协议支持的高效率机器人库

这个项目的名字来源于
     <p><a href = "http://www.kyotoanimation.co.jp/">京都动画</a>作品<a href = "https://zh.moegirl.org/zh-hans/%E5%A2%83%E7%95%8C%E7%9A%84%E5%BD%BC%E6%96%B9">《境界的彼方》</a>的<a href = "https://zh.moegirl.org/zh-hans/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5">栗山未来(Kuriyama <b>Mirai</b>)</a></p>
     <p><a href = "https://www.crypton.co.jp/">CRYPTON</a>以<a href = "https://www.crypton.co.jp/miku_eng">初音未来</a>为代表的创作与活动<a href = "https://magicalmirai.com/2019/index_en.html">(Magical <b>Mirai</b>)</a></p>
图标以及形象由画师<a href = "">DazeCake</a>绘制
</div>

# mirai-console

高效率 QQ 机器人框架，机器人核心来自 [mirai](https://github.com/mamoe/mirai)

![Gradle CI](https://github.com/mamoe/mirai-console/workflows/Gradle%20CI/badge.svg?branch=master)
[![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

## 开发

- **[准备工作 - 环境和前置知识](docs/Preparations.md)**
- **[配置项目](docs/ConfiguringProjects.md)**
- **[启动 Console](docs/Run.md)**

### 后端插件开发基础

- 插件 - [Plugin 模块](docs/Plugins.md)
- 指令 - [Command 模块](docs/Commands.md)
- 存储 - [PluginData 模块](docs/PluginData.md)
- 权限 - [Permission 模块](docs/Permissions.md)


**示例插件**：
- [mirai-console-example-plugin (Kotlin DSL)](https://github.com/Him188/mirai-console-example-plugin)
- [mirai-console-example-plugin (Groovy DSL)](https://github.com/Karlatemp/mirai-console-example-plugin)

### 后端插件开发进阶

- 扩展 - [Extension 模块和扩展点](docs/Extensions.md)

### 实现前端
- [FrontEnd](docs/FrontEnd.md)
# mirai-console
高效率插件支持 QQ 机器人框架, 机器人核心来自 [mirai](https://github.com/mamoe/mirai)

## 模块说明

console 由后端和前端一起工作. 使用时必须选择一个前端.

- `mirai-console`: console 的后端, 包含插件管理, 指令系统, 配置系统. 还包含一个轻量命令行的前端 (因此可以独立启动 `mirai-console`).
- `mirai-console-graphical`: console 的 JavaFX 图形化界面前端.
- `mirai-console-terminal`: console 的 Unix 终端界面前端. (实验性)


[`mirai-console-wrapper`](https://github.com/mamoe/mirai-console-wrapper): console 启动器. 可根据用户选择从服务器下载 console 后端, mirai-core, 和指定的前端并启动.

### 使用

#### Windows

建议任何人都使用一键安装包来快速启动 mirai-console (因此你无需解决 JavaFX 和兼容等相关问题)  
**[下载地址](https://suihou-my.sharepoint.com/:f:/g/personal/user18_5tb_site/ErWGr97FpPVDjkboIDmDAJkBID-23ZMNbTPggGajf1zvGw?e=51NZWM)**

**请注意**
* 使用时请留意安装包里的说明文字
* 目前本安装包只支持Windows系统，**且 mirai-console 仍在开发中，可能会存在一些bug**
* 关于安装包本身的一切问题请到 QQ 群内反馈 (推荐), 或 [邮件联系](mailto:support@mamoe.net)
* 如果上面的链接下载过慢，你可以到QQ群内高速下载

若你不愿意简单地启动, 你可以往下阅读复杂的启动方式.

#### Unix

Unix 没有一键包提供. 请使用 wrapper 启动器.

1. 安装 JRE (Java 运行环境):
   -  若使用图形界面, 至少需要 JRE 11 并带有 JavaFX 11, 且不推荐使用 12 或更高版本.
   -  若使用命令行或终端, 至少需要 JRE 8.
   -  可以在 [华为镜像源](https://repo.huaweicloud.com/java/jdk/) 下载 JDK (JDK 包含 JRE 和开发工具)
2. 下载 `mirai-console-wrapper-x.x.x.jar`
3. 参照 [wrapper 命令行参数](https://github.com/mirai/mirai-console-wrapper/README.md#命令行参数), 运行 `$ java -jar mirai-console-wrapper-x.x.x.jar`

### 插件开发与获取

mirai-console 支持 Jar 插件.

**mirai-console 目前仍为实验性阶段, 任何功能和 API 都不保证稳定性. 任何 API 都可能在没有警告的情况下修改.**

(实验性) [插件中心](https://github.com/mamoe/mirai-plugins)  
[mirai-console插件开发快速上手](PluginDocs/ToStart.MD) 

# Mirai - UserManual

这里是 Mirai 用户手册。本文面向对开发并不熟悉，但希望使用 Mirai 提供的 QQ 机器人服务支持的用户。
如果你要开发 Mirai 插件或参与贡献 Mirai 项目，请先阅读 [开发文档](README.md)。  

## 启动 Mirai

想要部署并使用 Mirai QQ机器人框架，只需要启动 Mirai 控制台（即 Mirai Console），
它自带一些基础功能，也可以加载社区提供的插件。

Mirai 控制台现在有两个版本，Mirai 插件在这两个版本的 Mirai Console 上都可以运行:

[MCLI-1.png]: .UserManual_images/MCLI-1.png

[MCPS-1.png]: .UserManual_images/MCPS-1.png

| 类型   | 长啥样?         | 好用吗?      | 怎么装?                  |
|:-----|:-------------|:----------|:----------------------|
| 纯控制台 | [MCLI-1.png] | 稳定，也适合服务器 | [使用纯控制台版本](#使用纯控制台版本) |
| 图形界面 | [MCPS-1.png] | 测试版，不推荐使用 | [使用图形界面版本](#使用图形界面版本) |

## 使用纯控制台版本

详细教程请查看 [ConsoleTerminal.md](ConsoleTerminal.md)。

以 Windows 系统为例，以下为简要安装步骤：  

1. 前往 [iTXTech/mcl-installer](https://github.com/iTXTech/mcl-installer/releases) 下载适合您系统的最新版本的 MCL 安装器
2. 创建好文件夹之后，将 MCL 安装器移动到其中
3. 双击 `mcl-installer.exe` 过程中只需要按几次回车键，即可安装完毕
4. 运行 `mcl.cmd` 即可启动 MCL 控制台

安装插件只需要将下载好的插件置于 plugins 目录，然后重启 MCL 控制台即可。  

## ~~使用图形界面版本~~

开发者已停止更新，且有许多历史问题，故不推荐使用  
前往 [sonder-joker/mirai-compose](https://github.com/sonder-joker/mirai-compose/releases)
下载适合你的系统的压缩包，
>  MAC 系统下载 .dmg 后缀的文件  
>  Windows 系统下载 .msi 后缀的文件  
>  Linux 系统下载 .deb 后缀的文件

以 Windows 系统为例，以下为简要安装步骤：

1. 下载 `mirai-compose-<版本>.msi`
2. 双击运行安装程序，选择一个合适的文件夹，然后点击安装
3. 安装完毕后打开刚才指定的文件夹
4. 双击启动其中的 `mirai-compose.exe` 即可开始运行
5. 运行后点击左上角可以添加 QQ bot 账号

安装插件只需要将下载好的插件置于 plugins 目录，安装完毕后重启 mirai-compose 以生效。

## 解决问题

请先阅读 [常见问题](Questions.md)  。

如果遇到使用问题或想提建议，可以在 [issues](https://github.com/mamoe/mirai/issues)
发表。也可以在[论坛](https://mirai.mamoe.net/)交流想法。


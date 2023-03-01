# Mirai - UserManual

这里是 Mirai 用户手册。本文面向对开发并不熟悉而希望使用 Mirai 提供的QQ机器人服务的用户  
如果你要开发Mirai插件或参与贡献Mirai项目，请先阅读 [开发文档](README.md)  

## 启动 Mirai

想要部署并使用 Mirai QQ机器人框架，只需要启动 Mirai 控制台（即 Mirai Console），  
它自带一些基础功能，也可以加载社区提供的插件。

Mirai 控制台现在有两个版本，Mirai 插件在这两个版本的 Mirai Console 上都可以运行:

[MCLI-1.png]: .UserManual_images/MCLI-1.png

[MCPS-1.png]: .UserManual_images/MCPS-1.png

| 类型   | 长啥样?         | 好用吗?      | 怎么装?                  |
|:-----|:-------------|:----------|:----------------------|
| 纯控制台 | [MCLI-1.png] | 稳定，也适合服务器 | [使用纯控制台版本](#使用纯控制台版本) |
| 图形界面 | [MCPS-1.png] | 测试版，不稳定   | [使用图形界面版本](#使用图形界面版本) |

## 使用图形界面版本

前往 [sonder-joker/mirai-compose](https://github.com/sonder-joker/mirai-compose/releases)  
下载适合你的系统的压缩包，  
>  MAC系统下载.dmg后缀的文件  
>  Windows系统下载.msi后缀的文件  
>  Linux系统下载.deb后缀的文件  

以Windows系统为例，下载 mirai-compose-版本.msi之后，  
双击运行安装程序，然后选择一个合适的文件夹，然后点击安装。  
安装完毕后打开刚才指定的文件夹，  
双击启动其中的 **mirai-compose.exe** 即可开始运行。  
运行后点击左上角可以添加QQ Bot账号。  
安装插件的话把插件往plugins文件夹里扔就可以了，记得重启Mirai面板哦。  

## 使用纯控制台版本

### 详细教程

请查看 [ConsoleTerminal.md](ConsoleTerminal.md)

### 省流教程

1. 前往 [iTXTech/mcl-installer](https://github.com/iTXTech/mcl-installer/releases) 下载适合您系统的最新版本的MCL安装器
2. 创建个文件夹把MCL安装器往里面一扔
3. 双击安装器，一路回车，安装完毕
4. 运行mcl.cmd即可启动MCL控制台

安装插件很简单，把下载好的插件往plugins目录扔，完事重启一下控制台就好了  
其他详细的自己看文档去(  

## 解决问题

如果遇到使用问题或想提建议，可以在 [issues](https://github.com/mamoe/mirai/issues)
发表。也可以在[论坛](https://mirai.mamoe.net/)交流想法。


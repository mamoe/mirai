# Mirai - UserManual

Mirai 用户手册。本文面向对开发不熟悉而希望使用 Mirai 的用户。如果你要开发，请先阅读 [开发文档](README.md)。

## 启动 Mirai

使用 Mirai，一般人要启动的是 Mirai 控制台（即 Mirai Console），它可以加载插件。

Mirai 控制台现在有两个版本，插件在这两个版本的 Mirai Console 上都可以运行:

[MCLI-1.png]: .UserManual_images/MCLI-1.png

[MCPS-1.png]: .UserManual_images/MCPS-1.png

| 类型   | 长啥样?         | 好用吗?      | 怎么装?                  |
|:-----|:-------------|:----------|:----------------------|
| 纯控制台 | [MCLI-1.png] | 稳定，也适合服务器 | [使用纯控制台版本](#使用纯控制台版本) |
| 图形界面 | [MCPS-1.png] | 测试版，不稳定   | [使用图形界面版本](#使用图形界面版本) |

## 使用图形界面版本

前往 [sonder-joker/mirai-compose](https://github.com/sonder-joker/mirai-compose/releases)
下载适合你的系统的压缩包，解压到一个文件就可以使用。

## 使用纯控制台版本

### 安装

可以使用[脚本](https://mirai.mamoe.net/assets/uploads/files/1618372079496-install-20210412.cmd)
自动安装 32 位带 HTTP 插件的版本，也可以使用安装器个性化安装：

1. 访问 [iTXTech/mcl-installer](https://github.com/iTXTech/mcl-installer/releases)；
2. 下载适合你的系统的可执行文件；
3. 在一个新文件夹存放这个文件，运行它；
4. 通常可以一路回车使用默认设置完成安装，安装完成后程序自动退出；
5. 运行 `mcl.cmd` 启动，成功后会看到绿色的 `mirai-console started successfully`。

### 了解运行环境

安装时自动下载了 Mirai Console
启动器（简称 [MCL](https://github.com/iTXTech/mirai-console-loader)）。

启动器会帮你准备运行环境，下载和更新 Mirai 核心。你也可以使用启动器下载一些插件（见下文）。

第一次运行 `mcl.cmd` 时会初始化运行环境。下表说明了各个文件夹的用途。

|   文件夹名称   | 用途                  |
|:---------:|:--------------------|
| `scripts` | 存放启动器的脚本，一般不需要在意他们  |
| `plugins` | 存放插件                |
|  `data`   | 存放插件的数据，一般不需要在意它们   |
| `config`  | 存放插件的配置，可以打开并修改配置   |
|  `logs`   | 存放运行时的日志，日志默认保留 7 天 |

> 可以在[这里](https://github.com/iTXTech/mirai-console-loader)查看 MCL 详细用法

### 下载和安装插件

刚刚装好的 Mirai Console 是没有任何功能的。功能将由插件提供。

#### 如何安装官方插件

Mirai 官方提供两个插件：

- [chat-command](https://github.com/project-mirai/chat-command):
  允许在聊天环境通过以 "/" 起始的消息执行指令
- [mirai-api-http](https://github.com/project-mirai/mirai-api-http)：提供
  HTTP 支持，允许使用其他编程语言的插件

打开命令行 (Windows 系统按住Shift+鼠标右键，点击"在此处打开 PowerShell"),  
可以使用 MCL 自动安装这些插件如：

```
./mcl --update-package net.mamoe:mirai-api-http --type plugin --channel stable-v2

./mcl --update-package net.mamoe:chat-command --type plugin --channel stable
```

注意: 插件有多个频道, `--channel stable` 表示使用名为 `stable` 的频道. 不同的插件可能会设置不同的频道, 具体需要使用哪个频道可参考特定插件的说明 (很多插件会单独说明要如何安装它们, 因此不必过多考虑).

详细文档：[MCL/scripts](https://github.com/iTXTech/mirai-console-loader/blob/master/scripts/README.md)

#### 在哪找社区插件

- Mirai
  官方论坛 [Mirai Forum](https://mirai.mamoe.net/category/11/%E6%8F%92%E4%BB%B6%E5%8F%91%E5%B8%83)

> *我们还正在建设插件中心，完成后将会简化寻找插件的工作*

#### 如何安装社区插件

如果是 JAR 文件的插件，放入 `plugins` 即可。其他插件一般都有特殊说明如何使用，请参考它们的说明。

#### 推荐安装的插件

- [chat-command](https://github.com/project-mirai/chat-command):
  不安装此环境不能在聊天环境中执行命令
- [LuckPerms-Mirai](https://github.com/Karlatemp/LuckPerms-Mirai) (*
  社区*): 易用的高级高效率权限组插件, 适合权限分配模型比较复杂的情况
- [mirai-api-http](https://github.com/project-mirai/mirai-api-http)：提供
  HTTP 支持，允许使用其他编程语言的插件

### 使用控制台指令

启动 `mcl.cmd` 就会看到控制台。在控制台可以输入指令，按回车执行这条指令。

Mirai Console 内置一些指令，输入 `?` 并回车可以查看指令列表。

一些常用指令介绍在[这里](/mirai-console/docs/BuiltInCommands.md#mirai-console---builtin-commands)
。

#### 在群聊中使用命令 (权限授予)

要允许从 QQ 聊天环境中使用各种命令, 你 **必须** 完成以下的配置

1. 安装 [chat-command](https://github.com/project-mirai/chat-command)
2. 完成命令执行权限授予

> 关于不同的权限系统, 授予权限的方式, 或者授予权限的命令格式, 可能有所不一样
>
> 当使用 `非内置权限系统` 时, 具体的权限管理相关命令以相关的权限系统的文档为准
> > 如 `LuckPerms-Mirai` 的权限管理命令为 `/lp` 而不是 `/permission`

要完成权限授予, 你必须通过在控制台执行
[`/permission permit [target] [permission]`](/mirai-console/docs/BuiltInCommands.md#permissioncommand)
来授予其他人执行相关命令的权限, 需要执行的权限一般情况在插件的介绍页都会给明

详见 [`PermissionCommand`](/mirai-console/docs/BuiltInCommands.md#permissioncommand)

## 解决问题

如果遇到使用问题或想提建议，可以在 [issues](https://github.com/mamoe/mirai/issues)
发表。也可以在[论坛](https://mirai.mamoe.net/)交流想法。


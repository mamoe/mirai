# Mirai - Console Terminal

本文介绍如何使用 Mirai Console 的纯控制台前端，即 `mirai-console-terminal`
。本文部分内容实际上对所有前端都通用。

本文面向用户，若要开发 Mirai
Console，可参考 [Console 开发文档](../mirai-console/docs/README.md)。

本文假设你使用 Windows 操作系统。但 Mirai Console 并不仅限于 Windows
平台使用，在其他操作系统上的使用方法应当是类似的。

**重要**：关闭 Mirai Console 需要通过 `stop` 命令关闭。  
直接关闭窗口会导致数据损坏、数据丢失、系统崩溃等错误。

安装
----

可以使用 [脚本](https://mirai.mamoe.net/assets/uploads/files/1618372079496-install-20210412.cmd)
自动安装 32 位带 HTTP 插件的版本，也可以使用安装器个性化安装：

[iTXTech/mcl-installer]: https://github.com/iTXTech/mcl-installer/releases

1. 访问 [iTXTech/mcl-installer]；
2. 下载适合你的系统的可执行文件；
3. 在一个新文件夹存放这个文件，运行它；
4. 通常可以一路回车使用默认设置完成安装，安装完成后程序自动退出；
5. 运行 `mcl.cmd` 启动，成功后会看到绿色的 `mirai-console started successfully`。

了解运行环境
----


安装时自动下载了 Mirai Console
启动器（简称 [MCL](https://github.com/iTXTech/mirai-console-loader)）。

启动器会帮你准备运行环境，下载和更新 Mirai 核心。你也可以使用启动器下载一些插件（见下文）。

第一次运行 `mcl.cmd` 时会初始化运行环境。下表说明了各个文件夹的用途。

MCL 只是启动器，没有机器人功能。MCL 支持从远程仓库下载插件，并启动 Mirai Console
终端版（`mirai-console-terminal`）。

如果遇到启动器问题，请提交至 [iTXTech/mirai-console-loader](https://github.com/iTXTech/mirai-console-loader)
。

|           文件夹名称           | 用途                   |
|:-------------------------:|:---------------------|
|          `data`           | 存放插件的数据，一般不需要在意它们    |
|         `config`          | 存放插件的配置，可以打开并修改配置    |
|          `logs`           | 存放运行时的日志，日志默认保留 7 天  |
|          `libs`           | 存放 mirai-core 等核心库文件 |
|         `plugins`         | 存放插件                 |
|    `plugin-libraries`     | 存放插件的库缓存             |
| `plugin-shared-libraries` | 存放插件的公共库             |
|         `modules`         | 存放启动器的拓展模块           |

> 可以在[这里](https://github.com/iTXTech/mirai-console-loader)查看 MCL 详细用法


了解插件
----

Mirai Console 原生支持 JAR 文件插件。一般插件的后缀为 `.mirai2.jar`（新版本）或 `.mirai.jar`
（旧版本）。

将插件 JAR 放在 `plugins` 目录中，重启 Mirai Console 就会自动扫描并加载。

Mirai Console

下载和安装插件
----

刚刚装好的 Mirai Console 没有功能，功能将由插件提供。

### 使用 MCL 自动安装插件

### 如何安装官方插件

Mirai 官方提供两个插件：

- [chat-command](https://github.com/project-mirai/chat-command):
  允许在聊天环境通过以 "/" 起始的消息执行指令（也可以配置前缀）
- [mirai-api-http](https://github.com/project-mirai/mirai-api-http)：提供
  HTTP 支持，允许使用其他编程语言的插件

打开命令行 (Windows 系统在文件夹按住 Shift 单击鼠标右键，点击 "在此处打开 PowerShell")，
可以使用 MCL 自动安装这些插件，例如：

安装 mirai-api-http 的 2.x 版本：

```powershell
./mcl --update-package net.mamoe:mirai-api-http --type plugin --channel maven-stable
```

安装 chat-command：

```powershell
./mcl --update-package net.mamoe:chat-command --type plugin --channel maven-stable
```

注意：插件有多个频道，`--channel maven-stable` 表示使用从 `maven` 更新的 `stable`（稳定）的频道。不同的插件可能会设置不同的频道，
具体需要使用哪个频道可参考特定插件的说明 (很多插件会单独说明要如何安装它们, 因此不必过多考虑)。

详细文档：[MCL 命令行参数](https://github.com/iTXTech/mirai-console-loader/blob/master/cli.md)

### 在哪找社区插件

- Mirai
  官方论坛 [Mirai Forum](https://mirai.mamoe.net/category/11/%E6%8F%92%E4%BB%B6%E5%8F%91%E5%B8%83)

> *我们还正在建设插件中心，完成后将会简化寻找插件的工作*

### 如何安装社区插件

如果是 JAR 文件的插件，放入 `plugins` 即可。其他插件一般都有特殊说明如何使用，请参考它们的说明。

注意，mirai 在 2.11 时修改了加载策略。请尽量使用 `mirai2.jar` 后缀版本的插件

### 常用的插件

- [chat-command](https://github.com/project-mirai/chat-command):
  不安装此插件不能在聊天环境中执行命令

- [mirai-api-http](https://github.com/project-mirai/mirai-api-http): 
  提供 HTTP 支持，允许使用其他编程语言的插件

- [mirai-silk-converter](https://github.com/project-mirai/mirai-silk-converter):
  可以自动将 `wav`, `mp3` 等格式转换为语音所需格式 `silk`

- [LuckPerms-Mirai](https://github.com/Karlatemp/LuckPerms-Mirai): 
  高级权限组插件，适合权限分配模型比较复杂的情况，并且可以提供网页UI的权限编辑器 (指令 `lp editor`)

- [mirai-login-solver-sakura](https://github.com/KasukuSakura/mirai-login-solver-sakura):
  验证处理工具，主要是为了优化和方便处理各种验证码

使用控制台指令
-----

启动 `mcl.cmd` 就会看到控制台。在控制台可以输入指令，按回车执行这条指令。

Mirai Console 内置一些指令，输入 `?` 并回车可以查看指令列表。

一些常用指令介绍在[这里](/mirai-console/docs/BuiltInCommands.md#mirai-console---builtin-commands)
。

### 在聊天框中使用命令 (权限授予)

要允许从 QQ 聊天环境中使用各种命令, 你 **必须** 完成以下的配置：

1. 安装 [chat-command](https://github.com/project-mirai/chat-command)
2. 完成命令执行权限授予

> 关于不同的权限系统，授予权限的方式或者授予权限的命令格式可能有所不一样。
>
> 当使用非内置权限系统时，具体的权限管理相关命令以相关的权限系统的文档为准
> > 如 `LuckPerms-Mirai` 插件的权限管理命令为 `/lp` 而不是 `/permission`

要完成权限授予，你必须通过在控制台执行
[`/permission permit [target] [permission]`](/mirai-console/docs/BuiltInCommands.md#permissioncommand)
来授予其他人执行相关命令的权限，需要执行的权限一般情况在插件的介绍页都会给明。

详见 [`PermissionCommand`](/mirai-console/docs/BuiltInCommands.md#permissioncommand)
。

### 指令参数智能解析

Console 会自动根据语境推断指令参数的含义。

假设一条禁言指令：

```
/mute <target> <duration>    # 为 target 设置 duration 秒的禁言
```

`<target>` 为目标群成员，`<duration>` 为禁言时间秒数。

若在群内发送消息执行指令 `/mute 123 60`，`123` 将会被首先看作是群员 QQ 号码寻找群员。
若未找到，则会将 `123` 看作是群员的名片重新寻找。

群名片和昵称搜索是模糊匹配的，优先取用不会产生歧义的最相似的匹配。
例如 `hello` 能匹配 `hella`，匹配率 80%；`hello` 匹配 `hel` 匹配率 60%。由于这两个匹配率都超过
60%，它们都会成为候选；由于它们之间相差超过 10%，Mirai 会认为这没有歧义，选用 `hella`。

在一个群内也可以引用另一个群内的成员，使用 `群号码.群员号码` 格式。若没有 `群号码` 则会使用指令执行人所在的群。

在有多个机器人账号登录的情况下，可以使用 `机器人号码.群员号码` 的格式来指定某个机器人的群员。如果只有一个机器人则只需要使用 `群员号码`。

在控制台执行指令时，需要提供群号码才能引用到群员。

下面将列举智能解析支持的格式列表。

#### 好友

| 格式         | 示例              | 说明                   |
|------------|-----------------|----------------------|
| 机器人号码.好友号码 | `123456.987654` | 一个机器人的一个好友           |
| 好友号码       | `987654`        | 当前唯一在线机器人的一个好友       |
| `~`        | `~`             | 仅聊天环境下，指代指令调用人自己作为好友 |

#### 群员

| 格式             | 示例                     | 说明                   |
|----------------|------------------------|----------------------|
| 机器人号码.群号码.群员号码 | `123456.123456.987654` | 一个机器人的一个群中的一个群员      |
| 机器人号码.群号码.群员名片 | `123456.123456.Alice`  | 一个机器人的一个群中的一个群员      |
| 提及群员           | `@Cinnamon`            | 一个机器人的一个群中的一个群员      |
| 机器人号码.群号码.$    | `123456.123456.$`      | 一个机器人的一个群中的随机群员      |
| 群号码.群员号码       | `123456.987654`        | 当前唯一在线机器人的一个群的一个群员   |
| 群号码.群员名片       | `123456.Alice`         | 当前唯一在线机器人的一个群的一个群员   |
| 群号码.$          | `123456.$`             | 当前唯一在线机器人的一个群的随机群员   |
| 群员号码           | `987654`               | 仅聊天环境下，当前群的一个群成员     |
| 群员名片           | `Alice`                | 仅聊天环境下，当前群的一个群成员     |
| `$`            | `$`                    | 仅聊天环境下，当前群的随机群成员     |
| `~`            | `~`                    | 仅聊天环境下，指代指令调用人自己作为群员 |

#### 群

| 格式        | 示例              | 说明            |
|-----------|-----------------|---------------|
| 机器人号码.群号码 | `123456.987654` | 一个机器人的一个群     |
| 群号码       | `987654`        | 当前唯一在线机器人的一个群 |
| `~`       | `~`             | 仅聊天环境下，指代当前群聊 |

配置
----

Mirai Console 支持一些自定义配置。各项配置可以在 `config` 目录中找到。

### 自动登录

修改 `AutoLogin.yml` 可配置自动登录。也可以使用 `/autologin` 指令。

### 指令前缀

可以在 `Command.yml` 配置指令前缀，默认为 `/`。注意，部分指令可不需要前缀也能使用，这取决于插件开发者的选择。

### 管理日志

Mirai Console 会记录运行时的日志并保存到 `logs` 目录中。

可以参考[日志文档](../mirai-console/docs/Logging.md)了解如何配置日志的详略程度。

若要向插件开发者提交问题，建议将日志等级调整为 `ALL` 并复现问题后将当天日志一并提交。

### 配置权限

`PermissionService.yml` 包含权限授予信息。通常建议使用指令 `/perm` 来修改权限，而不建议直接修改配置。

### 远程仓库

`PluginDependencies.yml` 包含对远程仓库的配置，通常不需要修改，除非某个插件要求。



帮助改善文档
-----

如果你认为本文档还需要覆盖一些内容，请在 [issues](https://github.com/mamoe/mirai/issues/new/choose)
提交建议。

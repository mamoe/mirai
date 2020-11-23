# Mirai Console - Run

Mirai Console 可以独立启动，也可以被嵌入到某个应用中。

## 使用工具自动独立启动

官方: https://github.com/iTXTech/mirai-console-loader  
第三方: https://github.com/LXY1226/MiraiOK

## 手动配置独立启动

### 环境
- JRE 11+ / JDK 11+

### 准备文件

要启动 Mirai Console，你需要：
- mirai-core-qqandroid 
- mirai-console 后端
- mirai-console 任一前端
- 相关依赖

只有 mirai-console 前端才有入口点 `main` 方法。目前只有一个 terminal 前端可用。

#### 从JCenter下载模块

mirai 在版本发布时会将发布的构建存放与 [mirai-bintray-repo]。

- mirai-core 会提供 [mirai-core-all]
- mirai-console 与其各个模块都会提供 `-all` 的 Shadowed 构建

```shell script
# 注: 自行更换对应版本号

# Download Mirai Core All

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-core-all/1.3.3/mirai-core-all-1.3.3-all.jar -o mirai-core-all-1.3.3.jar

# Download Mirai Console All

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-console/1.0.0/mirai-console-1.0.0-all.jar -o mirai-console-1.0.0.jar

# Download Mirai Console Terminal

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-console-terminal/1.0.0/mirai-console-terminal-1.0.0-all.jar -o mirai-console-terminal-1.0.0.jar

```

### 启动 mirai-console-terminal 前端

1. 下载如下三个模块的最新版本文件并放到一个文件夹内 (如 `libs`)(详见 [下载模块](#从JCenter下载模块))：
   - mirai-core-all
   - mirai-console
   - mirai-console-terminal

2. 创建一个新的文件, 名为 `start-mirai-console.bat`/`start-mirai-console.ps1`/`start-mirai-console.sh`

Windows CMD:
```shell script
@echo off
title Mirai Console
java -cp "./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader %*
pause
```

Windows PowerShell:
```shell script
$Host.UI.RawUI.WindowTitle = "Mirai Console"
java -cp "./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader $args
pause
```

Linux:
```shell script
#!/usr/bin/env bash
echo -e '\033]2;Mirai Console\007'
java -cp "./libs/*" net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader $*
```

然后就可以开始使用 mirai-console 了

#### mirai-console-terminal 前端参数
使用 `./start-mirai-console --help` 查看 mirai-console-terminal 支持的启动参数


### 启动 mirai-console-pure 前端

与启动 `mirai-console-terminal` 前端大体相同
- 下载 `mirai-console-terminal` 改成下载 `mirai-console-pure`
- 启动入口从 `net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader` 改成 `net.mamoe.mirai.console.pure.MiraiConsolePureLoader`


[mirai-repo]: https://github.com/project-mirai/mirai-repo/tree/master/shadow
[mirai-bintray-repo]: https://bintray.com/him188moe/mirai
[mirai-core-all]: https://bintray.com/him188moe/mirai/mirai-core-all

# Mirai Console - Run

Mirai Console 可以独立启动，也可以被嵌入到某个应用中。

## 使用第三方工具自动启动

## 独立启动

### 环境
- JDK 11

### 准备文件

要启动 Mirai Console，你需要：
- mirai-core-qqandroid 
- mirai-console 后端
- mirai-console 任一前端
- 相关依赖

只有 mirai-console 前端才有入口点 `main` 方法。目前只有一个 pure 前端可用。

### 启动 mirai-console-pure 前端

mirai 在版本发布时会同时发布打包依赖的 Shadow JAR，存放在 [mirai-repo]。

1. 在 [mirai-repo] 下载如下三个模块的最新版本文件：
   - []

[mirai-repo]: https://github.com/project-mirai/mirai-repo/

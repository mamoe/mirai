<div align="center">
   <img width="160" src="http://img.mamoe.net/2020/02/16/a759783b42f72.png" alt="logo"></br>


   <img width="95" src="http://img.mamoe.net/2020/02/16/c4aece361224d.png" alt="title">

----
Mirai 是一个在全平台下运行，提供 QQ Android 和 TIM PC 协议支持的高效率机器人库

这个项目的名字来源于
     <p><a href = "http://www.kyotoanimation.co.jp/">京都动画</a>作品<a href = "https://zh.moegirl.org/zh-hans/%E5%A2%83%E7%95%8C%E7%9A%84%E5%BD%BC%E6%96%B9">《境界的彼方》</a>的<a href = "https://zh.moegirl.org/zh-hans/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5">栗山未来(Kuriyama <b>Mirai</b>)</a></p>
     <p><a href = "https://www.crypton.co.jp/">CRYPTON</a>以<a href = "https://www.crypton.co.jp/miku_eng">初音未来</a>为代表的创作与活动<a href = "https://magicalmirai.com/2019/index_en.html">(Magical <b>Mirai</b>)</a></p>
图标以及形象由画师<a href = "">DazeCake</a>绘制
</div>


# mirai-console
高效率插件支持 QQ 机器人框架, 机器人核心来自 [mirai](https://github.com/mamoe/mirai)

## 模块说明

console 由后端和前端一起工作. 使用时必须选择一个前端.

**注意：`mirai-console` 后端和 pure 前端正在进行完全的重构，master 分支将不再维护。**  
**`mirai-console` 将在短时间内不可用。**

后端:
- [`mirai-console`](backend/mirai-console/): console 的后端, 包含插件管理, 指令系统, 配置系统. 没有入口程序. 

前端:
- [`mirai-console-pure`](frontend/mirai-console-pure): console 的轻量命令行前端
- [`mirai-console-graphical`](frontend/mirai-console-graphical): console 的 JavaFX 图形化界面前端. (实验性)
- [`mirai-console-terminal`](frontend/mirai-console-terminal): console 的 Unix 终端界面前端. (实验性)
- [`MiraiAndroid`](https://github.com/mzdluo123/MiraiAndroid): console 的 Android APP 前端.


[`mirai-console-wrapper`](https://github.com/mamoe/mirai-console-wrapper): console 启动器. 可根据用户选择从服务器下载 console 后端, mirai-core, 和指定的前端并启动.

## 使用

### Android

[MiraiAndroid](https://github.com/mzdluo123/MiraiAndroid) 提供在 Android 平台使用 mirai-console 插件的能力，同时拥有一个便于使用的 Lua 接口

[项目详细](https://github.com/mzdluo123/MiraiAndroid)

### Windows / Linux / Mac

**推荐任何人都使用全自动的 [LXY1226/MiraiOK](https://github.com/LXY1226/miraiOK) 一键启动器**

也可以使用不推荐的 mirai-console-wrapper 启动器.

1. 安装 JRE (Java 运行环境):
   -  若使用图形界面, 至少需要 JRE 11 并带有 JavaFX 11, 且不推荐使用 12 或更高版本.
   -  若使用命令行或终端, 至少需要 JRE 8.
   -  可以在 [华为镜像源](https://repo.huaweicloud.com/java/jdk/) 下载 JDK 安装. (JDK 包含 JRE 和开发工具)
2. 下载 `mirai-console-wrapper-x.x.x.jar`
3. 参照 [wrapper 命令行参数](https://github.com/mirai/mirai-console-wrapper/README.md#命令行参数), 运行 `$ java -jar mirai-console-wrapper-x.x.x.jar`

### 插件开发与获取

mirai-console 内建 Jar 插件支持.

**mirai-console 目前仍为实验性阶段, 任何功能和 API 都不保证稳定性. 任何 API 都可能在没有警告的情况下修改.**

(实验性) [插件中心](https://github.com/mamoe/mirai-plugins)  
[mirai-console插件开发快速上手](PluginDocs/ToStart.MD) 

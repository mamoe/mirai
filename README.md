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


## 使用

### Android

[MiraiAndroid](https://github.com/mzdluo123/MiraiAndroid) 提供在 Android 平台使用 mirai-console 插件的能力，同时拥有一个便于使用的 Lua 接口

[项目详细](https://github.com/mzdluo123/MiraiAndroid)

### Windows / Linux / Mac

**使用 [LXY1226/MiraiOK](https://github.com/LXY1226/miraiOK) 一键启动器, 自动配置运行环境**

### 插件开发与获取

mirai-console 内建 Jar 插件支持.

**mirai-console 目前仍为实验性阶段, 任何功能和 API 都不保证稳定性. 任何 API 都可能在没有警告的情况下修改.**

(实验性) [插件中心](https://github.com/mamoe/mirai-plugins)  
[mirai-console插件开发快速上手](PluginDocs/ToStart.MD) 

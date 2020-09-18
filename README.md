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
[ ![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-console/images/download.svg?) ](https://bintray.com/him188moe/mirai/mirai-console/)

高效率插件支持 QQ 机器人框架, 机器人核心来自 [mirai](https://github.com/mamoe/mirai)

## 模块说明

console 由后端和前端一起工作. 使用时必须选择一个前端.

- `mirai-console`: console 的后端, 包含插件管理, 指令系统, 配置系统.



前端:

- `mirai-console-terminal`: console 的 Unix 终端界面前端.
- `mirai-console-graphical`: console 的 JavaFX 图形化界面前端. (开发中)


**注意：`mirai-console` 后端和 terminal 前端正在进行完全的重构, 所有 API 都不具有稳定性**

### 使用

**查看示例插件**: [mirai-console-example-plugin](https://github.com/Him188/mirai-console-example-plugin)

正在更新中的文档：[参考文档](docs/README.md)

#### Gradle
`CORE_VERSION`: [ ![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg?) ](https://bintray.com/him188moe/mirai/mirai-core/)
`CONSOLE_VERSION`: [ ![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-console/images/download.svg?) ](https://bintray.com/him188moe/mirai/mirai-console/)


build.gradle.kts
```kotlin
repositories {
  jcenter()
}

dependencies {
  implementation("net.mamoe:mirai-core:$CORE_VERSION") // mirai-core 的 API
  implementation("net.mamoe:mirai-console:$CONSOLE_VERSION") // 后端
  
  testImplementation("net.mamoe:mirai-console-terminal:$CONSOLE_VERSION") // 前端, 用于启动测试
}
```

#### Maven
同理 Gradle

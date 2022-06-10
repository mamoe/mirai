# 贡献

**感谢你来到这里和你对 mirai 做的所有贡献。**

mirai 欢迎一切形式的代码贡献。你可以通过以下几种途径向 mirai 贡献。

## 主仓库 `mirai-core`

### 构建项目

#### 更新 submodules

mirai git 仓库含有 submodule, 请在 clone 时使用 `--recursive` 参数, 或在 clone 后使用如下命令更新 submodule:
```shell script
git submodule init
git submodule update
```

#### 安装 JDK

mirai 2.9.0 在如下环境测试可以编译:
- macOS 12.0.1, AdoptOpenJDK 17 aarch64, Gradle 7.2, Kotlin 1.6.0
- macOS 12.0.1, Amazon Corretto 11 amd64, Gradle 7.2, Kotlin 1.6.0

若在其他环境下无法正常编译, 请尝试选择上述一个环境配置.

#### 运行 Gradle 构建

项目首次初始化和构建可能要花费较长时间。

- 要构建项目, 请运行 `gradlew assemble`
- 要运行测试, 请运行 `gradlew check`
- 要构建项目并运行测试, 请运行 `gradlew build`

### 分支

- `1.x`: 1.x 版本的开发 (已停止)
- `dev`: 2.0 版本的开发
- `-release` 后缀: 基于[版本规范](docs/Evolution.md#版本规范), 用于从 `dev` 中筛选 bugfix 并发布一个版本的 patch 的版本. 如 `2.0-release` 会包含 `2.0.x` 版本的更新.

**请基于 `dev` 分支进行修改**

### 能做什么?

- 维护社区: 可以为 [mirai-console](/mirai-console) 编写插件, 并发布到论坛

- 代码优化: 优化任何功能设计或实现, 或是引入一个新的设计
- 解决问题: 在 [issues](https://github.com/mamoe/mirai/issues) 查看 mirai 正遇到的所有问题, 或在 [里程碑](https://github.com/mamoe/mirai/milestones) 查看版本计划. 所有没有 assignee 的 issue 都处于
- 协议支持: [添加新协议支持](#添加协议支持)

### 加入开发组

你可以随时提交 PR 解决任何问题。而若有兴趣，我们也欢迎你加入开发组，请联系 support@mamoe.net

[mirai-compose]: https://github.com/sonder-joker/mirai-compose
[plugin-center 服务端]: https://github.com/project-mirai/mirai-plugin-center
[mirai-api-http]: https://github.com/project-mirai/mirai-api-http
[project-mirai/docs]: https://github.com/project-mirai/docs
[docs.mirai.mamoe.net]: https://docs.mirai.mamoe.net


|           名称           |                                                   描述                                                   |
|:------------------------:|:------------------------------------------------------------------------------------------------------:|
|  core 和 console 日常更新  |           在 milestone 安排的日常更新。我们目前版本速度是一个月到两个月发布一个次版本（2.x)。需要日常的开发。           |
|       console 后端       |                       架构稳定，现在格外需要在易用性上的提升，首先需要一个优化方案，再实现它们。                       |
|       console 文档       |                   根据用户反馈，现在文档十分缺少。需要以用户的身份体验过 console 的人编写用户文档。                   |
| 图形前端 [mirai-compose]  |                       各功能都缺目前尤其缺少对接 console PluginConfig 的图形化配置的实现。                       |
|  [plugin-center 服务端]   |                    插件中心正在建设中。后端 Spring，前端 Vuetify。由于开发人员学业繁忙，暂搁置。                    |
|    plugin-center 社区    | 插件中心计划支持所有语言的插件，因此需要与社区 SDK 作者沟通并帮助它们接入 Console 的 PluginLoader API 和插件中心的要求。 |
| plugin-center console 端 |               需要评估现在 console 架构是否足够支持插件中心及所有语言插件的管理，实现与插件中心的对接。                |
|   plugin-center gradle   |                              对接插件中心，实现通过 Task 上传插件。还没有开始做。                               |
|   mirai-console-loader   |                 console 启动器。对接插件中心的 API，支持下载和更新插件等。不确定之后是否会有人实现。                 |
|         IDE 插件         |               IntelliJ IDEA 的插件的工作。可以为 mirai 框架添加检查等功能。这个部分目前基本满足需求。                |
|   [mirai-api-http] v2    |                                                日常维护。                                                |
|   [project-mirai/docs]   |  用户友好文档自动部署，使用 VuePress , 部署于 [docs.mirai.mamoe.net]，目前还有部分超链接错误的问题。               |


### 里程碑

[里程碑](https://github.com/mamoe/mirai/milestones) 为各版本的开发计划. 在完成所有任务后就会发布该版本.

`Backlog` 为没有设定目标版本的计划. 如果有相关 PR, 这些计划就可能会被确定到一个最近的版本.

### 添加协议支持

请查看 [PacketFactory.kt](mirai-core/src/commonMain/kotlin/network/protocol/packet/PacketFactory.kt) 了解网络层架构.  
参考现有的 `PacketFactory` 实现和一些有关协议的 PR (带有 `protocol` 标签) 了解如何添加新的 `PacketFactory`.


### 开发 mirai-core

- 使用 IntelliJ IDEA 或 Android Studio
- 安装 IDE 插件 [kotlin-jvm-blocking-bridge](https://github.com/Him188/kotlin-jvm-blocking-bridge/blob/master/README-chs.md#%E5%AE%89%E8%A3%85-intellij-idea-%E6%88%96-android-studio-%E6%8F%92%E4%BB%B6)
- 若要添加一个 suspend 函数, 请为它添加 `@JvmBlockingBridge`, 使用 [kotlin-jvm-blocking-bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge/blob/master/README-chs.md)
- 在 mirai-core 和 mirai-core-api 使用纯 Kotlin 实现
- 尽量不要引用新的库
- 遵守 Kotlin 官方代码规范（提交前使用 IDE 格式化代码 (commit 时勾选 'Reformat code')）
- 保证二进制兼容性: 在提交前执行 `./gradlew build`, 若有不兼容变更会得到错误。  
  如果你正在添加一个新功能，可以忽略这个错误，执行 `./gradlew clean apiDumpAll`。这将会生成 `*.api`，文件的变化反映了你的修改情况。将这些文件一并提交。 (详细了解 [Kotlin/binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator))
- 通过 GitHub 的 Pull Request 提交代码，很快就会有相关模块负责人员来审核


如果你不太保证自己能达到上述要求也没关系，mirai 感谢你的每一行代码，维护者会审核代码并尽可能帮助你。

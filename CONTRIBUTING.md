# 贡献

**感谢你来到这里和你对 mirai 做的所有贡献。**

mirai 欢迎一切形式的代码贡献。你可以通过以下几种途径向 mirai 贡献。

## 主仓库 `mirai-core`

### 构建
- 要构建项目, 请运行 `gradlew assemble`
- 要运行测试, 请运行 `gradlew test`
- 要构建项目并运行测试, 请运行 `gradlew build`
- 若要添加一个 suspend 函数, 请务必考虑 Java 兼容性, 使用 [kotlin-jvm-blocking-bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge/blob/master/README-chs.md)

### 分支

- `1.x`: 1.x 版本的开发 (已停止)
- `dev`: 2.0 版本的开发
- `master`: 最新稳定版
- `-release` 后缀: 基于[版本规范](docs/Evolution.md#版本规范), 用于从 `dev` 中筛选 bugfix 并发布一个版本的 patch 的版本. 如 `2.0-release` 会包含 `2.0.x` 版本的更新.

**请基于 `dev` 分支进行修改**

### 能做什么?

- 维护社区: 可以为 [mirai-console](https://github.com/mamoe/mirai-console) 编写插件, 并发布到 discussions

- 代码优化: 优化任何功能设计或实现, 或是引入一个新的设计（请先通过 issues 或 discussions 与维护者达成共识）
- 解决问题: 在 [issues](https://github.com/mamoe/mirai/issues) 查看 mirai 正遇到的所有问题, 或在 [里程碑](https://github.com/mamoe/mirai/milestones) 查看版本计划. 所有没有 assignee 的 issue 都处于
- 协议支持: [添加新协议支持](#添加协议支持)

### 里程碑

[里程碑](https://github.com/mamoe/mirai/milestones) 为各版本的开发计划. 在完成所有任务后就会发布该版本.

`Backlog` 为没有设定目标版本的计划. 如果有相关 PR, 这些计划就可能会被确定到一个最近的版本.

### 添加协议支持

请查看 [PacketFactory.kt](mirai-core/src/commonMain/kotlin/network/protocol/packet/PacketFactory.kt) 了解网络层架构.  
参考现有的 `PacketFactory` 实现和一些有关协议的 PR (带有 `protocol` 标签) 了解如何添加新的 `PacketFactory`.

> 如果你不熟悉 Kotlin 或不熟练 Kotlin 也没关系, 你的 PR 会首先被维护者审阅并会收到修改建议. mirai 感谢你的每一行代码并会尽可能帮助你.


### 注意事项
- 尽量不要引用新的库
- 遵守 Kotlin 官方代码规范（提交前使用 IDE 格式化代码 (commit 时勾选 'Reformat code')）
- 保证二进制兼容性: 在提交前执行 `gradlew build`, 若有不兼容变更会得到错误. 在提交时将 `binary-compatibility-validator.api` 一并提交 (如果有修改). (使用 [Kotlin/binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator))

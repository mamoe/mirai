# 贡献

**感谢你来到这里和你对 mirai 做的所有贡献。**

mirai 欢迎一切形式的代码贡献。你可以通过以下几种途径向 mirai 贡献。

## 主仓库 `mirai-core`

### 构建

mirai git 仓库含有 submodule, 请在 clone 时使用 `--recursive` 参数, 或在 clone 后使用如下命令更新 submodule:
```shell script
git submodule init
git submodule update
```

项目首次初始化和构建可能要花费较长时间。

- 要构建项目, 请运行 `gradlew assemble`
- 要运行测试, 请运行 `gradlew test`
- 要构建项目并运行测试, 请运行 `gradlew build`
- 若要添加一个 suspend 函数, 请务必考虑 Java 兼容性, 使用 [kotlin-jvm-blocking-bridge](https://github.com/mamoe/kotlin-jvm-blocking-bridge/blob/master/README-chs.md)

### 分支

- `1.x`: 1.x 版本的开发 (已停止)
- `dev`: 2.0 版本的开发
- `-release` 后缀: 基于[版本规范](docs/Evolution.md#版本规范), 用于从 `dev` 中筛选 bugfix 并发布一个版本的 patch 的版本. 如 `2.0-release` 会包含 `2.0.x` 版本的更新.

**请基于 `dev` 分支进行修改**

### 能做什么?

- 维护社区: 可以为 [mirai-console](https://github.com/mamoe/mirai-console) 编写插件, 并发布到论坛

- 代码优化: 优化任何功能设计或实现, 或是引入一个新的设计
- 解决问题: 在 [issues](https://github.com/mamoe/mirai/issues) 查看 mirai 正遇到的所有问题, 或在 [里程碑](https://github.com/mamoe/mirai/milestones) 查看版本计划. 所有没有 assignee 的 issue 都处于
- 协议支持: [添加新协议支持](#添加协议支持)

### 里程碑

[里程碑](https://github.com/mamoe/mirai/milestones) 为各版本的开发计划. 在完成所有任务后就会发布该版本.

`Backlog` 为没有设定目标版本的计划. 如果有相关 PR, 这些计划就可能会被确定到一个最近的版本.

### 添加协议支持

请查看 [PacketFactory.kt](mirai-core/src/commonMain/kotlin/network/protocol/packet/PacketFactory.kt) 了解网络层架构.  
参考现有的 `PacketFactory` 实现和一些有关协议的 PR (带有 `protocol` 标签) 了解如何添加新的 `PacketFactory`.


### 开发 mirai-core

- 使用 IntelliJ IDEA 或 Android Studio
- 安装 IDE 插件 [kotlin-jvm-blocking-bridge](https://github.com/Him188/kotlin-jvm-blocking-bridge/blob/master/README-chs.md#%E5%AE%89%E8%A3%85-intellij-idea-%E6%88%96-android-studio-%E6%8F%92%E4%BB%B6)
- 在 mirai-core 和 mirai-core-api 使用纯 Kotlin 实现
- 尽量不要引用新的库
- 遵守 Kotlin 官方代码规范（提交前使用 IDE 格式化代码 (commit 时勾选 'Reformat code')）
- 保证二进制兼容性: 在提交前执行 `gradlew build`, 若有不兼容变更会得到错误。  
  如果你正在添加一个新功能，可以忽略这个错误，执行 `gradlew clean apiDump`。这将会生成 `binary-compatibility-validator.api`，文件的变化反映了你的修改情况。将这些文件一并提交。 (详细了解 [Kotlin/binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator))
- 通过 GitHub 的 Pull Request 提交代码，很快就会有相关模块负责人员来审核


如果你不太保证自己能达到上述要求也没关系，mirai 感谢你的每一行代码，维护者会审核代码并尽可能帮助你。

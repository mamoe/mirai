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

- `master`: 最新稳定版
- `1.x`: 1.x 现有版本的开发
- `dev`: 2.0 重构版本的开发

**请基于 `dev` 分支进行修改**

### 能做什么?

- 代码优化: 优化任何功能设计或实现, 或是引入一个新的设计（请先通过 issue 与维护者达成共识）
- 解决问题: 在 [issues](https://github.com/mamoe/mirai/issues) 查看 mirai 正遇到的所有问题, 或在 [里程碑](https://github.com/mamoe/mirai/milestones) 查看版本计划
- 协议支持: 添加新协议支持

### 注意事项
- 尽量不要引用新的库
- 遵守 Kotlin 官方代码规范（提交前使用 IDE 格式化代码 (commit 时勾选 'Reformat code')）

## 社区

插件社区不要求太高的代码质量，任何人都可以帮助 mirai。  
可以为 [mirai-console](https://github.com/mamoe/mirai-console) 编写插件, 并发布到 Discussions

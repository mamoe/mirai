# 贡献

**感谢你来到这里和你对 mirai 做的所有贡献。**

mirai 欢迎一切形式的代码贡献。你可以通过以下几种途径向 mirai 贡献。

## 主仓库 `mirai-core`

**阅读文档**： [docs/mirai.md](docs/mirai.md)

### 能做什么?

**请基于 `master` 分支进行文档修改; 基于 `dev` 分支进行其他修改 (否则你的修改可能被关闭或不会立即合并)**

- 代码优化: 优化任何功能设计或实现, 或是引入一个新的设计（请先通过 issue 与维护者达成共识）
- 解决问题: 在 [issues](https://github.com/mamoe/mirai/issues) 查看 mirai 正遇到的所有问题, 或在 [里程碑](https://github.com/mamoe/mirai/milestones) 查看版本计划
- 协议支持: 添加新协议支持, 但需要避免添加 [mirai.md#声明](docs/mirai.md#L9) 中的内容

### 注意事项
- 尽量不要引用新的库
- 遵守 Kotlin 官方代码规范（提交前使用 IDE 格式化代码 (commit 时勾选 'Reformat code')）
- 不要手动拆解数据包. 请一定使用 `kotlinx.serialization` 拆解 ProtoBuf, 使用 [`jcekt`](https://github.com/him188/jcekt) 拆解 Tars 数据包, 使用 `kotlinx.serialization` 拆解 Json 数据.

## 社区

插件社区不要求太高的代码质量，任何人都可以帮助 mirai。  
可以为 [mirai-console](https://github.com/mamoe/mirai-console) 编写插件, 并发布到社区网站: (建设中)

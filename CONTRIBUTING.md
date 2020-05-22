# 贡献

感谢你来到这里和你对 mirai 做的所有贡献。

mirai 欢迎一切形式的代码贡献。你可以通过以下几种途径向 mirai 贡献。

## 主仓库 `mirai-core`

**阅读文档**： [docs/mirai.md](docs/mirai.md)

### 代码优化
优化功能设计或实现, 或是引入一个新的设计（建议先通过 issue 与我们达成共识）

### 协议更新
为 mirai 添加更广泛的协议支持。

### 注意事项
- mirai 使用 [`kotlinx.io`](https://github.com/Kotlin/kotlinx-io) IO 库
- 尽量不要引用新的库
- 遵守 Kotlin 代码规范（提交前使用 IDE 格式化代码 (commit 时勾选 'Reformat code')）
- 不要手动拆解数据包. 请一定使用 `kotlinx.serialization` 拆解 ProtoBuf, 使用 mirai 的 `Jce` 序列化器拆解 Jce 数据包, 使用 `kotlinx.serialization` 拆解 Json 数据.

## 社区

插件社区不要求太高的代码质量，任何人都可以帮助 mirai。
可以为 [mirai-console](https://github.com/mamoe/mirai-console) 编写插件, 并发布到社区网站: (建设中)

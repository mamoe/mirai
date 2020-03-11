# 贡献

感谢你来到这里和你对 mirai 做的所有贡献。

mirai 欢迎一切形式的代码贡献。你可以通过以下几种途径向 mirai 贡献。

## 主仓库 `mirai-core`

### 代码优化
优化功能设计或实现, 或是引入一个新的设计（建议先通过 issue 与我们达成共识）

### 协议更新
为 mirai 添加更广泛的协议支持。

### 注意事项
- mirai 框架已经把实现协议需要做的工作最小化. 为避免工作重复, 请务必熟悉 `net.mamoe.mirai.utils` 和 `net.mamoe.mirai.qqandroid.utils` 中工具类
- mirai 使用 [`kotlinx.io`](https://github.com/Kotlin/kotlinx-io) IO 库
- mirai 为多平台项目, 请务必考虑多平台兼容性
- mirai 为全协程实现, 请在有必要的时候考虑并发安全性
- 尽量不要引用新的库
- 遵守 Kotlin 代码规范（提交前使用 IDE 格式化代码）
- 熟悉 [`PacketFactory`](https://github.com/mamoe/mirai/blob/master/mirai-core-qqandroid/src/commonMain/kotlin/net/mamoe/mirai/qqandroid/network/protocol/packet/PacketFactory.kt) 架构
- 不要手动拆解数据包. 请一定使用 `kotlinx.serialization` 拆解 ProtoBuf 和使用 mirai 的 `Jce` 序列化器拆解 Jce 数据包
- 必须保证高代码效率（使用 `ByteArrayPool`，`WeakRef` 等）

## 社区

插件社区不要求太高的代码质量，任何人都可以帮助 mirai。
可以为 [mirai-console](https://github.com/mamoe/mirai-console) 编写插件, 并发布到社区网站: (建设中)

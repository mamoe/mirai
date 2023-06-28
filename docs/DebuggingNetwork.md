# Mirai - DebuggingNetwork

本章节介绍调试网络层的方法。

## 环境变量

可通过 JVM 环境变量改变网络层的行为。一般用户通常不需要手动变更这些功能。

[launch-undispatched]: https://github.com/mamoe/mirai/blob/6eff4bdf40815598a2d987e08d89df6b97663967/mirai-core-api/src/commonMain/kotlin/internal/event/InternalEventListeners.kt#L141

[#1715]: https://github.com/mamoe/mirai/issues/1715

| 环境变量名称                                              | 可选值                              | 解释                                                                                     |
|:----------------------------------------------------|:---------------------------------|:---------------------------------------------------------------------------------------|
| `mirai.network.handler.selector.max.attempts`       | `[1, 2147483647]`                | 最大重连尝试次数                                                                               |
| `mirai.network.reconnect.delay`                     | `[0, 9223372036854775807]`       | 两次重连尝试的间隔毫秒数                                                                           |
| `mirai.network.handler.selector.logging`            | `true`/`false`                   | 启用执行重连时的详细日志                                                                           |
| `mirai.network.handler.cancellation.trace`          | `true`/`false`                   | 让网络层的异常时包含详细原因                                                                         |
| `mirai.network.state.observer.logging`              | `true`/`on`/`false`/`off`/`full` | 启用网络层状态变更的日志                                                                           |
| `mirai.network.auth.logging`                        | `true`/`false`                   | 启用进行登录验证时的内部日志                                                                         |
| `mirai.event.launch.undispatched`                   | `true`/`false`                   | 详见 [源码内注释][launch-undispatched]                                                        |
| `mirai.resource.creation.stack.enabled`             | `true`/`false`                   | 启用 `ExternalResource` 创建时的 stacktrace 记录 (影响性能), 在资源泄露时展示                              |
| `mirai.unknown.image.type.logging`                  | `true`/`false`                   | 启用遇到未知图片格式时的日志                                                                         |
| `mirai.network.show.all.components`                 | `true`/`false`                   | 在网络层异常中附加当前所有组件 (components) 内容                                                        |
| `mirai.network.show.components.creation.stacktrace` | `true`/`false`                   | 在网络层异常中附加当前组件容器创建时的 stacktrace                                                         |
| `mirai.network.packet.logger`                       | `true`/`false`                   | 启用数据包日志 (将为展示所有接收的数据包的 id, sequenceId, extraData 以及内容 hex)                             |
| `mirai.network.show.verbose.packets`                | `true`/`false`                   | 在日志记录数据包时包含冗长的数据包 (如 `MessageSvc.PbGetMsg`, `OnlinePush.ReqPush`, `StatSvc.SimpleGet`) |
| `mirai.network.show.packet.details`                 | `true`/`false`                   | 在日志记录数据包时包含 mirai 解析结果                                                                 |
| `mirai.event.show.verbose.events`                   | `true`/`false`                   | 在日志记录事件时包含冗长的事件 (如 `GroupMessagePreSendEvent`, `GroupMessagePostSendEvent`)            |
| `mirai.event.trace`                                 | `true`/`false`                   | 在日志记录事件监听器创建及使用的信息                                                                     |
| `mirai.message.allow.sending.file.message`          | `true`/`false`                   | 允许发送 `FileMessage`, 用于兼容旧代码 ([#1715])                                                  |
| `mirai.jce.deserializer.debug`                      | `true`/`false`                   | 启用数据包解析错误的详细信息显示                                                                       |

修改示例：

在启动 JVM 时添加参数 `-Dmirai.network.handler.selector.logging=true`
则启用执行重连时的详细日志

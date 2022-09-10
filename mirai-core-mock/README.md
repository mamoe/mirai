# mirai-core-mock

mirai 模拟环境测试框架

> 模拟环境目前仅支持 JVM

--------------

# src 架构

- `contact` - 与 `mirai-core-api` 架构一致
- `database` - 数据库, 用于存储一些临时的零碎数据
- `resserver` - 资源服务
- `userprofile` - 与 `UserProfile` 相关的一些服务
- `utils` - 工具类

# test 架构

- `<toplevel>` 与 mirai-core-api 关系不大或者一些独立的组件的测试
- `.mock` 模拟的各个部分的测试, 每个测试都继承 `MockBotTestBase`


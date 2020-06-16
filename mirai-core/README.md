# mirai-core

mirai 的核心公开 API.

mirai 为多协议设计, `mirai-core` 只提供基础框架和抽象数据类.  
具体的各协议实现为 `mirai-core-PROTOCOL`, 这些协议模块都继承自 `mirai-core`.

可用的协议模块:
- [`mirai-core-qqandroid`](../mirai-core-qqandroid): Android QQ 8.3.0 版本协议实现.


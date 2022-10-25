# mirai-clikt

本模块是对 [Clikt](https://github.com/ajalt/clikt/) 的移植，适用于 IM 环境。主要变更：

1. 删除了 CLI 特定的部分，如文件、argfile、环境变量解析等。
2. 将 Clikt 的入口函数，`main()`、`parse()` 等，调整为 `suspend fun`
3. 命令的执行结果不会直接输出，而是返回一个 `CommandResult` 密封类，所有 `CliktError` 的子类都将被 `catch` 为 `CommandResult.Error`，成功则为 `CommandResult.Success`。
# 进行 ABI 验证

mirai
通过 [binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator))
维护 [ABI](https://zh.wikipedia.org/zh-cn/%E5%BA%94%E7%94%A8%E4%BA%8C%E8%BF%9B%E5%88%B6%E6%8E%A5%E5%8F%A3)
稳定性。

若要修改 mirai-core-api，可执行 Gradle 任务 `apiCheckAll` 来检验 ABI
兼容性，也可以在 IDEA 双击 Control 键运行 `Check Binary Compatiblity`。

若正在添加一个新功能，可以执行 Gradle 任务 `apiDumpAll` 或在 IDEA 双击
Control 键运行 `Dump API Changes for ...` 来更新记录。这将会生成 `*.api`
文件，文件的变化反映了你的修改情况。请人工审核该文件以确保向下兼容。
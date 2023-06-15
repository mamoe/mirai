# 构建

本文介绍如何构建 mirai 的各模块。

## 构建 JVM 目标

要构建只有 JVM 目标的项目（如 `mirai-console`，只需在项目根目录使用如下命令执行
Gradle 任务：

```shell
./gradlew :mirai-console:assemble # 编译
./gradlew :mirai-console:check # 测试
./gradlew :mirai-console:build # 编译和测试
```

其中 `:mirai-console` 是目标项目的路径（path）。

你也可以在 IDEA 等有 Gradle 支持的 IDE 中在通过侧边栏等方式选择项目的 `assemble` 等任务：

![](images/run-gradle-tasks-in-idea.png)

类似，但需要使用 `:mirai-core:compileKotlinJvm` 和 `:mirai-core:jvmTest`
分别用于编译和测试。提示：直接执行测试时也会自动先完成编译。

在 IDEA 开发中无需特殊考虑，一般直接通过点击单元测试行号处的运行按钮即可选择 JVM 平台运行。
要批量运行测试，可使用 `./gradlew :mirai-core:check` 运行 mirai-core 模块的所有目标的所有测试。

不建议在日常使用 `./gradlew check` 运行所有项目的测试，因为这可能会消耗时间和主机运行资源。但也值得在即将提交 PR 或喝咖啡休息时这么做。

### 构建 core 的 Android 目标

查看 [BuildingCoreAndroid](BuildingCoreAndroid.md)。

## 构建 IntelliJ 插件

可通过如下命令构建 IntelliJ 平台 IDE 的插件。构建成功的插件将可以在 `mirai-console/tools/intellij-plugin/build/distribution` 中找到。

```shell
./graldew :mirai-console-intellij:buidlPlugin
```

## 获得 mirai-console JAR

在项目根目录执行如下命令可以获得包含依赖的 mirai-console JAR。其他模块也类似。

```shell
./gradlew :mirai-console:shadowJar
```

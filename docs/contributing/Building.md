# 构建

本文介绍如何构建 mirai 的各模块。

## 构建 JVM 目标项目

要构建只有 JVM 目标的项目（如 `mirai-console`，只需在项目根目录使用如下命令执行
Gradle 任务：

```shell
$ ./gradlew :mirai-console:assemble # 编译
$ ./gradlew :mirai-console:check # 测试
$ ./gradlew :mirai-console:build # 编译和测试
```

其中 `:mirai-console` 是目标项目的路径（path）。

你也可以在 IDEA 等有 Gradle 支持的 IDE
中在通过侧边栏等方式选择项目的 `assemble` 等任务：

![](images/run-gradle-tasks-in-idea.png)

### 获得 mirai-console JAR

在项目根目录执行如下命令可以获得包含依赖的 mirai-console JAR。对于其他模块类似。

```shell
$ ./gradlew :mirai-console:shadowJar
```

## 构建多平台项目

core 是多平台项目。请参考 [构建 Core](BuildingCore.md)。

## 构建 IntelliJ 插件

可通过如下命令构建 IntelliJ 平台 IDE 的插件。构建成功的插件将可以在 `mirai-console/tools/intellij-plugin/build/distribution` 中找到。

```shell
$ ./graldew :mirai-console-intellij:buidlPlugin
```

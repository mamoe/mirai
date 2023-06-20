# 贡献

本文是关于向 mirai 贡献的指南。mirai 欢迎并感谢一切形式的贡献。

[mirai-core-api]: ../../mirai-core-api

[mirai-core-utils]: ../../mirai-core-utils

[mirai-core]: ../../mirai-core

[mirai-console]: ../../mirai-console/backend/mirai-console

[mirai-console-integration-test]: ../../mirai-console/backend/integration-test

[mirai-console-codegen]: ../../mirai-console/backend/codegen

[mirai-console-terminal]: ../../mirai-console/frontend/mirai-console-terminal

[mirai-conosle-compiler-annotations]: ../../mirai-console/tools/compiler-annotations

[mirai-conosle-compiler-common]: ../../mirai-console/tools/compiler-common

[mirai-conosle-intellij]: ../../mirai-console/tools/intellij-plugin

[mirai-conosle-gradle]: ../../mirai-console/tools/gradle-plugin

[mirai-bom]: ../../mirai-bom

[mirai-dokka]: ../../mirai-dokka

[mirai-core-all]: ../../mirai-core-all

[mirai-logging]: ../../logging

[mirai-logging-log4j2]: ../../logging/mirai-logging-log4j2

[mirai-logging-slf4j]: ../../logging/mirai-logging-slf4j

[mirai-logging-slf4j-simple]: ../../logging/mirai-logging-slf4j-simple

[mirai-logging-slf4j-logback]: ../../logging/mirai-logging-slf4j-logback

当前仓库 mamoe/mirai 包含 mirai 核心模块：

| 名称                     | 描述                  |
|------------------------|---------------------|
| mirai-core-utils       | 一些工具类，供其他模块使用       |
| mirai-core-api         | mirai 机器人核心 API     |
| mirai-core             | mirai 机器人核心实现       |
| mirai-core-all         | 上述三个模块的集合，用于启动器     |
| mirai-console          | 插件模式机器人框架后端         |
| mirai-console-terminal | mirai-console 的终端前端 |
| mirai-console-intellij | IntelliJ IDEA 插件    |
| mirai-console-gradle   | Gradle 插件           |
| mirai-bom              | Maven BOM           |
| mirai-logging          | 常用日志库转接器            |

若你想以非提交代码方式帮助 mirai，可以为 [mirai-console](/mirai-console)
编写插件并发布到[论坛](https://mirai.mamoe.net/)，或在论坛帮助新入门的朋友使用
mirai 等。

若希望提交代码，请继续阅读。

## Git 分支

- `1.x`：1.x 版本的开发 (已停止)；
- `dev`：2.x 版本的开发（主分支）；
- `-release` 后缀：某个版本的小更新分支。如 `2.10-release`
  会包含 `2.10.x` 小版本的更新；
- 其他分支：正在开发中或留档的开发进度。

通常请基于 `dev` 分支进行修改。

基于[版本规范](../Evolution.md#版本规范)
，若一个修改适合发布为小版本更新，mirai 会从 `dev`
中提取该修复到目标 `-release` 分支。

## 安装 JDK

需要安装 JDK 才能编译 mirai。mirai 主分支最新提交在如下环境测试可以编译:

| 操作系统         | JDK                | 架构      |
|--------------|--------------------|---------|
| macOS 12.0.1 | AdoptOpenJDK 17    | aarch64 |
| macOS 12.0.1 | Amazon Corretto 11 | amd64   |
| Windows 10   | OpenJDK 17         | amd64   |
| Ubuntu 20.04 | AdoptOpenJDK 17    | amd64   |

若在其他环境下无法正常编译, 请尝试选择上述一个环境配置。

## 我不熟悉 Gradle 或 Kotlin / 我赶时间

在 [SimpleInstructions](SimpleInstructions.md) 查看你可能想做的事情的简单命令。

## `mirai-core` 术语

根据语境，mirai-core 有时候可能指 `mirai-core`
这个模块，有时候可能指 `mirai-core-utils`
、`mirai-core-api`、 `mirai-core` 这三个模块的整体。
本文中，`mirai-core` 将特指 `mirai-core` 模块，而用 'core' 或者 'mirai
core'
指相关三个模块的整体。

## core 多平台架构

[HMPP]: https://kotlinlang.org/docs/multiplatform-discover-project.html

core 三个模块都使用 Kotlin [HMPP] 功能，同时支持 JVM 和 Android
两种平台。你可以在 [Kotlin 官方文档][HMPP] 了解 HMPP 模式。

core 的编译目标层级结构如图所示：

```
    common
    /    \      
  jvm   android 
```

| 发布平台名称  | 描述               |
|---------|------------------|
| jvm     | JVM              |
| android | Android (Dalvik) |

备注：

- common 包含全平台通用代码，绝大部分代码都位于 common；
- jvmBase 包含针对 JVM 平台的通用代码；

## 开发提示

建议使用 IntelliJ IDEA 或 Android Studio，并安装最新的 Kotlin 插件。

建议设置 IntelliJ 的内存为至少 6GB，否则 IDE 可能会频繁冻结编辑器收集垃圾。（可在 `Help -> Edit Custom VM Options` 中添加 `-Xmx6000m`）

### 关闭部分项目以提升速度

你可以在项目根目录创建 `local.properties`，中按照如下配置，关闭部分项目来提升开发速度。在关闭后，请终止所有 Gradle 后台进程，以保证更改正确应用。

```properties
# 关闭 IntelliJ IDEA 插件模块，这可以避免下载 1~2GB 的依赖
projects.mirai-console-intellij.enabled=false
# 关闭 Gradle 插件模块
projects.mirai-console-gradle.enabled=false
# 关闭 mirai 依赖测试模块
projects.mirai-deps-test.enabled=false
# 也可以用其他模块的路径替换 module-path，可关闭该模块
projects.module-path.enabled=false
# 特殊配置，关闭 mirai-console 后端，这同时也会关闭全部 console 相关的项目
projects.mirai-console.enabled=false
# 特殊配置，关闭 mirai-logging，这会关闭所有日志转接模块
projects.mirai-logging.enabled=false
# 特殊配置，是否取消指定 jvmToolchain，在本地 jvmTest 中需要访问 JDK 9+ 的内容时需要携带此配置
mirai.enable.jvmtoolchain.special=false
```

通常关闭 IDEA 插件和 Gradle 插件可以显著提高初始化速度（IDEA 插件项目在初始化时需要下载 1G 左右编译依赖）。

### 关闭 core 的部分构建目标

可以在上述 `local.properties` 中，配置 `projects.mirai-core.targets=` 使用以下配置语法关闭部分构建目标。关闭后可以减轻 IDE 负担，也可以避免下载工具链而加快初始化速度。

所有目标默认都启用。

**注意**，在关闭一个目标后，将无法编辑该目标的相关源集的源码。
因此若非主机性能太差或在 CI 机器运行，**不建议**关闭目标。

[//]: # (备注: 如果要发版, 必须开启全部目标, 否则会导致 metadata 中的平台不全)

- `xxx`：显式启用 `xxx` 目标
- `!xxx`：显式禁用 `xxx` 目标
- `others`：显式启用其他所有所有目标
- `!others`：禁用没有显式启用的所有目标

其中 xxx 表示构建目标名称。可用的目标名称有（区分大小写）：`jvm`、`android`

```
# 只启用 `jvm` 目标，禁用其他所有目标 (Android)
projects.mirai-core.targets=jvm;!others

# 启用 `jvm` 和 `android` 目标
projects.mirai-core.targets=jvm;android
```

### 直接启动 mirai-core 本地测试

一般情况下, 只要 JVM 平台测试通过其他平台也能测试通过。

在 JVM 平台直接启动 mirai-core, 见 [mirai-core/jvmTest](/mirai-core/src/jvmTest/README.md)

## 构建 mirai 项目 JAR

查看 [Building](building/README.md)

## 部署 mirai 到本地仓库

[bignum]: https://github.com/ionspin/kotlin-multiplatform-bignum

要部署 mirai 项目到本地 Maven 仓库（Maven Local），只需使用如下命令，其中 `2.99.0-local` 可为任意想在本地仓库发布的版本号。

```shell
./gradlew publishMiraiArtifactsToMavenLocal "-Dmirai.build.project.version=2.99.0-local"
```

注意，因为构建默认启用多线程，此操作可能会占用约 32GB 内存。如果主机条件不足，或希望减少内存占用，可以如下方式禁用多线程加速：

```shell
./gradlew publishMiraiArtifactsToMavenLocal "-Dmirai.build.project.version=2.99.0-local" "-Porg.gradle.parallel=false"
```

随后可通过 `implementation("net.mamoe:mirai-core:2.99.0-local")` 引入项目。

由于 mirai 项目结构复杂，构建可能由于各种原因失败，mirai 提供依赖可用性测试。
部署一份版本为 `2.99.0-deps-test` 的 mirai 到本地仓库，执行 `./gradlew :mirai-deps-test:test` 即可运行相关测试。若测试通过，则代表部署的项目可通过各种方式正常引入到其他项目。

## 通过 Gradle Composite Build 引入 mirai

若在 Gradle 通过 Composite Build 引入 mirai，则在编译时可能遇到依赖冲突问题。
mirai 使用特定版本的 Ktor 2、[kt-bignum][bignum] 等库，会将它们的包名增加前缀 `net.mamoe.mirai.internal.deps.` 来避免产生冲突。
但这个操作仅对部署的版本有效，在 Gradle 中构建时，仍然可能会有依赖冲突。但若在测试中没遇到问题，一般不用担心。

要详细了解这个过程以及实现原理，请查看 [源码](../../buildSrc/src/main/kotlin/shadow/Relocation.kt)（含注释）。

## 寻找待解决的问题

可以在 [issues](https://github.com/mamoe/mirai/issues) 查看 mirai
遇到的所有问题，或在里程碑查看版本计划.

[里程碑](https://github.com/mamoe/mirai/milestones) 为各版本的开发计划.
在完成所有任务后就会发布该版本.

`Backlog` 为没有设定目标版本的计划. 如果有相关 PR, 这些计划就可能会被确定到一个最近的版本.

## 开发文档

本节列举为了帮助开发某些模块的文档。

- [添加新协议支持](ImplementingProtocol.md)。

## 提交高质量的 commit 以及 PR

mirai 由社区驱动，审核者在业余时间审核 PR。这些规范性建议将帮助你提交高质量的
commit 和 PR，同时节约你和审核者的时间，最大程度地帮助
mirai，也能帮助你在其他项目提交同样高质量的 PR。

如果你不太保证自己能达到这些要求也没关系，mirai 感谢你花费的每一分钟，维护者会审核
PR 并尽可能帮助你。

### 代码规范

- 请在 Kotlin 模块使用纯 Kotlin 实现。
- 尽量不要引用新的库
- 遵守 Kotlin 官方代码规范（提交前使用 IDE 格式化代码即可 (commit
  时勾选 'Reformat code')）

### 为 Java 做兼容

mirai 使用 Kotlin 编写，大量使用 Kotlin 协程等语言级特性。为了能兼容
Java，mirai 使用 Kotlin
编译器插件 [KJBB](https://github.com/him188/kotlin-jvm-blocking-bridge)
。KJBB 会为 Kotlin 挂起函数生成一个辅助方法来允许 Java 调用。在开发时只需要：

- 安装 IDE
  插件 [kotlin-jvm-blocking-bridge](https://github.com/Him188/kotlin-jvm-blocking-bridge/blob/master/README-chs.md#%E5%AE%89%E8%A3%85-intellij-idea-%E6%88%96-android-studio-%E6%8F%92%E4%BB%B6)

- 若要添加一个 suspend 函数, 为它添加 `@JvmBlockingBridge` 注解即可允许
  Java 调用

### 确保二进制兼容性

请在提交前进行 [ABI 验证](VerifyingABI.md)。

### 代码注释语言

内部实现的注释可以使用英文或中文（无变体要求）。公开 API
的注释（KDoc）请只使用简体中文，且无需提供翻译。

### 编写高质量的文档

mirai 在乎质量，这也包括文档质量。根据 mirai 的历史 PR，许多贡献者容易忽视下列问题：

- 汉字与英文或数字之间需要有空格
- KDoc 语法衍生于 Markdown 语法，没有空行的换行会被压缩为一行，因此需要有正确的标点符号结束一句话

可以阅读 [中文技术文档的写作规范](https://github.com/ruanyf/document-style-guide/blob/master/docs/title.md)
了解如何编写高质量的中文文档。为了方便，在 mirai 代码文档和注释中可以使用英文标点符号。但在编写
docs 目录中等的 `.md` 正式文档时，请遵守写作规范。

### PR 标题

编写 PR 标题时，可以使用英文或中文。正确描述 PR 的修改，避免 "修复了一个
bug" 这类模糊标题即可。

### 会如何合并 PR

由于仓库庞大，mirai 主分支（`dev`）维护线性提交历史来确保历史的可读性。这意味着主分支不允许有
merge 操作，只允许基于分支最新提交的提交。

一般 PR 会以 squash 方式合并，即 PR 的所有 commit 都会被合并为一个
commit，并由审核者拟定标题。这个标题通常会直接采用 PR 标题（如果符合规范）。

若提交的 PR 需要以多个步骤完成，且 PR 的提交概述符合约定，审核者通常会以
rebase 方式合并 PR，即 PR 的 commit 会被重置到基于最新 `dev` 分支并按顺序并入。

### commit message

由于很多 PR 会以 squash 方式合并，可以不需要遵守本节的提交概述（commit
message）的约定。

编写提交概述以及 PR 的标题时，可以使用英文或中文。

由于代码量庞大，请在提交概述包含涉及模块名称。模块名称可以是 `[core]`
、`[console]`、`[idea]`、`[gradle]` 或 `[build]`
。如有必要，还可以添加子模块名称如 `[core/native]` 表示 mirai-core 中的
native 部分。

mirai 不要求将 commit 为了区分新功能或修复 bug 而*特地*使用 `fix:`
或 `feat:`
等前缀（允许你认为的合理的使用，或是你更喜欢这样区分）。只需要用最合适的语言描述修改。如 `Optimize gradle properties`
。

请以标准英文句子语法编写提交概述，可省略末尾标点符号。如 `Configure shadow relocation, and add checks for multiplatform publishing.`
或 `Optimize gradle properties`。

### 确保 PR 只解决一类问题

一个 PR 只能解决一个（类）问题，比如 "支持视频消息"、"
修复无法发送图片的问题"、"修复一些消息类型无法序列化的问题"。
等。当解决一类问题时，请规范 commit 来区分解决这类问题的步骤（如果有必要）。

### 确保 commit 的可阅读性

一个 PR 只能包含能通过 commit message
描述的对一个部分的修改。比如 "更新 console 文档"
、"Add logger for NetworkHandler" 等。

commit 必须不带有或指明必要的副作用。例如名称为 "修复无法发送图片的问题"
的 commit 却修改了对消息长度的限制是不好的。此时一个恰当的概述为 "
调整消息长度限制, 修复无法发送图片的问题" (修改内容 + 修改带来的影响)。

若要修复某一个 issue，请不要仅提交 "fix #123"。请附带具体修复内容，把修复
issue 作为"修改带来的影响"。例如 "调整消息长度限制, 修复无法发送图片的问题,
fix #123"。（"fix #123" 会触发 GitHub 自动链接 issue 到 PR）

### PR 在有审核后避免 force push

PR 在有人审核（review）后，请不要进行 force push（`git push -f`
），否则将可能会导致审核人需要重新审核你的全部代码——这会浪费大量时间。请在收到
approve 的 review 并且 PR 被标记 `ready-to-merge` 之后再进行 force push
优化提交历史。当然，即使不优化提交历史，PR 也会在合适的时机使用 squash
方式合并。

在有审核之前可任意 force push。

[//]: # (### 加入开发组)

[//]: # ()

[//]: # (你可以随时提交 PR 解决任何问题。而若有兴趣，我们也欢迎你加入开发组，请联系 support@mamoe.net)

[//]: # ()

[//]: # ([mirai-compose]: https://github.com/sonder-joker/mirai-compose)

[//]: # ()

[//]: # ([plugin-center 服务端]: https://github.com/project-mirai/mirai-plugin-center)

[//]: # ()

[//]: # ([mirai-api-http]: https://github.com/project-mirai/mirai-api-http)

[//]: # ()

[//]: # ([project-mirai/docs]: https://github.com/project-mirai/docs)

[//]: # ()

[//]: # ([docs.mirai.mamoe.net]: https://docs.mirai.mamoe.net)

[//]: # ()

[//]: # (|           名称            |                                      描述                                      |)

[//]: # (|:-----------------------:|:----------------------------------------------------------------------------:|)

[//]: # (|   core 和 console 日常更新   |          在 milestone 安排的日常更新。我们目前版本速度是一个月到两个月发布一个次版本（2.x&#41;。需要日常的开发。           |)

[//]: # (|       console 后端        |                    架构稳定，现在格外需要在易用性上的提升，首先需要一个优化方案，再实现它们。                     |)

[//]: # (|       console 文档        |                根据用户反馈，现在文档十分缺少。需要以用户的身份体验过 console 的人编写用户文档。                 |)

[//]: # (|  图形前端 [mirai-compose]   |                各功能都缺目前尤其缺少对接 console PluginConfig 的图形化配置的实现。                 |)

[//]: # (|   [plugin-center 服务端]   |                插件中心正在建设中。后端 Spring，前端 Vuetify。由于开发人员学业繁忙，暂搁置。                |)

[//]: # (|    plugin-center 社区     | 插件中心计划支持所有语言的插件，因此需要与社区 SDK 作者沟通并帮助它们接入 Console 的 PluginLoader API 和插件中心的要求。 |)

[//]: # (| plugin-center console 端 |              需要评估现在 console 架构是否足够支持插件中心及所有语言插件的管理，实现与插件中心的对接。               |)

[//]: # (|  plugin-center gradle   |                        对接插件中心，实现通过 Task 上传插件。还没有开始做。                         |)

[//]: # (|  mirai-console-loader   |               console 启动器。对接插件中心的 API，支持下载和更新插件等。不确定之后是否会有人实现。               |)

[//]: # (|         IDE 插件          |            IntelliJ IDEA 的插件的工作。可以为 mirai 框架添加检查等功能。这个部分目前基本满足需求。            |)

[//]: # (|   [mirai-api-http] v2   |                                    日常维护。                                     |)

[//]: # (|  [project-mirai/docs]   |     用户友好文档自动部署，使用 VuePress , 部署于 [docs.mirai.mamoe.net]，目前还有部分超链接错误的问题。      |)

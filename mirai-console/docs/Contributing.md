# Mirai Console - Contributing

感谢你来到这里，感谢你对 Mirai Console 做的一切贡献。

## 开发 Mirai Console

### 模块

Mirai Console 项目由四个模块组成：后端，前端，Gradle 插件，Intellij 插件。

```
/
|--- backend                     后端
|  |--- codegen                     后端代码生成工具
|  `--- mirai-console               后端主模块, 发布为 net.mamoe:mirai-console
|--- buildSrc                    项目构建
|--- frontend                    前端
|  `--- mirai-console-terminal      终端前端，发布为 net.mamoe:mirai-console-terminal
`--- tools                       开发工具
   |--- compiler-common             编译器通用模块
   |--- gradle-plugin               Gradle 插件，发布为 net.mamoe.mirai-console
   `--- intellij-plugin             IntelliJ 平台 IDE 插件，发布为 Mirai Console
```

请前往各模块内的 README.md 查看详细说明。

### 构建
```shell script
./gradlew build
```

首次加载和构建 mirai-console 项目可能要花费数小时时间。

## 贡献代码

### 代码风格
- 请优先使用 Kotlin
- 请遵守 [Kotlin 官方代码风格](https://www.kotlincn.net/docs/reference/coding-conventions.html)


<!--
## 发布版本

（以下内容针对拥有 Mirai Console write 权限的项目成员）

若你要发布一个 Mirai Console dev release：

1. 添加 Git 版本号 tag，格式为 `v1.0.1-dev-1`；
2. `git push --tags` 推送 tag 更新，GitHub Actions 将会检测到 tag 更新并执行 JCenter 发布。


若你要发布一个 Mirai Console 稳定版 release，请按顺序进行如下检查：


1. 在 GitHub [milestones](https://github.com/mamoe/mirai-console/milestones) 确认目标版本的工作已经处理完毕；
2. Close milestone；
3. 更新 buildSrc/Versions.kt 中 `project` 版本号为目标版本；
4. 在 [ConfiguringProjects](ConfiguringProjects.md#选择版本) 更新稳定版本号；
5. 本地执行 `./gradlew fillBuildConstants`；
6. Push 前几步的修改为同一个 commit，commit 备注为版本号名称，如 `1.1.0`；
7. 在 GitHub release 根据 Git commit 记录编写更新记录：
   - 描述所有来自社区的 PR 记录；
   - 完整列举公开 API 的任何变动，简要描述或省略内部变动；
   - 为更改按 “后端”，“前端”，“IDE 插件”，“Gradle 插件” 分类；
8. 点击 `Publish`。GitHub Actions 将会检测到 tag 更新并执行 JCenter 发布。

-->
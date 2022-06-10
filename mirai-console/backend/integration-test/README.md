# Console - Integration Test

Mirai Console 一体化测试单元 (目前仅内部测试)

---

## 使用 Integration Test Framework

TODO

### 添加一个新测试

#### 创建 Integration Test 测试点

创建一个新的子测试单元并继承 `AbstractTestPoint`

- 在其 `beforeConsoleStartup()` 准备测试环境 (如写入配置文件, etc)
- 在其 `onConsoleStartSuccessfully()` 检查插件相关行为是否正确

然后在 `MiraiConsoleIntegrationTestLauncher.points` 添加新单元的完整类路径

----

## Mirai Console Internal Testing

### 添加一个新测试 (CONSOLE 内部测试)

在 `test/testpoints` 添加新测试点,
然后在 [`MiraiConsoleIntegrationTestBootstrap.kt`](test/MiraiConsoleIntegrationTestBootstrap.kt)
添加相关单元

### 创建配套子插件

在 `testers` 创建新的文件夹, 刷新 Gradle 即可获得生成的 build.gradle.kts.
即可创建新的配套插件, 可用于测试插件依赖, etc

只有在修改 build.gradle.kts 后才需要将其添加 git, 其他情况下会自动生成.
# Integration Test - Sub Testers

Integration Test 的测试插件

创建新测试插件只需要在本文件夹创建新的目录, 然后重载 (Reimport gradle projects)

如果需要添加新的依赖, 请在 IntegrationTest 添加相关依赖并标注哪个测试框架使用此依赖, 为何使用此依赖

如果需要自定义 `build.gradle.kts`, 请在 IDEA 右键 `build.gradle.kts` 并选择 `Git > Add File`

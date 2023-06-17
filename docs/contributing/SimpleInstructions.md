# 简单命令

以下为你可能想做的事情的示例命令：

1. clone 项目
2. 在项目根目录创建 local.properties，并加入如下内容：
   ```properties
   projects.mirai-console-intellij.enabled=false
   projects.mirai-deps-test.enabled=false
   ```
3. 执行以下命令：

- 我只是 JDK/Java/Kotlin JVM 用户（或者我不知道这什么什么意思）：
    - 编译并打包 mirai-core-all JAR，成品将存放在 `mirai-core-all/build/libs/`：
      ```shell
      ./gradlew :mirai-core-all:shadowJar "-Dprojects.mirai-core.targets=jvm;!others"
      ```
    - 编译并打包 mirai-console JAR，成品将存放在 `mirai-console/build/libs/`:
      ```shell
      ./gradlew :mirai-console:shadowJar "-Dprojects.mirai-core.targets=jvm;!others"
      ```
    - 将 mirai 发布到 mavenLocal 以便本地引入，发布后的版本为 `2.99.0-local`：
      ```shell
      ./gradlew publishMiraiArtifactsToMavenLocal "-Dprojects.mirai-core.targets=jvm;!others" "-Dmirai.build.project.version=2.99.0-local"
      ```
- 我是 Android 用户：
    - 将 mirai 发布到 mavenLocal 以便本地引入，发布后的版本为 `2.99.0-local`：
      ```shell
      ./gradlew publishMiraiArtifactsToMavenLocal "-Dprojects.mirai-core.targets=jvm;android;!others"
      ```
      若上述命令不工作，尝试在 Android Studio 中打开项目并在 Studio 的终端中执行命令。

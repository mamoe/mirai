# 构建 mirai-core Android 目标

mirai 项目支持两种方式构建 Android 目标。

若主机在 `local.properties` 中配置了 `sdk.dir` 为 Android SDK 路径（就像普通 Android 项目一样），
并且 mirai 的 Android 目标为启用状态（见 [关闭部分项目以提升速度](../README.md#关闭部分项目以提升速度)），则会使用 Android SDK 方式构建 android 目标，否则使用 JDK 方式。

## Android 源集结构

以 "ADK" 指代 "Android SDK"，下表展示 mirai core 项目中与 Android 相关的 Kotlin 源集、其依赖的源集列表、以及可用性。

| sourceSet               | dependsOn   | 可用性       |
|-------------------------|-------------|-----------|
| androidMain             | jvmBaseMain | ADK 和 JDK |
| androidInstrumentedTest | jvmBaseTest | ADK       |
| androidUnitTest         | jvmBaseTest | ADK 和 JDK |

## Android SDK 构建方式

就像一个普通的 Android 库一样，mirai 可使用 Android SDK 编译，并拥有在 JVM 的单元测试和在 Dalvik 上运行的 instrumented tests。
这是最推荐的构建方式，能保证 mirai 在真实 Android 环境通过测试，且能获得针对 Android 的 IDEA 代码检查。

注意，`androidInstrumentedTest` 将会使用 Android 模拟器运行。

## JDK 构建方式

若 `sdk.dir` 未配置，则不会配置使用 Android SDK，而会使用桌面 JDK。`androidInstrumentedTest` 将会被禁用。

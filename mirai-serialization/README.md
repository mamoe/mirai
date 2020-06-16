# mirai-serialization

mirai-core 的序列化支持模块. 提供 `Message` 类型的序列化支持与相关 [mirai 码](../docs/mirai-code-specification.md) 支持.

- 状态: 在 `1.1.0` 起为实验性
- 版本: 自 mirai-core `1.1.0` 起随 mirai-core 以同版本号发布

## 依赖配置

### Maven

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-serialization</artifactId>
        <version>0.23.0</version> <!-- 替换版本为最新版本 -->
    </dependency>
</dependencies>
```

### Gradle (推荐)

```kotlin
repositories{
  jcenter()
}
```

#### Java / Kotlin JVM:
- Android:
**注意**: 在 [KT-37152](https://youtrack.jetbrains.com/issue/KT-37152) 修复前, mirai 无法支持 Android 平台目标, 请使用普通 JVM.
```kotlin
implementation("net.mamoe:mirai-serialization-android:VERSION")
```
- 其他 JVM:
```kotlin
implementation("net.mamoe:mirai-serialization:VERSION")
```

#### Kotlin Multi-platform:
**注意**: 在 [KT-37152](https://youtrack.jetbrains.com/issue/KT-37152) 修复前, mirai 无法支持 Android 平台目标, 请使用普通 JVM.
```kotlin
implementation("net.mamoe:mirai-serialization:VERSION") // jvm
implementation("net.mamoe:mirai-serialization-common:VERSION") // common
implementation("net.mamoe:mirai-serialization-android:VERSION") // android 
```


## 使用

本模块包含两部分功能, 'mirai 码' 和 '序列化'

### mirai 码
- 状态: 在 `1.1.0` 实现, 处于实验性阶段

- [mirai 码规范](../docs/mirai-code-specification.md)

#### 将消息转换为 mirai 码
实现了 [CodableMessage](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/code/CodableMessage.kt#L36) 接口的 [Message](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Message.kt#L78) 支持转换为 mirai 码表示.

[`CodableMessage.toMiraiCode(): String`](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/code/CodableMessage.kt#L43)

#### 将 mirai 码转换为消息
[`fun String.parseMiraiCode(): MessageChain`](../mirai-serialization/src/commonMain/kotlin/net/mamoe/mirai/message/code/MiraiCode.kt#L26)

### 序列化
- 状态: 待实现
- 相关 issue: [#219](https://github.com/mamoe/mirai/issues/219), [#201](https://github.com/mamoe/mirai/issues/219)

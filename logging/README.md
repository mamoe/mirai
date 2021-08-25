# mirai-logging

Mirai 日志转接模块，用于使用各大主流日志库接管 Mirai 日志系统以实现统一管理。

请注意，该接管不会影响通过 `redirectBotLogToFile()` 将 Bot 日志重定向到文件以及其他依赖于 mirai 内建日志系统的功能。在接管日志后，需要在目标日志库以配置等方式实现重定向等功能。

仅在 mirai 2.7 起提供。

## 模块列表

[Log4j2]: https://logging.apache.org/log4j/
[SLF4J]: http://www.slf4j.org/
[Logback]: http://logback.qos.ch/

若使用 SLF4J 的转接器，则会同时将对 Log4J2 的调用转向 SLF4J。同样地，若使用 Log4J2 的转接器，同时将对 SLF4J 的调用转向 Log4J2。因此可使用单一日志库。

|   groupId   |          artifactId           | 对接日志库  | 备注                            | 最低 mirai 版本 |
|:-----------:|:-----------------------------:|:---------:|:-------------------------------|:--------------:|
| `net.mamoe` |    `mirai-logging-log4j2`     | [Log4J2]  | 使用 log4j2-core.               |     2.7.0      |
| `net.mamoe` | `mirai-logging-slf4j-logback` | [Logback] | 使用 logback-classic.           |     2.7.0      |
| `net.mamoe` | `mirai-logging-slf4j-simple`  |  [SLF4J]  | 使用 slf4j-simple.              |     2.7.0      |
| `net.mamoe` |     `mirai-logging-slf4j`     |  [SLF4J]  | 需要自定义添加 SLF4J 的任意实现模块. |     2.7.0     |

## 使用方法

选择上述模块中的其中一个，在运行时 classpath 包含即可。

若使用构建工具，可将其作为类似 mirai-core 的依赖添加，版本号与 mirai-core 相同。

### Gradle Kotlin DSL

在 `build.gradle.kts` 添加：

```kotlin
dependencies {
    api("net.mamoe", "mirai-core", "2.7.0")
    api("net.mamoe", "mirai-logging-log4j2", "2.7.0") // 在依赖 mirai-core 或 mirai-core-api 的前提下额外添加日志转接模块. 版本号相同
}
```

### Gradle Groovy DSL

在 `build.gradle.kts` 添加：

```groovy
dependencies {
    api 'net.mamoe:mirai-core:2.7.0'
    api 'net.mamoe:mirai-logging-log4j2:2.7.0' // 在依赖 mirai-core 或 mirai-core-api 的前提下额外添加日志转接模块. 版本号相同
}
```

### Maven

在 `pom.xml` 添加：

```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-jvm</artifactId>
        <version>2.7.0</version>
    </dependency>
    
    <!--在依赖 mirai-core 或 mirai-core-api 的前提下额外添加日志转接模块. 版本号相同-->
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-logging-log4j2</artifactId>
        <version>2.7.0</version>
    </dependency>
</dependencies>
```

## 自行实现日志转接

Mirai 通过 Java `ServiceLoader` 加载 `MiraiLogger.Factory`。只需要实现该类型并以标准 service 方式提供即可（如 `resources` 中 `META-INF/services`）。

但**更推荐**使用上述转接模块先转接到 Log4J2 或 SLF4J，然后再基于 log4j-api 或 slf4j-api 转接。这样更稳定，也可以获取到 `Marker` 的支持。
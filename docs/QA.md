# Q&A

## 插件


### 使用 AutoService

- 方法 A. (推荐) 自动创建 service 文件 (使用 Google auto-service)  
  在 `build.gradle.kts` 添加:
  ```kotlin
  plugins {
    kotlin("kapt")
  }
  dependencies {
    val autoService = "1.0-rc7"
    kapt("com.google.auto.service", "auto-service", autoService)
    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)
  }
  ```
  *对于 `build.gradle` 用户, 请自行按照 Groovy DSL 语法翻译*

- 方法 B. 手动创建 service 文件  
  在 `jar` 内 `META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin` 文件内存放插件主类全名.


**注意**:
- 插件自身的版本要求遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 合格的版本例如: `1.0.0`, `1.0`, `1.0-M1`, `1.0-pre-1`
- 插件依赖的版本遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/) 规范, 同时支持 [Apache Ivy 风格表示方法](http://ant.apache.org/ivy/history/latest-milestone/settings/version-matchers.html).


### 为什么不支持热加载和卸载插件？

在热加载过程容易产生冲突情况；  
卸载时不容易完全卸载所有静态对象。

为了避免这些麻烦，Mirai Console 认为没有支持热加载和热卸载的必要。

# Q&A

## 插件


### 使用 AutoService

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

### 为什么不支持热加载和卸载插件？

在热加载过程容易产生冲突情况；  
卸载时不容易完全卸载所有静态对象。

为了避免这些麻烦，Mirai Console 认为没有支持热加载和热卸载的必要。

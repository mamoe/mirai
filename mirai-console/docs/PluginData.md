# Mirai Console Backend - PluginData

[`Plugin`]: ../backend/mirai-console/src/plugin/Plugin.kt
[`PluginDescription`]: ../backend/mirai-console/src/plugin/description/PluginDescription.kt
[`PluginLoader`]: ../backend/mirai-console/src/plugin/loader/PluginLoader.kt
[`PluginManager`]: ../backend/mirai-console/src/plugin/PluginManager.kt
[`JvmPluginLoader`]: ../backend/mirai-console/src/plugin/jvm/JvmPluginLoader.kt
[`JvmPlugin`]: ../backend/mirai-console/src/plugin/jvm/JvmPlugin.kt
[`JvmPluginDescription`]: ../backend/mirai-console/src/plugin/jvm/JvmPluginDescription.kt
[`AbstractJvmPlugin`]: ../backend/mirai-console/src/plugin/jvm/AbstractJvmPlugin.kt
[`KotlinPlugin`]: ../backend/mirai-console/src/plugin/jvm/KotlinPlugin.kt
[`JavaPlugin`]: ../backend/mirai-console/src/plugin/jvm/JavaPlugin.kt


[`Value`]: ../backend/mirai-console/src/data/Value.kt
[`PluginData`]: ../backend/mirai-console/src/data/PluginData.kt
[`AbstractPluginData`]: ../backend/mirai-console/src/data/AbstractPluginData.kt
[`AutoSavePluginData`]: ../backend/mirai-console/src/data/AutoSavePluginData.kt
[`AutoSavePluginConfig`]: ../backend/mirai-console/src/data/AutoSavePluginConfig.kt
[`PluginConfig`]: ../backend/mirai-console/src/data/PluginConfig.kt
[`PluginDataStorage`]: ../backend/mirai-console/src/data/PluginDataStorage.kt
[`MultiFilePluginDataStorage`]: ../backend/mirai-console/src/data/PluginDataStorage.kt#L116
[`MemoryPluginDataStorage`]: ../backend/mirai-console/src/data/PluginDataStorage.kt#L100
[`AutoSavePluginDataHolder`]: ../backend/mirai-console/src/data/PluginDataHolder.kt#L45
[`PluginDataHolder`]: ../backend/mirai-console/src/data/PluginDataHolder.kt
[`PluginDataExtensions`]: ../backend/mirai-console/src/data/PluginDataExtensions.kt

[`MiraiConsole`]: ../backend/mirai-console/src/MiraiConsole.kt
[`MiraiConsoleImplementation`]: ../backend/mirai-console/src/MiraiConsoleImplementation.kt
<!--[MiraiConsoleFrontEnd]: ../backend/mirai-console/src/MiraiConsoleFrontEnd.kt-->

[`Command`]: ../backend/mirai-console/src/command/Command.kt
[`CompositeCommand`]: ../backend/mirai-console/src/command/CompositeCommand.kt
[`SimpleCommand`]: ../backend/mirai-console/src/command/SimpleCommand.kt
[`RawCommand`]: ../backend/mirai-console/src/command/RawCommand.kt
[`CommandManager`]: ../backend/mirai-console/src/command/CommandManager.kt

[`Annotations`]: ../backend/mirai-console/src/util/Annotations.kt
[`ConsoleInput`]: ../backend/mirai-console/src/util/ConsoleInput.kt
[`JavaPluginScheduler`]: ../backend/mirai-console/src/plugin/jvm/JavaPluginScheduler.kt
[`ResourceContainer`]: ../backend/mirai-console/src/plugin/ResourceContainer.kt
[`PluginFileExtensions`]: ../backend/mirai-console/src/plugin/PluginFileExtensions.kt

[Kotlin]: https://www.kotlincn.net/
[Java]: https://www.java.com/zh_CN/
[JVM]: https://zh.wikipedia.org/zh-cn/Java%E8%99%9A%E6%8B%9F%E6%9C%BA
[JAR]: https://zh.wikipedia.org/zh-cn/JAR_(%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F)

[为什么不支持热加载和卸载插件？]: QA.md#为什么不支持热加载和卸载插件
[使用 AutoService]: QA.md#使用-autoservice

Mirai Console 提供支持自动保存的，静态类型插件数据模型。

### 设计目标

- 源码级静态强类型：避免 `getString()`, `getList()`...
- 全自动加载保存：插件仅需在启动时通过一行代码链接自动保存
- 与前端同步修改：在 Android 等图形化前端实现中可以在内存动态同步修改
- 存储扩展性：可使用多种方式存储，无论是文件还是数据库，插件层都使用同一种实现方式

综上，**最小化插件作者在处理数据和配置做的付出**。

*暂无数据库保存支持，但这已经被提上日程。*

## [`Value`]
```kotlin
interface Value<T> : ReadWriteProperty<Any?, T> {
    @get:JvmName("get")
    @set:JvmName("set")
    var value: T
}
```

表示一个值代理。在 [`PluginData`] 中，除简单数据类型外，值都经过 [`Value`] 包装。

## [`PluginData`]

一个插件内部的, 对用户隐藏的数据对象。类似于属性名作为键，对应 [`Value`] 作为值的 `Map`。

[`PluginData`] 接口拥有一个基础实现类，[`AbstractPluginData`]，默认不支持自动保存，仅存储键值关系及其序列化器。

插件可继承 [`AbstractPluginData`]，拥有高自由的实现细节访问权限，并扩展数据结构。  
但通常，插件使用 [`AutoSavePluginData`]。

[`AutoSavePluginData`] 监听保存在它之中的值的修改，并在合适的时机在提供的 [`AutoSavePluginDataHolder`] 协程作用域下启动协程保存数据。

### 使用 `PluginData`
示例在此时比理论更高效。

1. 定义一个单例，继承 `AutoSavePluginData`
```kotlin
object MyData : AutoSavePluginData("MyData")
```

2. 使用委托添加属性。所有类型都可以使用同样的‘语法’。
```kotlin
object MyData : AutoSavePluginData("MyData") { // 文件名为 MyData, 会被保存为 MyData.yml
    val value1 by value<Int>() // 推断为 Int
    val value2 by value(0) // 默认值为 0， 推断为 Int
    var value3 by value(0) // 支持 var，修改会自动保存
    val value4: Int by value() // 显式类型和推断类型，你喜欢哪种？
    val value5: List<String> by value() // 支持 List，Set
    val value6: MutableList<String> by value() // 可按需使用 Mutable 类型
    val value7: List<List<String>> by value() // 支持嵌套
    val value8: Map<String, List<List<String>>> by value() // 支持 Map
    
    var value9: List<String> by value() // List、Set 或 Map 同样支持 var。但请注意这是非引用赋值（详见下文）。
}
```

3. 建立自动保存链接
使用 `PluginDataStorage.load(PluginDataHolder, PluginData)` 即可完成自动保存链接，并读取数据。  
对于 [JVM 插件][`JvmPlugin`]，可简便地在 `onEnable()` 中使用 `MyData.reload()`（对于上例）。详见 [读取 `PluginData` 或 `PluginConfig`](Plugins.md#读取-plugindata-或-pluginconfig)

### 定义数据模型（Java）
*由于 Java 语法局限，为 Kotlin 而设计的 PluginData 在 Java 使用很复杂。*  
*即使 Mirai Console 为 Java 提供适配器，也强烈推荐 Java 用户在项目中混用 Kotlin 代码来完成数据模型定义。*

参考 [JAutoSavePluginData](../backend/mirai-console/src/data/java/JAutoSavePluginData.kt#L69)

### 非引用赋值
由于实现特殊, 赋值时不会写其引用. 即:
```kotlin
val list = ArrayList<String>("A")
MyPluginData.list = list // 赋值给 PluginData 的委托属性是非引用的
println(MyPluginData.list) // "[A]"
list.add("B")
println(list) // "[A, B]"
println(MyPluginData.list) // "[A]"  // !! 由于 `list` 的引用并未赋值给 `MyPluginData.list`.
```

另一个更容易出错的示例:
```kotlin
// MyPluginData.nestedMap: MutableMap<Long, List<Long>> by value()
val newList = MyPluginData.map.getOrPut(1, ::mutableListOf)
newList.add(1) // 不会添加到 MyPluginData.nestedMap 中, 因为 `mutableListOf` 创建的 MutableList 被非引用地添加进了 MyPluginData.nestedMap
```

要解决这种无法自动初始化空集合的问题，请查看 [实验性扩展方法](#实验性扩展方法) 方法

### 使用自定义可序列化数据类型
在 Kotlin，支持使用 [kotlinx.serialization](https://github.com/kotlin/kotlinx.serialization) 序列化的自定义数据类型。

**Console 使用反射构造自定义数据类型示例**。当自定义数据类型拥有公开无参构造器，或者一个构造器的所有参数都可选时，在使用委托 `by value()` 时可无需提供默认值。
否则，需要提供默认值。（见如下示例）

自定义数据类型定义：
```kotlin
@Serializable // kotlinx.serialization.Serializable
class CustomA(val str: String)

@Serializable
class CustomB(val str: String = "") // 参数可选，CustomB 就可以直接被反射构造。
```

使用时：
```kotlin
object MyData : AutoSavePluginData("MyData") {
    val value1 by value(CustomA("")) // CustomA 不可以通过反射直接构造实例，因为必须提供参数 str。因此要在创建 value 时提供默认值。
    val value2: CustomB by value() // CustomB 可以通过反射直接构造实例
}
```

### （实验性）[扩展方法][`PluginDataExtensions`]
由于非引用赋值特性，在 `PluginData` 中定义的 `Map` 无法使用 `map.getOrPut(..., ::mutableListOf)` 等方法。  
为此，Console 提供一些 *映射方法*。

（下文示例省略 `Value` 所在的 `PluginData` 定义）

#### （实验性）`Map.withEmptyDefault`
```kotlin
fun <K, InnerE, InnerV> SerializerAwareValue<MutableMap<K, Map<InnerE, InnerV>>>.withEmptyDefault(): SerializerAwareValue<MutableMap<K, Map<InnerE, InnerV>>>
```
创建一个代理对象, 当 `Map.get` 返回 `null` 时先放入一个 `LinkedHashMap`, 再返回这个 `LinkedHashMap`。

示例：
```kotlin
val value1 by value<MutableMap<Long, List<Int>>>().withEmptyDefault()
```
使用时
```kotlin
val v: MutableMap<Long, List<Int>> = MyData.value1[123456] // 此时 Map.get 返回非 null。因为若 MyData 中不存在 123456 对应的值，就先放入一个空 List。
```

**但是，这种方法不支持多层嵌套**：例如 `Map<Long, Map<Long, List<Int>>>` 内层的 Map 不会被这样处理。  
因此此方法仍处于实验性状态。如果你有任何建议，请在 issues 中发起讨论。

#### （实验性）`Map.withDefault`
```kotlin
fun <K, V> SerializerAwareValue<MutableMap<K, V>>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<MutableMap<K, V>>
```

与上述 `Map.withEmptyDefault` 类似。只是把默认值从 `mutableListOf` 换成了 `defaultValueComputer()`

**但是，方法命名仍有待确认**：`withDefault` 可能不是最好的命名，因为可能与标准库的 `map.withDefault` 产生歧义（他们行为不同）

#### （实验性）`Map.mapKeys`
映射 `Map` 的键。
```kotlin
fun <OldK, NewK, V> SerializerAwareValue<MutableMap<OldK, V>>.mapKeys(
    oldToNew: (OldK) -> NewK,
    newToOld: (NewK) -> OldK,
): SerializerAwareValue<MutableMap<NewK, V>>
```

可进一步简化配置的操作。

示例：
```kotlin
val value by value<MutableMap<Long, List<Int>>>().withEmptyDefault().mapKeys(Bot::id, Bot::getInstance)
```
使用时：
```kotlin
val bot: Bot = getBot()

val list: List<Int> = value[bot]
value[bot] = listOf()
```

## [`PluginConfig`]

### [`PluginData`] 与 [`PluginConfig`] 的区别
- [`PluginData`] 表示插件内部的数据，不应该被用户看到。
- [`PluginConfig`] 表示插件的配置，用户可以修改这些配置。

### 使用 [`PluginConfig`]
[`PluginConfig`] 与 [`PluginData`] 用法完全相同。

在上述 [使用 `PluginData`](#使用-plugindata) 的示例中，
将 [`AutoSavePluginData`] 换为 [`AutoSavePluginConfig`] 即可创建一个配置，而不是数据。

在加载时使用 `configInstance.reload()` 或 `JvmPlugin.reloadPluginConfig(configInstance)`。

## [`PluginDataHolder`]
***注意：这是实验性 API。***

[`PluginData`] 的拥有者。一般用于区分不同插件的不同 [`PluginData`]，避免命名冲突。

[`JvmPlugin`] 实现 [`PluginDataHolder`]，使用插件名作为保存时的名称。

## [`PluginDataStorage`]
***注意：这是实验性 API。***

[`PluginData`] 的存储仓库，将 [`PluginData`] 从内存序列化到文件或到数据库，或反之。

内置的实现包含：[`MultiFilePluginDataStorage`], [`MemoryPluginDataStorage`]

----

> 下一步，[Permissions](Permissions.md#mirai-console-backend---permissions)
>
> 返回 [开发文档索引](README.md#mirai-console)


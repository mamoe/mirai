# Mirai Console Backend - JVM Plugins - Debug

> 建议在 Java 9+ 的环境中进行排错, mirai-console 在 java 9+ 中的错误堆栈中报告了错误类来自哪个类加载器

## 错误堆栈基本解析

```log
java.lang.Exception: Thread stack dump
        at java.base/java.lang.Thread.dumpStack(Thread.java)
        at example-plugin.mirai2.jar[shared]//com.example.exmapleplugin.sharedlib.SharedLib.handle(shared.kt:6)
        at example-plugin.mirai2.jar[private]//com.example.exmapleplugin.privatelib.PrivLib.cmd(priv.kt:5)
        at example-plugin.mirai2.jar//com.example.exmapleplugin.MyCommand.cmd(MyCommand.kt:63)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at ......
        at net.mamoe.mirai.console.command.CommandManager.executeCommand$default(CommandManager.kt:125)
        at chat-command-0.5.1.jar//net.mamoe.mirai.console.plugins.chat.command.PluginMain.handleCommand(PluginMain.kt:86)
        at chat-command-0.5.1.jar//net.mamoe.mirai.console.plugins.chat.command.PluginMain$onEnable$2$1.invokeSuspend(PluginMain.kt:69)
        at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        at ......
```

来自 plugin 本身的类加载器的堆栈会以 插件文件名 开头, 其中 `...[private]` `....[shared]` 都是该插件使用的类库.

- `[shared]` 代表是共享库, 其中的类可以被依赖此插件的其他插件解析到
- `[private]` 代表是私有库, 仅该插件自己内部使用, 依赖此插件的其他插件将不能解析到此类加载器的类

## 多插件间数据交换结果和预期不符合

多插件间数据结果不一致 90% 是因为缺少依赖关系导致的未解析到相同的类导致结果不一致

关于如何建立关系, 见 [JVMPlugin - Data Exchange](./JVMPlugin-DataExchange.md)

可以使用以下代码确定是否是因为类链接错误导致的数据不一致

```kotlin
fun MiraiLogger.dumpClass(klass: Class<*>) {
    info { "Class name: $klass" }
    info { "  |- loader: ${klass.classLoader}" }
    info { "  |- module: ${klass.module}" }
}
```

```java
public static void dumpClass(MiraiLogger logger, Class<?> klass) {
    logger.info("Class name: " + klass);
    logger.info("  |- loader: " + klass.getClassLoader());
    logger.info("  |- module: " + klass.getModule());
}
```

## 使用的第三方库报错没有模块实现

在插件初始化的时候, 线程上下文类加载器依然还是 console 的系统类加载器 (`AppClassLoader`), 需要手动将其切换到插件的类加载器

详见 [Issue Comment](https://github.com/mamoe/mirai/issues/2138#issuecomment-1179673302)

```kotlin
fun onEnable() {
    val oThreadCtxLoader = Thread.currentThread().contextClassLoader
    try {
        Thread.currentThread().contextClassLoader = javaClass.classLoader
        // .......
    } finally {
        Thread.currentThread().contextClassLoader = oThreadCtxLoader
    }
}
```

## 使用依赖库后无法加载插件 / clinit 无法使用依赖库

错误类似

```log
2023-12-08 00:23:42 E/main: Failed to init MiraiConsole.
net.mamoe.mirai.console.internal.util.ServiceLoadException: Could not load service com.example.exmapleplugin.MyPlugin
    at ....
Caused by: java.lang.NoClassDefFoundError: com/example/somelibrary/ClassFromLibrary
	at java.base/java.lang.Class.forName0(Native Method)
	at java.base/java.lang.Class.forName(Class.java:467)
	at net.mamoe.mirai.console.internal.util.PluginServiceHelper.loadService(PluginServiceHelper.kt:51)
Caused by: java.lang.NoClassDefFoundError: org/quartz/SchedulerException
	... 23 more
Caused by: java.lang.ClassNotFoundException: com/example/somelibrary/ClassFromLibrary
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
Caused by: java.lang.ClassNotFoundException: com.example.somelibrary.ClassFromLibrary
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
	at net.mamoe.mirai.console.internal.plugin.JvmPluginClassLoaderN.loadClass(JvmPluginClassLoader.kt:389)
	... 26 more
```

此原因是因为 clinit 阶段时 mirai-console 还未加载依赖库至插件类搜索路径中。

如果您使用 mirai-console 2.16.0+

请创建 `plugin.yml`, mirai-console 才能将依赖库在 clinit 阶段前加载到插件类搜索路径，见 [JVMPlugin - 通过资源文件提供静态信息](./JVMPlugin.md#%E9%80%9A%E8%BF%87%E8%B5%84%E6%BA%90%E6%96%87%E4%BB%B6%E6%8F%90%E4%BE%9B%E9%9D%99%E6%80%81%E4%BF%A1%E6%81%AF)

如果您使用 mirai-console 2.160 之前的版本, 请创建一个新的类，此类不要包含依赖库的代码，然后将此类的代码转移到您的真正的逻辑代码

示例
```kotlin
object OuterPlugin: KotlinPlugin(...) {
    override fun onEnable() {
        ActuallyPluginClassLoader.onEnable()
    }
}

object ActuallyPluginClassLoader {
    fun onEnable() {
        // .....
    }
}
```

> 底层分析
>
> 为了实现插件之间的相互依赖，mirai-console 必须获取到插件的信息 (PluginDescription) 才能进行插件类路径链接操作
>
> 在 mirai-console 2.16.0 之前，插件加载顺序为
>
> - 加载插件主类 (即 clinit 阶段)
> - 加载插件实例 (传递 PluginDescription 给 mirai-console)
> - mirai-console 构建插件依赖关系，链接插件类路径
> - 执行插件的 onEnable
>
> 在 2.16.0+, 存在 plugin.yml 时, 插件加载的顺序为
>
> - 加载 plugin.yml
> - 构建插件依赖关系，链接插件类路径
> - 加载插件主类
> - 加载插件实例
> - 执行 onEnable

## java.lang.LinkageError: loader constraint violation

```log
java.lang.LinkageError: loader constraint violation: when resolving method 'void io.ktor.client.request.HttpRequestBuilder.setMethod(io.ktor.http.HttpMethod)' the class loader 'test-ktor-dev.mirai2.jar' @61dde151 of the current class, com/kasukusakura/testktor/TestKtor$getTailrec$$inlined$get$2, and the class loader 'app' for the method's defining class, io/ktor/client/request/HttpRequestBuilder, have different Class objects for the type io/ktor/http/HttpMethod used in the signature (com/kasukusakura/testktor/TestKtor$getTailrec$$inlined$get$2 is in unnamed module of loader 'test-ktor-dev.mirai2.jar' @61dde151, parent loader 'global-shared' @32b9bd12; io.ktor.client.request.HttpRequestBuilder is in unnamed module of loader 'app')
	at .................

java.lang.LinkageError: loader constraint violation:
    when resolving method 'void io.ktor.client.request.HttpRequestBuilder.setMethod(io.ktor.http.HttpMethod)'
        the class loader 'test-ktor-dev.mirai2.jar' @61dde151 of the current class, com/kasukusakura/testktor/TestKtor$getTailrec$$inlined$get$2,
        and
        the class loader 'app' for the method's defining class, io/ktor/client/request/HttpRequestBuilder,

    have different Class objects for the type io/ktor/http/HttpMethod used in the signature
    
    (
        com/kasukusakura/testktor/TestKtor$getTailrec$$inlined$get$2 is in unnamed module of loader 'test-ktor-dev.mirai2.jar' @61dde151,
            parent loader 'global-shared' @32b9bd12;

        io.ktor.client.request.HttpRequestBuilder is in unnamed module of loader 'app'
    )

```

翻译

```log
JVM 无法解析 com/kasukusakura/testktor/TestKtor$getTailrec$$inlined$get$2 中的方法引用
    'void io.ktor.client.request.HttpRequestBuilder.setMethod(io.ktor.http.HttpMethod)'

搜索到的 'io.ktor.client.request.HttpRequestBuilder' 位于系统类加载器 'app' 中

HttpRequestBuilder 中的 HttpMethod 引用和
    TestKtor$getTailrec$$inlined$get$2 得到的 HttpMethod 引用
不一致, 无法连接
```

结论: 插件没有附带完整的 ktor 依赖导致部分类解析至插件库加载器, 部分类解析至系统类加载器

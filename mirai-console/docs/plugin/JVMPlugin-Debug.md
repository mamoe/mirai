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

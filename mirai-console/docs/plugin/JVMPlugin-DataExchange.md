# Mirai Console Backend - JVM Plugins - Data Exchange

> 本章主要介绍如何在多个插件内直接交换数据
>
> 注: 多插件之间广播事件也是直接交换数据的一种方式

--------------------------


如果需要在插件之间直接交换数据, 则插件之间必须存在直接或间接的依赖关系。

在 [Console - JvmPlugin](JVMPlugin.md#类加载隔离) 中有提到类加载隔离，没有依赖关系的插件之间是不能直接交换数据的。

> 关于如何建立依赖关系, 见 [JVMPlugin - 有关插件依赖的说明](JVMPlugin.md#有关插件依赖的说明)

## 在有依赖关系的插件中广播事件

现在系统中存在有两个插件 `com.example.guide.baseplugin` 和 `com.example.guide.extplugin`, 其中 `extplugin` 依赖 `baseplugin`。

在 `baseplugin` 中定义的类都可以在 `extplugin` 中访问

## 在无依赖关系的两个插件中广播事件

在两个没有依赖关系的插件中, 是不能直接交换数据的, 即使使用了相同的类名也是不能进行数据交换的。

如果需要在两个没有任何关系的插件中交换数据, 需要最少三个模块: `data-typedef`, `plugin-a`, `plugin-b`, `plugin-....`

### 多插件数据交换核心思路

在多个插件间交换数据, 必须存在直接或者间接的关系, 只有建立了关系才能解析到相同的类。

两个没有关系的插件 `A` 和 `B` 之间, 必须通过另外的模块 `data-typedef` 建立起间接的关系。

比如在 `data-typedef` 中定义的事件 `MyEvent`, 即使该事件是在 A 广播的也可以在 B 监听到。


### 通过修改插件全局类路径链接插件

> 不推荐此方法, 仅适合于高度自定义
>
> - 使用此方法很可能会干扰到其他插件的执行导致其他插件报错。
> - 使用此方法很可能会遇到库冲突的情况。

将 `data-typedef.jar` 放入 `plugin-shared-libraries` 即可。

模块间的关系如下图
```text
     plugin-a               plugin-b      others....      ......
        |                      |             |            ......
        |                      |             |            ......
        |                      |             |            ......
=================================================================
## data-typedef
## ........
=================================================================
                               |
=================================================================
##
## MIRAI CONSOLE PLUGIN SYSTEM
##
```

### 将 data-typedef 打包成中转插件

将 `data-typedef` 也编写为一个 mirai-console 插件, 
并在 `plugin-a`, `plugin-b` 中定义对 `data-typedef` 的依赖定义即可。


模块间的关系如下图
```text
     plugin-a               plugin-b      others....      ......
        |                      |             |            ......
        +---  data-typedef  ---+             |            ......
                   |                         |            ......
=================================================================
## MIRAI CONSOLE PLUGIN SYSTEM
##
```

## 排错

见 [JVMPlugin Debug](JVMPlugin-Debug.md)

----------------------

> 返回 [JVMPlugin](JVMPlugin.md)
>
> 返回 [开发文档索引](../README.md#mirai-console)

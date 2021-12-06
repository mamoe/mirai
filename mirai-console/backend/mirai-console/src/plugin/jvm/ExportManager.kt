/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.internal.plugin.ExportManagerImpl
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 插件的类导出管理器
 *
 *
 * 允许插件将一些内部实现保护起来， 避免其他插件调用， 要启动这个特性，
 * 只需要创建名为 `export-rules.txt` 的规则文件，便可以控制插件的类的公开规则。
 *
 * 如果正在使用 `Gradle` 项目, 该规则文件一般位于 `src/main/resources` 下
 *
 * Example:
 * ```text
 *
 * # #开头的行全部识别为注释
 *
 * # exports, 允许其他插件直接使用某个类
 *
 * # 导出了一个internal包的一个类
 * #
 * exports org.example.miraiconsole.myplugin.internal.OpenInternal
 *
 * # 导出了整个 api 包
 * #
 * exports org.example.miraiconsole.myplugin.api
 *
 * # 保护 org.example.miraiconsole.myplugin.api2.Internal, 不允许其他插件直接使用
 * #
 * protects org.example.miraiconsole.myplugin.api2.Internal
 *
 * # 保护整个包
 * #
 * # 别名: protect-package
 * protects org.example.miraiconsole.myplugin.internal
 *
 * # 此规则不会生效, 因为在此条规则之前,
 * # org.example.miraiconsole.myplugin.internal 已经被加入到保护域中
 * exports org.example.miraiconsole.myplugin.internal.NotOpenInternal
 *
 *
 * # export-plugin, 允许其他插件使用除了已经被保护的全部类
 * # 使用此规则会同时让此规则后的所有规则全部失效
 * # 别名: export-all, export-system
 * # export-plugin
 *
 *
 * # 将整个插件放入保护域中
 * # 除了此规则之前显式 export 的类, 其他插件将不允许直接使用被保护的插件的任何类
 * # 别名: protect-all, protect-system
 * protect-plugin
 *
 * ```
 *
 * 插件也可以通过 Service 来自定义导出控制
 *
 * Example:
 * ```kotlin
 * @AutoService(ExportManager::class)
 * object MyExportManager: ExportManager {
 * override fun isExported(className: String): Boolean {
 * println("  <== $className")
 * return true
 * }
 * }
 * ```
 *
 * @see StandardExportManagers
 */
@ConsoleExperimentalApi
public interface ExportManager {
    /**
     *  如果 [className] 能够通过 [ExportManager] 的规则, 返回 true
     *
     *  @param className [className] 是一个合法的满足 [ClassLoader] 的加载规则 的全限定名.
     *  [className] 不应该是数组的全限定名或者JVM基本类型的名字.
     *  See also: [ClassLoader.loadClass]
     *  [ClassLoader#name](https://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html#name)
     */
    public fun isExported(className: String): Boolean
}

@ConsoleExperimentalApi
public object StandardExportManagers {
    @ConsoleExperimentalApi
    public object AllExported : ExportManager {
        override fun isExported(className: String): Boolean = true
    }

    @ConsoleExperimentalApi
    public object AllDenied : ExportManager {
        override fun isExported(className: String): Boolean = false
    }

    @ConsoleExperimentalApi
    @JvmStatic
    public fun parse(lines: Iterator<String>): ExportManager {
        return ExportManagerImpl.parse(lines)
    }
}
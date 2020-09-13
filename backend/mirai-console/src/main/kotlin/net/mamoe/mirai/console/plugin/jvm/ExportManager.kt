/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.internal.plugin.ExportManagerImpl

/**
 * 插件的类导出管理器
 *
 * 我们允许插件将一些内部实现hidden起来， 避免其他插件调用， 要启动这个特性，
 * 只需要在你的 resources 文件夹创建名为 `export-rules.txt` 的规则文件，便可以控制插件的类的公开规则
 *
 * Example:
 * ```text
 *
 * # #开头的行我们都识别为注释, 你可以在规则文件里面写很多注释
 *
 * # export 运行插件访问一个类, 或者一个包
 *
 * # 导出了一个internal包的一个类
 * export org.example.miraiconsole.myplugin.internal.OpenInternal
 *
 * # 导出了整个 api 包, 导出包和导出类的区别就是末尾是否存在 . 号
 * export org.example.miraiconsole.myplugin.api.
 *
 * # deny, 不允许其他插件使用这个包, 要隐藏一个包的时候, 注意不要忘记最后的 . 号
 * #
 * # 别名: hidden, internal
 * deny org.example.miraiconsole.myplugin.internal.
 *
 * # 这条规则不会生效, 因为在这条规则前已经被上面的 deny 给隐藏了
 * export org.example.miraiconsole.myplugin.internal.NotOpenInternal
 *
 *
 * # export-all, 导出全部内容, 当然在此规则之前的deny依然会生效
 * # 使用此规则会同时让此规则后的所有规则全部失效
 * # export-all
 *
 * # 拒绝其他插件使用任何类, 除了之前已经explort的
 * # 此规则会导致后面的所有规则全部失效
 * deny-all
 *
 * ```
 *
 * 插件也可以通过 Service 来自定义导出控制
 *
 * Example:
 * ```
 * @AutoService(ExportManager::class)
 * object MyExportManager: ExportManager {
 *     override fun isExported(className: String): Boolean {
 *         println("  <== $className")
 *         return true
 *     }
 * }
 * ```
 *
 */
public interface ExportManager {
    public fun isExported(className: String): Boolean
}

public object StandardExportManagers {
    public object AllExported : ExportManager {
        override fun isExported(className: String): Boolean = true
    }

    public object AllDenied : ExportManager {
        override fun isExported(className: String): Boolean = false
    }

    @JvmStatic
    public fun parse(lines: Iterator<String>): ExportManager {
        return ExportManagerImpl.parse(lines)
    }
}
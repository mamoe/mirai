/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package samepkg

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

/*
same-pkg-2: 测试包名一样时插件可以正常加载
 */
internal object P : KotlinPlugin(
    JvmPluginDescription(
        id = "net.mamoe.tester.samepkg-2",
        version = "1.0.0",
        name = "SamePkg 2",
    )
) {}

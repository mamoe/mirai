/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

@Suppress("RemoveExplicitTypeArguments")
internal class PluginDependenciesConfig : ReadOnlyPluginConfig("PluginDependencies") {
    @ValueDescription("远程仓库, 如无必要无需修改")
    val repoLoc: List<String> by value(
        listOf<String>(
            "https://maven.aliyun.com/repository/central",
            "https://repo1.maven.org/maven2/",
        )
    )
}
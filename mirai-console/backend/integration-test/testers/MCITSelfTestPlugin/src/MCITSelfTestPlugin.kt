/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.ep.mcitselftest

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import kotlin.test.assertTrue

/*
MCITSelfTestPlugin: 用于测试 Integration-test 可正常加载
@see /test/testpoints/MCITBSelfAssertions
 */
public object MCITSelfTestPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "net.mamoe.tester.mirai-console-self-test",
        version = "1.0.0",
        name = "MCITSelfTestPlugin",
    )
) {
    override fun onEnable() {
        logger.info { "MCITSelfTestPlugin.onEnable() called" }

        assertTrue { true }
    }

    public fun someAction() {
        logger.info { "Called!" }
    }
}

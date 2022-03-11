/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.itest.serviceloader.ndep

import net.mamoe.console.integrationtest.mod.servicetypedef.ServiceTypedef
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import java.util.*
import kotlin.test.assertEquals


internal class PS : ServiceTypedef

internal object PMain : KotlinPlugin(JvmPluginDescription("net.mamoe.console.itest.serviceloader-ndp", "0.0.0") {
    dependsOn("net.mamoe.console.itest.serviceloader")
}) {
    override fun onEnable() {
        val loader = ServiceLoader.load(
            Class.forName("net.mamoe.console.integrationtest.mod.servicetypedef.ServiceTypedef"),
            javaClass.classLoader,
        )
        val services = loader.asSequence().map { it.javaClass.name }.toMutableList()
        services.sort()
        services.forEach { service ->
            logger.info { "Service: $service" }
        }
        assertEquals(mutableListOf(
            "net.mamoe.console.integrationtest.mod.serviceimpl.ServiceImpl",
            "net.mamoe.console.itest.serviceloader.ndep.PS",
        ), services)
    }
}
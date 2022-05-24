/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.console.itest.serviceloader

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import java.io.File
import java.util.*
import kotlin.test.assertEquals

internal object PMain : KotlinPlugin(JvmPluginDescription("net.mamoe.console.itest.serviceloader", "0.0.0")) {
    init {
        val access = jvmPluginClasspath
        val sharedCL = access.pluginSharedLibrariesClassLoader
        access.addToPath(sharedCL, File("modules/module-service-loader-typedef-0.0.0.jar"))
        access.addToPath(sharedCL, File("modules/module-service-loader-impl-0.0.0.jar"))
    }

    override fun onEnable() {
        @Suppress("LocalVariableName")
        val ServiceTypedef = Class.forName("net.mamoe.console.integrationtest.mod.servicetypedef.ServiceTypedef")
        val loader = ServiceLoader.load(
            ServiceTypedef,
            javaClass.classLoader,
        ).toList()
        val services = loader.asSequence().map { it.javaClass.name }.toMutableList()
        services.forEach { service ->
            logger.info { "Service: $service" }
        }
        assertEquals(mutableListOf("net.mamoe.console.integrationtest.mod.serviceimpl.ServiceImpl"), services)
        ServiceTypedef.getMethod("act").invoke(loader.first())

        assertEquals(
            "from plugin",
            javaClass.getResourceAsStream("/test-res.txt")!!.reader().use { it.readText() }.trim(),
        )
        val tstRes = javaClass.classLoader.getResources("test-res.txt").asSequence().onEach {
            println(it)
        }.toMutableList()
        // /service-loader-0.0.0.jar!/test-res.txt
        // /module-service-loader-typedef-0.0.0.jar!/test-res.txt
        // /module-service-loader-impl-0.0.0.jar!/test-res.txt
        assertEquals(3, tstRes.size)

        assertEquals(
            mutableListOf(),
            javaClass.classLoader.getResources("something/not/exists.bin").asSequence().toMutableList()
        )
    }
}

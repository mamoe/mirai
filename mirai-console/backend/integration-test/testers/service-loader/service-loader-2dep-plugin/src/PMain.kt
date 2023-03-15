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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


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
        assertEquals(
            mutableListOf(
                "net.mamoe.console.integrationtest.mod.serviceimpl.ServiceImpl",
                "net.mamoe.console.itest.serviceloader.ndep.PS",
            ), services
        )
        assertEquals(
            "from 2nd plugin",
            javaClass.getResourceAsStream("/test-res.txt")!!.reader().use { it.readText() }.trim(),
        )
        val tstRes = javaClass.classLoader.getResources("test-res.txt").asSequence().onEach {
            println(it)
        }.toMutableList()
        // /service-loader-2dep-plugin-0.0.0.jar!/test-res.txt
        // /service-loader-0.0.0.jar!/test-res.txt
        // /module-service-loader-typedef-0.0.0.jar!/test-res.txt
        // /module-service-loader-impl-0.0.0.jar!/test-res.txt
        assertEquals(4, tstRes.size)

        assertNotNull(javaClass.getResource("/net/mamoe/console/it/psl/PluginSharedLib.class").also {
            println(it)
        })
        assertEquals(
            1,
            javaClass.classLoader.getResources("net/mamoe/console/it/psl/PluginSharedLib.class")
                .asSequence().toList()
                .also {
                    println(it)
                }.size
        )

        // ************************* resources loading tests *************************

        val miraiConsoleClassPath = "net/mamoe/mirai/console/MiraiConsole.class"
        val miraiBotClassPath = "net/mamoe/mirai/Bot.class"
        val allClassesPath = "META-INF/mirai-console/allclasses.txt"

        fun ClassLoader.getSizeOfResources(path: String): Int {
            return this.getResources(path).toList().size
        }

        assertNull(javaClass.getResource("/$miraiConsoleClassPath"))
        assertNull(javaClass.classLoader.getResource(miraiConsoleClassPath))
        val miraiConsoleClassResourceCount = javaClass.classLoader.getSizeOfResources(miraiConsoleClassPath)
            .also { assertEquals(0, it) }

        assertNull(javaClass.getResource("/$miraiBotClassPath"))
        assertNull(javaClass.classLoader.getResource(miraiBotClassPath))

        val allClassesResourceCount = javaClass.classLoader.getSizeOfResources(allClassesPath)
            .also { assertEquals(0, it) }

        jvmPluginClasspath.shouldResolveConsoleSystemResource = true

        assertNotNull(javaClass.classLoader.getResource(miraiConsoleClassPath))
        assertNotNull(javaClass.classLoader.getResource(miraiBotClassPath))

        assertTrue(javaClass.classLoader.getSizeOfResources(miraiConsoleClassPath) > miraiConsoleClassResourceCount)
        assertTrue(javaClass.classLoader.getSizeOfResources(allClassesPath) > allClassesResourceCount)

        jvmPluginClasspath.shouldResolveConsoleSystemResource = false

        assertNull(javaClass.getResource("/$miraiConsoleClassPath"))
        assertNull(javaClass.classLoader.getResource(miraiConsoleClassPath))
        assertEquals(0, javaClass.classLoader.getSizeOfResources(miraiConsoleClassPath))

        assertNull(javaClass.getResource("/$miraiBotClassPath"))
        assertNull(javaClass.classLoader.getResource(miraiBotClassPath))

        assertEquals(0, javaClass.classLoader.getSizeOfResources(allClassesPath))
    }
}
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData
import net.mamoe.mirai.console.plugin.jvm.reloadPluginData
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import net.mamoe.mirai.console.util.JavaFriendlyApi
import kotlin.test.Test
import kotlin.test.assertEquals


class JAutoSavePluginDataTest : AbstractConsoleInstanceTest() {
    @OptIn(JavaFriendlyApi::class)
    class MyData : JavaAutoSavePluginData("testSaveName") {
        val strMember: Value<String> = value("strMember", "str")
        val intMember: Value<Int> = value("intMember", 1)

        val typed: Value<List<String>> = typedValue(
            "typed",
            createKType(
                List::class.java,
                false,
                createKType(String::class.java, false)
            ),
            listOf("aa", "bb")
        )
    }

    @Test
    fun `can reload`() {
        val instance = MyData()
        mockPlugin.reloadPluginData(instance)
        assertEquals("str", instance.strMember.value)
        assertEquals(Integer.valueOf(1), instance.intMember.value)
        assertEquals(listOf("aa", "bb"), instance.typed.value)

        assertEquals(
            """
            strMember: str
            intMember: 1
            typed: 
              - aa
              - bb
        """.trimIndent(), mockPlugin.dataFolder.resolve("testSaveName.yml").readText()
        )
    }
}
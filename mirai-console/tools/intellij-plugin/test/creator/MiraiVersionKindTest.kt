/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package creator

import net.mamoe.mirai.console.intellij.wizard.sortVersionsDescending
import kotlin.test.Test
import kotlin.test.assertEquals

class MiraiVersionKindTest {

    @Test
    fun sortVersions() {
        assertEquals(
            "2.10.0, 2.10.0-RC, 2.10.0-M1, 2.9.0, 2.9.0-RC, 2.9.0-M2, 2.9.0-M1, 2.7.0, 2.7.0-RC"
                .split(",")
                .map { it.trim() },
            sequenceOf(
                "2.9.0",
                "2.9.0-M1",
                "2.9.0-M2",
                "2.9.0-RC",
                "2.7.0",
                "2.7.0-RC",
                "2.10.0",
                "2.10.0-RC",
                "2.10.0-M1"
            ).sortVersionsDescending().toList()
        )
    }
}
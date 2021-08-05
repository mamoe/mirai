/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

class TestSemanticVersion {
    @Test
    fun `test analyzeMiraiVersion`() {
        assertEquals(
            SemanticVersion(2, 7, null, "RC", "dev", 1),
            analyzeMiraiVersion("2.7-RC-dev-1")
        )
        assertEquals(
            SemanticVersion(2, 7, null, "RC", "dev", 1),
            analyzeMiraiVersion("2.7-RC-dev-1")
        )
        assertEquals(
            SemanticVersion(2, 7, null, "M1", "dev", 1),
            analyzeMiraiVersion("2.7-M1-dev-1")
        )

        assertEquals("2.7.0-M1", SemanticVersion(2, 7, 0, "M1", null, null).toString())
        assertEquals("2.7.0-RC", SemanticVersion(2, 7, 0, "RC", null, null).toString())
        assertEquals("2.7-M1", SemanticVersion(2, 7, null, "M1", null, null).toString())
        assertEquals("2.7-RC", SemanticVersion(2, 7, null, "RC", null, null).toString())
        assertEquals("2.7-dev-1", SemanticVersion(2, 7, null, null, "dev", 1).toString())
        assertEquals("2.7.0-dev-1", SemanticVersion(2, 7, 0, null, "dev", 1).toString())
        assertEquals("2.7-M1-dev-1", SemanticVersion(2, 7, null, "M1", "dev", 1).toString())
        assertEquals("2.7-RC-dev-1", SemanticVersion(2, 7, null, "RC", "dev", 1).toString())
        assertEquals("2.7.0-RC-dev-1", SemanticVersion(2, 7, 0, "RC", "dev", 1).toString())
    }

    @Test
    fun `RC is newer than Milestones`() {
        assertTrue { analyzeMiraiVersion("2.7-RC")!! > analyzeMiraiVersion("2.7-M1")!! }
        assertTrue { analyzeMiraiVersion("2.7-RC")!! > analyzeMiraiVersion("2.7-M999")!! }
        assertTrue { analyzeMiraiVersion("2.7-RC-dev-1")!! > analyzeMiraiVersion("2.7-M999")!! }

        assertTrue { analyzeMiraiVersion("2.7-M1")!! < analyzeMiraiVersion("2.7-RC")!! }
        assertTrue { analyzeMiraiVersion("2.7-M999")!! < analyzeMiraiVersion("2.7-RC")!! }
        assertTrue { analyzeMiraiVersion("2.7-M999")!! < analyzeMiraiVersion("2.7-RC-dev-1")!! }
    }

    @Test
    fun `stable is newer than Milestones`() {
        assertTrue { analyzeMiraiVersion("2.7.0")!! > analyzeMiraiVersion("2.7-M999")!! }
        assertTrue { analyzeMiraiVersion("2.7-M999")!! < analyzeMiraiVersion("2.7.0")!! }

        assertTrue { analyzeMiraiVersion("2.7.0")!! > analyzeMiraiVersion("2.7-RC")!! }
        assertTrue { analyzeMiraiVersion("2.7-RC")!! < analyzeMiraiVersion("2.7.0")!! }

        assertTrue { analyzeMiraiVersion("2.7.0")!! > analyzeMiraiVersion("2.7.0-dev-1")!! }
        assertTrue { analyzeMiraiVersion("2.7.0-dev-1")!! < analyzeMiraiVersion("2.7.0")!! }

        assertTrue { analyzeMiraiVersion("2.7.1")!! > analyzeMiraiVersion("2.7.0-dev-1")!! }
        assertTrue { analyzeMiraiVersion("2.7.0-dev-1")!! < analyzeMiraiVersion("2.7.1")!! }
    }

    @Test
    fun `test getLatestMiraiVersionForBranch`() {
        assertEquals(
            "2.7.0-dev-2",
            getLatestMiraiVersionForBranch(
                """
                <metadata>
                <groupId>net.mamoe</groupId>
                <artifactId>mirai-core-utils</artifactId>
                <versioning>
                <release>2.7-M2</release>
                <latest>2.7-M2</latest>
                <lastUpdated>20210805064152</lastUpdated>
                <versions>
                <version>2.7-M2</version>
                <version>2.7-M1</version>
                <version>2.7-RC</version>
                <version>2.7-RC-dev-1</version>
                <version>2.7.0-dev-2</version>
                <version>2.7.0-dev-1</version>
                </versions>
                </versioning>
                </metadata>
            """.trimIndent(),
                "dev"
            ).toString()
        )

        assertEquals(
            "2.7-RC-dev-3",
            getLatestMiraiVersionForBranch(
                """
                <version>2.7-M1</version>
                <version>2.7-M2</version>
                <version>2.7-RC-dev-3</version>
                <version>2.7-RC-dev-1</version>
                <version>2.7-RC-dev-2</version>
                <version>2.7-RC</version>
            """.trimIndent(),
                "dev"
            ).toString()
        )
    }

    @Test
    fun `test nextVersion`() {
        assertEquals(
            "2.7-RC-dev-1",
            analyzeMiraiVersion("2.7-RC")!!.nextSnapshotVersion("dev").toString()
        )
        assertEquals(
            "2.7-RC-dev-2",
            analyzeMiraiVersion("2.7-RC-dev-1")!!.nextSnapshotVersion("dev").toString()
        )

        assertEquals(
            "2.7-M1-dev-1",
            analyzeMiraiVersion("2.7-M1")!!.nextSnapshotVersion("dev").toString()
        )
        assertEquals(
            "2.7-M1-dev-2",
            analyzeMiraiVersion("2.7-M1-dev-1")!!.nextSnapshotVersion("dev").toString()
        )

        assertEquals(
            "2.7.0-dev-1",
            analyzeMiraiVersion("2.7.0")!!.nextSnapshotVersion("dev").toString()
        )
        assertEquals(
            "2.7.0-dev-2",
            analyzeMiraiVersion("2.7.0-dev-1")!!.nextSnapshotVersion("dev").toString()
        )
    }
}
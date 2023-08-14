/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.logging

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.utils.*
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge
import org.apache.logging.log4j.LogManager
import kotlin.test.*

internal class Log4j2LoggingTest : AbstractLoggingTest() {
    @BeforeTest
    fun init() {
        MiraiLoggerFactoryImplementationBridge.wrapCurrent {
            object : MiraiLogger.Factory {
                override fun create(requester: Class<*>, identity: String?): MiraiLogger {
                    return LogManager.getLogger(requester)
                        .asMiraiLogger(Marker(identity ?: requester.simpleName, MARKER_MIRAI))
                }
            }
        }
    }

    private fun MiraiLogger.cast(): Log4jLoggerAdapter = this as Log4jLoggerAdapter

    @Test
    fun `created is Log4jLoggerAdapter`() {
        val logger = MiraiLogger.Factory.create(Log4j2LoggingTest::class, "test1")
        assertIs<Log4jLoggerAdapter>(logger)
    }

    @Test
    fun `identity is considered as marker`() {
        val logger = MiraiLogger.Factory.create(Log4j2LoggingTest::class, "test1")
        assertEquals("test1", logger.cast().marker!!.name)
    }


    @Test
    fun `test subLogger Marker`() {
        val parent = MiraiLogger.Factory.create(Log4j2LoggingTest::class, "test1")
        val parentMarker = parent.cast().marker!!

        val child = subLoggerImpl(parent, "sub")
        val childMarker = child.markerOrNull!!

        assertEquals("test1", parentMarker.name)
        assertEquals("sub", childMarker.name)

        assertSame(parentMarker, childMarker.parents.single())
        assertSame("test1", childMarker.parents.single().name)
    }

    @Test
    fun `test subLogger Marker 2`() {
        val parent = MiraiLogger.Factory.create(Log4j2LoggingTest::class, "test1")
        val parentMarker = parent.cast().marker!!

        val child = parent.subLogger("sub").subLogger("sub2")
        val childMarker = child.markerOrNull!!

        assertEquals("test1", parentMarker.name)
        assertEquals("sub2", childMarker.name)

        assertSame("sub", childMarker.parents.single().name)
        assertSame(parentMarker, childMarker.parents.single().parents.single())
    }

    @Test
    fun `logging output test`() {
        val logger = LogManager.getLogger(Bot::class.java)
        logger.info("Test")
        MiraiLogger.Factory.create(Bot::class).run {
            info("InfoFF")
        }
    }
}

internal fun MiraiLogger.subLogger(s: String): MiraiLogger {
    return subLoggerImpl(this, s)
}

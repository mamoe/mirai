/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.logging.externalbind.slf4j

import net.mamoe.mirai.console.MiraiConsoleImplementation.ConsoleDataScope.Companion.get
import net.mamoe.mirai.console.internal.data.builtins.DataScope
import net.mamoe.mirai.console.internal.data.builtins.LoggerConfig
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.safeCast
import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.event.SubstituteLoggingEvent
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.NOPLoggerFactory
import org.slf4j.helpers.NOPMDCAdapter
import org.slf4j.helpers.SubstituteLoggerFactory
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

@PublishedApi
internal class MiraiConsoleSLF4JService : SLF4JServiceProvider {
    private val basicMarkerFactory = BasicMarkerFactory()
    private val nopMDCAdapter = NOPMDCAdapter()
    private val dfactory = ILoggerFactory { MiraiConsoleSLF4JAdapter.getCurrentLogFactory().getLogger(it) }

    override fun getMarkerFactory(): IMarkerFactory = basicMarkerFactory
    override fun getMDCAdapter(): MDCAdapter = nopMDCAdapter
    override fun getRequestedApiVersion(): String = "2.0"
    override fun getLoggerFactory(): ILoggerFactory = dfactory
    override fun initialize() {}
}

internal object MiraiConsoleSLF4JAdapter {
    /**
     * Used before mirai-console start
     */
    private val substituteServiceFactory = SubstituteLoggerFactory()

    @Volatile
    private var initialized: Boolean = false

    @Volatile
    private var currentLoggerFactory: ILoggerFactory = substituteServiceFactory

    internal fun getCurrentLogFactory(): ILoggerFactory {
        if (initialized) return currentLoggerFactory

        synchronized(MiraiConsoleSLF4JAdapter::class.java) {
            return currentLoggerFactory
        }
    }

    internal fun doSlf4JInit() {
        synchronized(MiraiConsoleSLF4JAdapter::class.java) {
            val logConfig = DataScope.get<LoggerConfig>()

            currentLoggerFactory = if (logConfig.binding.slf4j) {
                ILoggerFactory { ident ->
                    SLF4JAdapterLogger(MiraiLogger.Factory.create(MiraiConsoleSLF4JAdapter::class.java, ident))
                }
            } else {
                NOPLoggerFactory()
            }
            initialized = true

            // region relay events

            substituteServiceFactory.postInitialization()
            substituteServiceFactory.loggers.forEach { slog ->
                slog.setDelegate(currentLoggerFactory.getLogger(slog.name))
            }

            substituteServiceFactory.eventQueue.let { queue ->
                for (event in queue) {
                    replaySingleEvent(event)
                }
            }
            substituteServiceFactory.clear()
            // endregion
        }
    }


    private fun replaySingleEvent(event: SubstituteLoggingEvent?) {
        if (event == null) return
        val substLogger = event.logger

        substLogger.delegate().safeCast<SLF4JAdapterLogger>()?.process(event)
    }

}
